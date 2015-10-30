package org.talend.dataprep.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.api.type.TypeUtils;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadataUtils;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;
import org.talend.dataquality.semantic.statistics.SemanticAnalyzer;
import org.talend.dataquality.semantic.statistics.SemanticQualityAnalyzer;
import org.talend.dataquality.standardization.index.ClassPathDirectory;
import org.talend.dataquality.statistics.cardinality.CardinalityAnalyzer;
import org.talend.dataquality.statistics.frequency.DataFrequencyAnalyzer;
import org.talend.dataquality.statistics.frequency.PatternFrequencyAnalyzer;
import org.talend.dataquality.statistics.numeric.histogram.HistogramAnalyzer;
import org.talend.dataquality.statistics.numeric.histogram.HistogramColumnParameter;
import org.talend.dataquality.statistics.numeric.histogram.HistogramParameter;
import org.talend.dataquality.statistics.numeric.quantile.QuantileAnalyzer;
import org.talend.dataquality.statistics.numeric.summary.SummaryAnalyzer;
import org.talend.dataquality.statistics.quality.DataTypeQualityAnalyzer;
import org.talend.dataquality.statistics.quality.ValueQualityAnalyzer;
import org.talend.dataquality.statistics.text.TextLengthAnalyzer;
import org.talend.datascience.common.inference.Analyzer;
import org.talend.datascience.common.inference.Analyzers;
import org.talend.datascience.common.inference.ValueQualityStatistics;
import org.talend.datascience.common.inference.type.DataType;
import org.talend.datascience.common.inference.type.DataTypeAnalyzer;

@Service
public class AnalyzerService implements DisposableBean {

    public static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerService.class);

    private static CategoryRecognizerBuilder newCategoryRecognizer() {
        try {
            final URI ddPath = AnalyzerService.class.getResource("/luceneIdx/dictionary").toURI(); //$NON-NLS-1$
            final URI kwPath = AnalyzerService.class.getResource("/luceneIdx/keyword").toURI(); //$NON-NLS-1$
            return CategoryRecognizerBuilder.newBuilder() //
                    .ddPath(ddPath) //
                    .kwPath(kwPath) //
                    .lucene();
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Build a {@link ValueQualityAnalyzer analyzer} for both type and domain validation.
     *
     * @param columns The column metadata information where type and domain are extracted from.
     * @return A non-initialized but configured {@link ValueQualityAnalyzer}.
     */
    public ValueQualityAnalyzer qualityAnalyzer(List<ColumnMetadata> columns) {
        final CategoryRecognizerBuilder categoryBuilder = newCategoryRecognizer();
        final DataType.Type[] types = TypeUtils.convert(columns);
        List<String> domainList = columns.stream() //
                .map(c -> {
                    final SemanticCategoryEnum category = SemanticCategoryEnum.getCategoryById(c.getDomain().toUpperCase());
                    return category == null ? SemanticCategoryEnum.UNKNOWN.getId() : category.getId();
                }) //
                .collect(Collectors.toList());
        final String[] domains = domainList.toArray(new String[domainList.size()]);
        DataTypeQualityAnalyzer dataTypeQualityAnalyzer = new DataTypeQualityAnalyzer(types);
        SemanticQualityAnalyzer semanticQualityAnalyzer = new SemanticQualityAnalyzer(categoryBuilder, domains);
        return new ValueQualityAnalyzer(dataTypeQualityAnalyzer, semanticQualityAnalyzer, true);
    }

    public Analyzer<Analyzers.Result> full(List<ColumnMetadata> columns) {
        // Configure quality & semantic analysis (if column metadata information is present in stream).
        final DataType.Type[] types = TypeUtils.convert(columns);
        final CategoryRecognizerBuilder categoryBuilder = newCategoryRecognizer();
        // Set min and max for each column in histogram
        final HistogramParameter histogramParameter = new HistogramParameter();
        for (int i = 0; i < columns.size(); i++) {
            ColumnMetadata column = columns.get(i);
            final boolean isNumeric = Type.NUMERIC.isAssignableFrom(Type.get(column.getType()));
            if (isNumeric) {
                final Statistics statistics = column.getStatistics();
                final double min = statistics.getMin();
                final double max = statistics.getMax();
                if (min < max) {
                    final HistogramColumnParameter columnParameter = new HistogramColumnParameter();
                    columnParameter.setParameters(min, max, 20);
                    histogramParameter.putColumnParameter(i, columnParameter);
                }
            }
        }
        final HistogramAnalyzer histogramAnalyzer = new HistogramAnalyzer(types, histogramParameter);
        final SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(categoryBuilder);
        // Configure value quality analysis
        final ValueQualityAnalyzer valueQualityAnalyzer = qualityAnalyzer(columns);
        final Analyzer<Analyzers.Result> analyzer = Analyzers.with(
                // Value quality (invalid values...)
                valueQualityAnalyzer,
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
                new TextLengthAnalyzer(),
                // Semantic analysis
                semanticAnalyzer);
        analyzer.init();
        return analyzer;
    }

    public Analyzer<Analyzers.Result> qualityAnalysis(List<ColumnMetadata> columns) {
        DataType.Type[] types = TypeUtils.convert(columns);
        // Run analysis
        final CategoryRecognizerBuilder categoryBuilder = newCategoryRecognizer();
        // Configure value quality analysis
        final ValueQualityAnalyzer valueQualityAnalyzer = qualityAnalyzer(columns);
        final Analyzer<Analyzers.Result> analyzer = Analyzers.with(valueQualityAnalyzer, //
                new SummaryAnalyzer(types), //
                new SemanticAnalyzer(categoryBuilder), //
                new DataTypeAnalyzer());
        analyzer.init();
        return analyzer;
    }

    public Analyzer<Analyzers.Result> schemaAnalysis(List<ColumnMetadata> columns) {
        // Run analysis
        final CategoryRecognizerBuilder categoryBuilder = newCategoryRecognizer();
        // Configure value quality analysis
        final ValueQualityAnalyzer valueQualityAnalyzer = qualityAnalyzer(columns);
        final Analyzer<Analyzers.Result> analyzer = Analyzers.with(valueQualityAnalyzer, //
                new SemanticAnalyzer(categoryBuilder), //
                new DataTypeAnalyzer());
        analyzer.init();
        return analyzer;
    }

    @Override
    public void destroy() throws Exception {
        LOGGER.info("Clean up analyzers...");
        final Map<String, Analyzer<ValueQualityStatistics>> cache = ActionMetadataUtils.getAnalyzerCache();
        for (Map.Entry<String, Analyzer<ValueQualityStatistics>> entry : cache.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception e) {
                LOGGER.error("Unable to close analyzer for type '" + entry.getKey() + "'.", e);
            }
        }
        ClassPathDirectory.destroy();
        LOGGER.info("Clean up analyzers done.");
    }
}
