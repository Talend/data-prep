package org.talend.dataprep.transformation.api.transformer.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataprep.configuration.AnalyzerService;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.transformation.api.action.ActionParser;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.ParsedActions;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.transformer.Transformer;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.format.WriterRegistrationService;
import org.talend.datascience.common.inference.Analyzer;
import org.talend.datascience.common.inference.Analyzers;

/**
 * Base implementation of the Transformer interface.
 */
@Component
class SimpleTransformer implements Transformer {

    private static final String CONTEXT_ANALYZER = "analyzer";

    private static final int ANALYSIS_BUFFER_SIZE = 20;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTransformer.class);

    private static final NullAnalyzer NULL_ANALYZER = new NullAnalyzer();

    @Autowired
    private StatisticsAdapter adapter;

    @Autowired
    private ActionParser actionParser;

    /** Service who knows about registered writers. */
    @Autowired
    private WriterRegistrationService writersService;

    /** The data-prep jackson builder. */
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private AnalyzerService analyzerService;

    private final List<DataSetRow> initialAnalysisBuffer = new ArrayList<>(ANALYSIS_BUFFER_SIZE + 1);

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

    // Indicate what the current status related to Analyzer configuration.
    private AnalysisStatus currentAnalysisStatus = AnalysisStatus.SCHEMA_ANALYSIS;

    private Analyzer<Analyzers.Result> configureAnalyzer(TransformationContext context, DataSetRow row) {
        switch (currentAnalysisStatus) {
            case SCHEMA_ANALYSIS:
                if (initialAnalysisBuffer.size() < ANALYSIS_BUFFER_SIZE) {
                    initialAnalysisBuffer.add(row.clone());
                    return NULL_ANALYZER; // Returns a no op (like a "> /dev/null").
                } else {
                    emptyInitialAnalysisBuffer(context, row);
                    return configureFullAnalyzer(context, row.getRowMetadata().getColumns());
                }
            case FULL_ANALYSIS:
                return configureFullAnalyzer(context, row.getRowMetadata().getColumns());
            default:
                throw new UnsupportedOperationException("Unable to process state '" + currentAnalysisStatus + "'.");
        }
    }

    // Get or create a full analyzer (with all possible analysis), columns must contain correct type information.
    private Analyzer<Analyzers.Result> configureFullAnalyzer(TransformationContext context, List<ColumnMetadata> columns) {
        Analyzer<Analyzers.Result> analyzer = (Analyzer<Analyzers.Result>) context.get(CONTEXT_ANALYZER);
        if (analyzer == null) {
            analyzer = analyzerService.full(columns);
            context.put(CONTEXT_ANALYZER, analyzer);
        }
        return analyzer;
    }

    // Empty the initial buffer and perform an early schema analysis, configure a full analyzer, run full analysis on
    private void emptyInitialAnalysisBuffer(TransformationContext context, DataSetRow row) {
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
        adapter.adapt(columns, schema.getResult());
        // Now configure the actual (full) analysis and don't forget to process stored records
        Analyzer<Analyzers.Result> analyzer = configureFullAnalyzer(context, columns);
        for (DataSetRow dataSetRow : initialAnalysisBuffer) {
            analyzer.analyze(dataSetRow.order(columns).toArray(DataSetRow.SKIP_TDP_ID));
        }
        // Clear all stored records and set current status to FULL_ANALYSIS for further records.
        initialAnalysisBuffer.clear();
        currentAnalysisStatus = AnalysisStatus.FULL_ANALYSIS;
    }

    /**
     * @see Transformer#transform(DataSet, Configuration)
     */
    @Override
    public void transform(DataSet input, Configuration configuration) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null.");
        }
        final TransformerWriter writer = writersService.getWriter(configuration.formatId(), configuration.output(),
                configuration.getArguments());
        try {
            writer.startObject();
            final ParsedActions parsedActions = actionParser.parse(configuration.getActions());
            final List<DataSetRowAction> rowActions = parsedActions.getRowTransformers();
            final boolean transformColumns = !input.getColumns().isEmpty();
            TransformationContext context = configuration.getTransformationContext();
            final AtomicBoolean wroteMetadata = new AtomicBoolean(false);
            // Row transformations
            Stream<DataSetRow> records = input.getRecords();
            // Apply actions to records
            for (DataSetRowAction action : rowActions) {
                records = records.map(r -> action.apply(r, context));
            }
            // Analyze content after all actions were applied to row
            records = records.map(r -> {
                if (transformColumns) {
                    // Use analyzer (for empty values, semantic...)
                    if (!r.isDeleted()) {
                        final DataSetRow row = r.order(r.getRowMetadata().getColumns());
                        configureAnalyzer(context, row).analyze(row.toArray(DataSetRow.SKIP_TDP_ID));
                    }
                }
                return r;
            });
            // Write transformed records to stream
            final Stack<DataSetRow> postProcessQueue = new Stack<>(); // Stack to remember last processed row.
            writer.fieldName("records");
            writer.startArray();
            records.forEach(row -> {
                try {
                    if (!postProcessQueue.empty()) {
                        postProcessQueue.pop();
                    }
                    if (writer.requireMetadataForHeader() && !wroteMetadata.get()) {
                        writer.write(row.getRowMetadata());
                        wroteMetadata.set(true);
                    }
                    if (row.shouldWrite()) {
                        writer.write(row);
                        context.freezeActionContexts();
                    }
                    postProcessQueue.push(row); // In the end, last row remains in stack.
                } catch (IOException e) {
                    throw new TDPException(TransformationErrorCodes.UNABLE_TRANSFORM_DATASET, e);
                }
            });
            writer.endArray();
            // Write columns (using last processed row).
            if (!wroteMetadata.get() && transformColumns) {
                writer.fieldName("columns");
                final DataSetRow row = postProcessQueue.pop();
                // End analysis and set the statistics
                emptyInitialAnalysisBuffer(context, row); // Call in case row number < ANALYSIS_BUFFER_SIZE
                final Analyzer<Analyzers.Result> analyzer = (Analyzer<Analyzers.Result>) context.get(CONTEXT_ANALYZER);
                if (analyzer != null) {
                    // Analyzer may not be initialized when all rows were deleted.
                    analyzer.end();
                    adapter.adapt(row.getRowMetadata().getColumns(), analyzer.getResult());
                }
                writer.write(row.getRowMetadata());
            }
            if (postProcessQueue.size() > 1) {
                LOGGER.warn("Too many processed rows in stack (expected 1, got {}).", postProcessQueue.size());
            }
            postProcessQueue.clear(); // Clear last processed row.
            writer.endObject();
            writer.flush();
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TRANSFORM_DATASET, e);
        }
    }

    @Override
    public boolean accept(Configuration configuration) {
        return Configuration.class.equals(configuration.getClass()) && configuration.volume() == Configuration.Volume.SMALL;
    }

    /**
     * An implementation of {@link Analyzer} that does not record nor analyze anything.
     */
    private static class NullAnalyzer implements Analyzer<Analyzers.Result> {
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
