package org.talend.dataprep.transformation.api.transformer.json;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.api.type.TypeUtils;
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
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;
import org.talend.dataquality.semantic.statistics.SemanticAnalyzer;
import org.talend.dataquality.statistics.cardinality.CardinalityAnalyzer;
import org.talend.dataquality.statistics.frequency.DataFrequencyAnalyzer;
import org.talend.dataquality.statistics.frequency.PatternFrequencyAnalyzer;
import org.talend.dataquality.statistics.numeric.histogram.HistogramAnalyzer;
import org.talend.dataquality.statistics.numeric.histogram.HistogramColumnParameter;
import org.talend.dataquality.statistics.numeric.histogram.HistogramParameter;
import org.talend.dataquality.statistics.numeric.quantile.QuantileAnalyzer;
import org.talend.dataquality.statistics.numeric.summary.SummaryAnalyzer;
import org.talend.dataquality.statistics.quality.ValueQualityAnalyzer;
import org.talend.dataquality.statistics.text.TextLengthAnalyzer;
import org.talend.datascience.common.inference.Analyzer;
import org.talend.datascience.common.inference.Analyzers;
import org.talend.datascience.common.inference.type.DataType;
import org.talend.datascience.common.inference.type.DataTypeAnalyzer;

/**
 * Base implementation of the Transformer interface.
 */
@Component
class SimpleTransformer implements Transformer {

    public static final String CONTEXT_ANALYZER = "analyzer";
    public static final Logger LOGGER = LoggerFactory.getLogger(SimpleTransformer.class);

    @Autowired
    StatisticsAdapter adapter;

    @Autowired
    ActionParser actionParser;

    /** Service who knows about registered writers. */
    @Autowired
    private WriterRegistrationService writersService;

    /** The data-prep jackson builder. */
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    private static Analyzer<Analyzers.Result> configureAnalyzer(TransformationContext context, DataSetRow row) {
        try {
            Analyzer<Analyzers.Result> analyzer = (Analyzer<Analyzers.Result>) context.get(CONTEXT_ANALYZER);
            if (analyzer == null) {
                // Configure quality & semantic analysis (if column metadata information is present in stream).
                final List<ColumnMetadata> columns = row.getRowMetadata().getColumns();
                final DataType.Type[] types = TypeUtils.convert(columns);
                final URI ddPath = SimpleTransformer.class.getResource("/luceneIdx/dictionary").toURI(); //$NON-NLS-1$
                final URI kwPath = SimpleTransformer.class.getResource("/luceneIdx/keyword").toURI(); //$NON-NLS-1$
                final CategoryRecognizerBuilder categoryBuilder = CategoryRecognizerBuilder.newBuilder() //
                        .ddPath(ddPath) //
                        .kwPath(kwPath) //
                        .setMode(CategoryRecognizerBuilder.Mode.LUCENE);
                // Set min and max for each column in histogram
                final HistogramParameter histogramParameter = new HistogramParameter();
                for (int i = 0; i < columns.size(); i++) {
                    ColumnMetadata column = columns.get(i);
                    final boolean isNumeric = Type.NUMERIC.isAssignableFrom(Type.get(column.getType()));
                    if (isNumeric) {
                        final HistogramColumnParameter columnParameter = new HistogramColumnParameter();
                        final Statistics statistics = column.getStatistics();
                        columnParameter.setParameters(statistics.getMin(), statistics.getMax(), 20);
                        histogramParameter.putColumnParameter(i, columnParameter);
                    }
                }
                final HistogramAnalyzer histogramAnalyzer = new HistogramAnalyzer(types, histogramParameter);
                analyzer = Analyzers.with(new ValueQualityAnalyzer(types),
                        // Type analysis (especially useful for new columns).
                        new DataTypeAnalyzer(),
                        // Cardinality (distinct + duplicate)
                        new CardinalityAnalyzer(),
                        // Frequency analysis (Pattern + data)
                        new DataFrequencyAnalyzer(), new PatternFrequencyAnalyzer(),
                        // Quantile analysis
                        new QuantileAnalyzer(types),
                        // Summary (min, max, mean, variance)
                        new SummaryAnalyzer(types),
                        // Histogram
                        histogramAnalyzer,
                        // Text length analysis (for applicable columns)
                        new TextLengthAnalyzer(), new SemanticAnalyzer(categoryBuilder));
                context.put(CONTEXT_ANALYZER, analyzer);
            }
            return analyzer;
        } catch (URISyntaxException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TRANSFORM_DATASET, e);
        }
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
                analyzer.end();
                adapter.adapt(row.getRowMetadata().getColumns(), analyzer.getResult());
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
