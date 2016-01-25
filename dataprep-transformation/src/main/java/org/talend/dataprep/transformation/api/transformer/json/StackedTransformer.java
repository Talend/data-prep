package org.talend.dataprep.transformation.api.transformer.json;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.*;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.stream.ExtendedStream;
import org.talend.dataprep.transformation.BaseTransformer;
import org.talend.dataprep.transformation.api.action.ActionParser;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.ParsedActions;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.transformer.Transformer;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.format.WriterRegistrationService;
import org.talend.datascience.common.inference.Analyzer;
import org.talend.datascience.common.inference.Analyzers;

@Component
@Scope("prototype")
public class StackedTransformer implements Transformer {

    private static final int ANALYSIS_BUFFER_SIZE = 20;

    private static final NullAnalyzer NULL_ANALYZER = new NullAnalyzer();

    private static final Logger LOGGER = LoggerFactory.getLogger(StackedTransformer.class);

    private final List<DataSetRow> initialAnalysisBuffer = new ArrayList<>();

    @Autowired
    private StatisticsAdapter adapter;

    @Autowired
    private ActionParser actionParser;

    /** Service who knows about registered writers. */
    @Autowired
    private WriterRegistrationService writersService;

    @Autowired
    private AnalyzerService analyzerService;

    /**
     * A filter to filter out non modified columns, by default include all columns.
     */
    private Predicate<ColumnMetadata> modifiedColumnsFilter = c -> true;

    private enum AnalysisStatus {
        /**
         * Status indicating transformation hasn't yet decided what is the schema of the transformed content.
         */
        SCHEMA_ANALYSIS,
        /**
         * Schema was detected (based on first {@link #ANALYSIS_BUFFER_SIZE} rows). This status indicates transformer has
         * enough information (about type & al.) to perform a full analysis of the data.
         */
        FULL_ANALYSIS

    }

    /** Indicate what the current status related to Analyzer configuration. */
    private AnalysisStatus currentAnalysisStatus = AnalysisStatus.SCHEMA_ANALYSIS;

    private Analyzer<Analyzers.Result> analyzer = NULL_ANALYZER; // Uses a no op (like a "> /dev/null") by default.

    /**
     * Configure the analyzer for quality depending on the current tAnalysisStatus.
     *
     * @param row the current row.
     * @see StackedTransformer#currentAnalysisStatus
     */
    private void configureAnalyzer(DataSetRow row) {
        switch (currentAnalysisStatus) {
            case SCHEMA_ANALYSIS:
                if (initialAnalysisBuffer.size() < ANALYSIS_BUFFER_SIZE) {
                    initialAnalysisBuffer.add(row.clone());
                } else {
                    emptyInitialAnalysisBuffer(row);
                }
            break;
            case FULL_ANALYSIS:
        default:
                // Nothing to do
        }
    }

    /**
     * Empty the initial buffer and perform an early schema analysis, configure a full analyzer, run full analysis on.
     *
     * @param row the current row.
     */
    private void emptyInitialAnalysisBuffer(DataSetRow row) {
        if (currentAnalysisStatus == AnalysisStatus.FULL_ANALYSIS || initialAnalysisBuffer.isEmpty()) {
            // Got called for nothing
            return;
        }
        // Perform a first rough guess of records in reservoir
        final List<ColumnMetadata> columns = row.getRowMetadata().getColumns();
        final Analyzer<Analyzers.Result> schema = analyzerService.schemaAnalysis(columns);
        for (DataSetRow dataSetRow : initialAnalysisBuffer) {
            schema.analyze(dataSetRow.order(columns).toArray(DataSetRow.SKIP_TDP_ID));
        }
        adapter.adapt(columns, schema.getResult(), modifiedColumnsFilter);
        // TODO To analyze data on deleted columns, removes filter (accept all).
        modifiedColumnsFilter = c -> true;
        // Now configure the actual (full) analysis and don't forget to process stored records
        analyzer = analyzerService.full(row.getRowMetadata().getColumns());
        for (DataSetRow dataSetRow : initialAnalysisBuffer) {
            analyzer.analyze(dataSetRow.order(columns).toArray(DataSetRow.SKIP_TDP_ID));
        }
        // Clear all stored records and set current status to FULL_ANALYSIS for further records.
        initialAnalysisBuffer.clear();
        currentAnalysisStatus = AnalysisStatus.FULL_ANALYSIS;
    }

