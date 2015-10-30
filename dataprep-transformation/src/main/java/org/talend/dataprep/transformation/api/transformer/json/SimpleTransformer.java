package org.talend.dataprep.transformation.api.transformer.json;

import java.io.IOException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTransformer.class);

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

    private Analyzer<Analyzers.Result> configureAnalyzer(TransformationContext context, DataSetRow row) {
        Analyzer<Analyzers.Result> analyzer = (Analyzer<Analyzers.Result>) context.get(CONTEXT_ANALYZER);
        if (analyzer == null) {
            final List<ColumnMetadata> columns = row.getRowMetadata().getColumns();
            analyzer = analyzerService.full(columns);
            context.put(CONTEXT_ANALYZER, analyzer);
        }
        return analyzer;
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
            // Row transformations
            Stream<DataSetRow> records = input.getRecords();
            // Apply actions to records
            for (DataSetRowAction action : rowActions) {
                records = records.map(r -> action.apply(r, context));
            }
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
            final AtomicBoolean wroteMetadata = new AtomicBoolean(false); // Flag to prevent multiple writes of header
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

}
