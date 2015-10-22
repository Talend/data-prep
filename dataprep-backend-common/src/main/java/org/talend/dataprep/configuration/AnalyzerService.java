package org.talend.dataprep.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.api.type.TypeUtils;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
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

@Service
public class AnalyzerService {

    public Analyzer<Analyzers.Result> full(List<ColumnMetadata> columns) {
        try {
            // Configure quality & semantic analysis (if column metadata information is present in stream).
            final DataType.Type[] types = TypeUtils.convert(columns);
            final URI ddPath = AnalyzerService.class.getResource("/luceneIdx/dictionary").toURI(); //$NON-NLS-1$
            final URI kwPath = AnalyzerService.class.getResource("/luceneIdx/keyword").toURI(); //$NON-NLS-1$
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
            return Analyzers.with(new ValueQualityAnalyzer(types),
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
                    new SemanticAnalyzer(categoryBuilder));
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    public Analyzer<Analyzers.Result> qualityAnalysis(List<ColumnMetadata> columns) {
        DataType.Type[] types = TypeUtils.convert(columns);
        // Run analysis
        final CategoryRecognizerBuilder categoryBuilder;
        try {
            final URI ddPath = AnalyzerService.class.getResource("/luceneIdx/dictionary").toURI(); //$NON-NLS-1$
            final URI kwPath = AnalyzerService.class.getResource("/luceneIdx/keyword").toURI(); //$NON-NLS-1$
            categoryBuilder = CategoryRecognizerBuilder.newBuilder() //
                    .ddPath(ddPath) //
                    .kwPath(kwPath) //
                    .setMode(CategoryRecognizerBuilder.Mode.LUCENE);
        } catch (URISyntaxException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_ANALYZE_DATASET_QUALITY, e);
        }
        final ValueQualityAnalyzer valueQualityAnalyzer = new ValueQualityAnalyzer(types);
        valueQualityAnalyzer.setStoreInvalidValues(true);
        return Analyzers.with(valueQualityAnalyzer, //
                new SummaryAnalyzer(types), //
                new SemanticAnalyzer(categoryBuilder), //
                new DataTypeAnalyzer());
    }

    public Analyzer<Analyzers.Result> schemaAnalysis(List<ColumnMetadata> columns) {
        DataType.Type[] types = TypeUtils.convert(columns);
        // Run analysis
        final CategoryRecognizerBuilder categoryBuilder;
        try {
            final URI ddPath = AnalyzerService.class.getResource("/luceneIdx/dictionary").toURI(); //$NON-NLS-1$
            final URI kwPath = AnalyzerService.class.getResource("/luceneIdx/keyword").toURI(); //$NON-NLS-1$
            categoryBuilder = CategoryRecognizerBuilder.newBuilder() //
                    .ddPath(ddPath) //
                    .kwPath(kwPath) //
                    .setMode(CategoryRecognizerBuilder.Mode.LUCENE);
        } catch (URISyntaxException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_ANALYZE_DATASET_QUALITY, e);
        }
        return Analyzers.with(new ValueQualityAnalyzer(types), //
                new SemanticAnalyzer(categoryBuilder), //
                new DataTypeAnalyzer());
    }
}