    @Override
    public void transform(DataSet input, Configuration configuration) {
        try {
            final ParsedActions parsedActions = actionParser.parse(configuration.getActions());
            // Keeps modified columns and new columns for statistics
            final DataSetMetadata metadata = input.getMetadata();
            if (metadata != null) {
                final RowMetadata inputRowMetadata = metadata.getRowMetadata();
                final List<ColumnMetadata> inputColumns = inputRowMetadata.getColumns();
                final Set<String> originalColumns = inputColumns.stream().map(ColumnMetadata::getId).collect(Collectors.toSet());
                final Set<String> modifiedColumns = new HashSet<>();
                final List<Action> actions = parsedActions.getAllActions();
                if (actions != null) {
                    for (Action action : actions) {
                        final String modifiedColumnId = action.getParameters().get(ImplicitParameters.COLUMN_ID.getKey());
                        modifiedColumns.add(modifiedColumnId);
                    }
                }
                // Is modified by action OR not contained in original data set metadata (i.e. new column).
                // TODO This filter does *not* take into account actions that deletes the whole line!
                modifiedColumnsFilter = c -> modifiedColumns.contains(c.getId()) || !originalColumns.contains(c.getId());
            } else {
                // No metadata available, consider all columns.
                modifiedColumnsFilter = c -> true;
            }

            final List<DataSetRowAction> allActions = parsedActions.getRowTransformers();
            final TransformerWriter writer = writersService.getWriter(configuration.formatId(), configuration.output(), configuration.getArguments());

            TransformationContext context = new TransformationContext();
            ExtendedStream<DataSetRow> records = BaseTransformer.baseTransform(input.getRecords(), allActions, context)
                    .map(r -> {
                        try {
                            return analyzeRecords(input, r);
                        } catch (Exception e) {
                            LOGGER.debug("Unable to compute statistics on '{}'", r, e);
                            return r;
                        }
                    });
            // Write records
            Deque<DataSetRow> processingRows = new ArrayDeque<>();
            writer.startObject();
            records.mapOnce(r -> startRecords(writer, r), r -> r) //
                .forEach(r -> writeRecords(writer, processingRows, r));
            writer.endArray();
            //
            if (!writer.requireMetadataForHeader()) {
                writeMetadata(writer, processingRows.pop());
            }
            writer.endObject();
            writer.flush();
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TRANSFORM_DATASET, e);
        } finally {
            // cleanup context (to make sure resources are properly closed)
            configuration.getTransformationContext().cleanup();
            try {
                analyzer.close();
            } catch (Exception e) {
                LOGGER.warn("Unable to close analyzer after transformation.", e);
            }
        }
    }

    /**
     * Write transformed records.
     *
     * @param writer the writer to use.
     * @param processingRows the stack of processing rows.
     * @param r the current row to write.
     */
    private void writeRecords(TransformerWriter writer, Deque<DataSetRow> processingRows, DataSetRow r) {
        try {
            if (processingRows.size() > 0) {
                processingRows.pop();
            }
            if (r.shouldWrite()) {
                writer.write(r);
            }
            processingRows.push(r);
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TRANSFORM_DATASET, e);
        }
    }

    private DataSetRow analyzeRecords(DataSet input, DataSetRow r) {
        // Use analyzer (for empty values, semantic...)
        if (input.getMetadata() != null && !r.isDeleted()) {
            final List<ColumnMetadata> columns = r.getRowMetadata().getColumns();
            final DataSetRow row = r.order(columns);
            final String[] array = row.toArray(DataSetRow.SKIP_TDP_ID);
            // Configure analyzer (and eventually switch analyzer state)
            configureAnalyzer(row);
            // Now that analyzer is properly configured, analyze values
            for (int i = 0; i < columns.size(); i++) {
                // Removes non modified values from analysis
                if (!modifiedColumnsFilter.test(columns.get(i))) {
                    array[i] = null;
                }
            }
            analyzer.analyze(array);
        }
        return r;
    }

    private DataSetRow startRecords(TransformerWriter writer, DataSetRow r) {
        try {
            // Write metadata if writer asks for it
            if (writer.requireMetadataForHeader()) {
                writeMetadata(writer, r);
            }
            // Now starts records
            writer.fieldName("records");
            writer.startArray();
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TRANSFORM_DATASET, e);
        }
        return r;
    }

    private DataSetRow writeMetadata(TransformerWriter writer, DataSetRow row) {
        try {
            emptyInitialAnalysisBuffer(row); // Call in case row number < ANALYSIS_BUFFER_SIZE
            // Analyzer may not be initialized when all rows were deleted.
            analyzer.end();
            adapter.adapt(row.getRowMetadata().getColumns(), analyzer.getResult(), modifiedColumnsFilter);
            writer.fieldName("metadata");
            writer.startObject();
            {
                writer.fieldName("columns");
                writer.write(row.getRowMetadata());
            }
            writer.endObject();
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TRANSFORM_DATASET, e);
        }
        return row;
    }

    @Override
    public boolean accept(Configuration configuration) {
        return Configuration.class.equals(configuration.getClass()) && configuration.volume() == Configuration.Volume.SMALL;
    }

    private static class NullAnalyzer implements Analyzer<Analyzers.Result> {

        private static final long serialVersionUID = 1L;

        @Override
        public void init() {
            // Nothing to do
        }

        @Override

        public boolean analyze(String... strings) {
            // Nothing to do
            return true;
        }

        @Override
        public void end() {
            // Nothing to do
        }

        @Override
        public List<Analyzers.Result> getResult() {
            return Collections.emptyList();
        }

        @Override
        public Analyzer<Analyzers.Result> merge(Analyzer<Analyzers.Result> analyzer) {
            return this;
        }

        @Override
        public void close() throws Exception {
            // Nothing to do
        }
    }
}
