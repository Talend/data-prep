// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.quality;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.dataset.statistics.date.StreamDateHistogramAnalyzer;
import org.talend.dataprep.api.dataset.statistics.date.StreamDateHistogramStatistics;
import org.talend.dataprep.api.dataset.statistics.number.StreamNumberHistogramAnalyzer;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.api.type.TypeUtils;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadataUtils;
import org.talend.dataprep.transformation.api.action.metadata.date.DateParser;
import org.talend.dataprep.transformation.api.transformer.json.NullAnalyzer;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;
import org.talend.dataquality.semantic.statistics.SemanticAnalyzer;
import org.talend.dataquality.semantic.statistics.SemanticQualityAnalyzer;
import org.talend.dataquality.semantic.statistics.SemanticType;
import org.talend.dataquality.standardization.index.ClassPathDirectory;
import org.talend.dataquality.statistics.cardinality.CardinalityAnalyzer;
import org.talend.dataquality.statistics.cardinality.CardinalityStatistics;
import org.talend.dataquality.statistics.frequency.AbstractFrequencyAnalyzer;
import org.talend.dataquality.statistics.frequency.DataTypeFrequencyAnalyzer;
import org.talend.dataquality.statistics.frequency.DataTypeFrequencyStatistics;
import org.talend.dataquality.statistics.frequency.pattern.CompositePatternFrequencyAnalyzer;
import org.talend.dataquality.statistics.frequency.pattern.PatternFrequencyStatistics;
import org.talend.dataquality.statistics.frequency.recognition.AbstractPatternRecognizer;
import org.talend.dataquality.statistics.frequency.recognition.DateTimePatternRecognizer;
import org.talend.dataquality.statistics.frequency.recognition.EmptyPatternRecognizer;
import org.talend.dataquality.statistics.frequency.recognition.LatinExtendedCharPatternRecognizer;
import org.talend.dataquality.statistics.numeric.quantile.QuantileAnalyzer;
import org.talend.dataquality.statistics.numeric.quantile.QuantileStatistics;
import org.talend.dataquality.statistics.numeric.summary.SummaryAnalyzer;
import org.talend.dataquality.statistics.numeric.summary.SummaryStatistics;
import org.talend.dataquality.statistics.quality.DataTypeQualityAnalyzer;
import org.talend.dataquality.statistics.quality.ValueQualityAnalyzer;
import org.talend.dataquality.statistics.text.TextLengthAnalyzer;
import org.talend.dataquality.statistics.text.TextLengthStatistics;
import org.talend.dataquality.statistics.type.DataTypeAnalyzer;
import org.talend.dataquality.statistics.type.DataTypeEnum;
import org.talend.dataquality.statistics.type.DataTypeOccurences;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;
import org.talend.dataquality.common.inference.ValueQualityStatistics;

/**
 * Service in charge of analyzing dataset quality.
 */
@Service
public class AnalyzerService implements DisposableBean {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerService.class);

    @Autowired
    private DateParser dateParser;

    @Value("#{'${luceneIndexStrategy:singleton}'}")
    private String luceneIndexStrategy;

    /**
     * In case of a date column, return the most used pattern.
     *
     * @param column the column to inspect.
     * @return the most used pattern or null if there's none.
     */
    public static String getMostUsedDatePattern(ColumnMetadata column) {
        // only filter out non date columns
        if (Type.get(column.getType()) != Type.DATE) {
            return null;
        }
        final List<PatternFrequency> patternFrequencies = column.getStatistics().getPatternFrequencies();
        if (!patternFrequencies.isEmpty()) {
            patternFrequencies.sort((p1, p2) -> Long.compare(p2.getOccurrences(), p1.getOccurrences()));
            return patternFrequencies.get(0).getPattern();
        }
        return null;
    }

    private static AbstractFrequencyAnalyzer buildPatternAnalyzer(List<ColumnMetadata> columns) {
        // deal with specific date, even custom date pattern
        final DateTimePatternRecognizer dateTimePatternFrequencyAnalyzer = new DateTimePatternRecognizer();
        List<String> patterns = new ArrayList<>(columns.size());
        for (ColumnMetadata column : columns) {
            final String pattern = getMostUsedDatePattern(column);
            if (StringUtils.isNotBlank(pattern)) {
                patterns.add(pattern);
            }
        }
        dateTimePatternFrequencyAnalyzer.addCustomDateTimePatterns(patterns);

        // warning, the order is important
        List<AbstractPatternRecognizer> patternFrequencyAnalyzers = new ArrayList<>();
        patternFrequencyAnalyzers.add(new EmptyPatternRecognizer());
        patternFrequencyAnalyzers.add(dateTimePatternFrequencyAnalyzer);
        patternFrequencyAnalyzers.add(new LatinExtendedCharPatternRecognizer());

        return new CompositePatternFrequencyAnalyzer(patternFrequencyAnalyzers);
    }

    /**
     * Return the list of most used patterns for dates.
     *
     * @param columns the columns to analyze.
     * @return the list of most used patterns for dates or an empty list if there's none.
     */
    private List<String> getMostUsedDatePatterns(List<ColumnMetadata> columns) {

        List<String> patterns = new ArrayList<>(columns.size());
        for (ColumnMetadata column : columns) {
            final String pattern = getMostUsedDatePattern(column);
            if (StringUtils.isNotBlank(pattern)) {
                patterns.add(pattern);
            }
        }

        return patterns;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("Analyzer service lucene index strategy set to '{}'", luceneIndexStrategy);
        if ("basic".equals(luceneIndexStrategy)) {
            ClassPathDirectory.setProvider(new ClassPathDirectory.BasicProvider());
        } else if ("singleton".equals(luceneIndexStrategy)) {
            ClassPathDirectory.setProvider(new ClassPathDirectory.SingletonProvider());
        } else {
            // Default
            LOGGER.warn("Not a supported strategy for lucene indexes: '{}'", luceneIndexStrategy);
            ClassPathDirectory.setProvider(new ClassPathDirectory.BasicProvider());
        }
    }

    /**
     * Similarly to {@link #build(List, Analysis...)} but for a single column.
     *
     * @param column A column, may be null.
     * @param settings A varargs with {@link Analysis}. Duplicates are possible in varargs but will be considered only
     * once.
     * @return A ready to use {@link Analyzer}.
     */
    public Analyzer<Analyzers.Result> build(ColumnMetadata column, Analysis... settings) {
        if (column == null) {
            return build(Collections.emptyList(), settings);
        } else {
            return build(Collections.singletonList(column), settings);
        }
    }

    /**
     * Build a {@link Analyzer} to analyze records with columns (in <code>columns</code>). <code>settings</code> give
     * all the wanted analysis settings for the analyzer.
     *
     * @param columns A list of columns, may be null or empty.
     * @param settings A varargs with {@link Analysis}. Duplicates are possible in varargs but will be considered only
     * once.
     * @return A ready to use {@link Analyzer}.
     */
    public Analyzer<Analyzers.Result> build(List<ColumnMetadata> columns, Analysis... settings) {
        if (columns == null || columns.isEmpty()) {
            return Analyzers.with(NullAnalyzer.INSTANCE);
        }
        try {
            // Get all needed analysis
            final Set<Analysis> all = EnumSet.noneOf(Analysis.class);
            for (Analysis setting : settings) {
                if (setting != null) {
                    all.add(setting);
                    all.addAll(Arrays.asList(setting.dependencies));
                }
            }
            if (all.isEmpty()) {
                return Analyzers.with(NullAnalyzer.INSTANCE);
            }

            // Column types
            DataTypeEnum[] types = TypeUtils.convert(columns);
            // Semantic domains
            List<String> domainList = columns.stream() //
                    .map(c -> {
                        final SemanticCategoryEnum category = SemanticCategoryEnum.getCategoryById(c.getDomain().toUpperCase());
                        return category == null ? SemanticCategoryEnum.UNKNOWN.getId() : category.getId();
                    }) //
                    .collect(Collectors.toList());
            final String[] domains = domainList.toArray(new String[domainList.size()]);
            // Semantic indexes
            final URI ddPath = AnalyzerService.class.getResource("/luceneIdx/dictionary").toURI(); //$NON-NLS-1$
            final URI kwPath = AnalyzerService.class.getResource("/luceneIdx/keyword").toURI(); //$NON-NLS-1$
            final CategoryRecognizerBuilder builder = CategoryRecognizerBuilder.newBuilder() //
                    .ddPath(ddPath) //
                    .kwPath(kwPath) //
                    .lucene();

            // Build all analyzers
            List<Analyzer> analyzers = new ArrayList<>();
            for (Analysis setting : settings) {
                switch (setting) {
                case SEMANTIC:
                    analyzers.add(new SemanticAnalyzer(builder));
                    break;
                case HISTOGRAM:
                    analyzers.add(new StreamDateHistogramAnalyzer(columns, types, dateParser));
                    analyzers.add(new StreamNumberHistogramAnalyzer(types));
                    break;
                case QUALITY:
                    final DataTypeQualityAnalyzer dataTypeQualityAnalyzer = new DataTypeQualityAnalyzer(types);
                    columns.forEach(c -> dataTypeQualityAnalyzer.addCustomDateTimePattern(getMostUsedDatePattern(c)));
                    analyzers.add(new ValueQualityAnalyzer(dataTypeQualityAnalyzer, new SemanticQualityAnalyzer(builder, domains), // NOSONAR:
                                                                                                                                   // Up
                                                                                                                                   // to
                                                                                                                                   // caller
                                                                                                                                   // to
                                                                                                                                   // close
                                                                                                                                   // resources
                            true));
                    break;
                case CARDINALITY:
                    analyzers.add(new CardinalityAnalyzer());
                    break;
                case PATTERNS:
                    analyzers.add(buildPatternAnalyzer(columns));
                    break;
                case LENGTH:
                    analyzers.add(new TextLengthAnalyzer());
                    break;
                case QUANTILES:
                    analyzers.add(new QuantileAnalyzer(types));
                    break;
                case SUMMARY:
                    analyzers.add(new SummaryAnalyzer(types));
                    break;
                case TYPE:
                    final List<String> mostUsedDatePatterns = getMostUsedDatePatterns(columns);
                    analyzers.add(new DataTypeAnalyzer(mostUsedDatePatterns));
                    break;
                case FREQUENCY:
                    analyzers.add(new DataTypeFrequencyAnalyzer());
                    break;
                default:
                    throw new IllegalArgumentException("Missing support for '" + setting + "'.");
                }
            }

            // Merge all analyzers into one
            final Analyzer<Analyzers.Result> analyzer = Analyzers.with(analyzers.toArray(new Analyzer[analyzers.size()]));
            analyzer.init();
            return analyzer;
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    public Analyzer<Analyzers.Result> full(final List<ColumnMetadata> columns) {
        // Configure quality & semantic analysis (if column metadata information is present in stream).
        return build(columns, Analysis.QUALITY, Analysis.TYPE, Analysis.CARDINALITY, Analysis.FREQUENCY, Analysis.PATTERNS,
                Analysis.LENGTH, Analysis.SEMANTIC, Analysis.QUANTILES, Analysis.SUMMARY, Analysis.HISTOGRAM);
    }

    /**
     * <p>
     * Return analyzers for a "baseline" analysis of a dataset.
     * </p>
     * <ul>
     * <li>Value Quality (invalid, valid, empty)</li>
     * <li>Data Type</li>
     * <li>Cardinality (distinct & duplicates)</li>
     * <li>Frequency</li>
     * <li>Pattern frequency</li>
     * <li>Semantic</li>
     * </ul>
     *
     * @param columns the columns to analyze.
     * @return Return analyzers for a "baseline" analysis of a dataset.
     */
    public Analyzer<Analyzers.Result> baselineAnalysis(final List<ColumnMetadata> columns) {
        // Configure value quality analysis
        return build(columns, Analysis.QUALITY, Analysis.CARDINALITY, Analysis.TYPE, Analysis.FREQUENCY, Analysis.PATTERNS,
                Analysis.SEMANTIC);
    }

    /**
     * <p>
     * Return analyzers for an "advanced" analysis of a dataset.
     * </p>
     * <ul>
     * <li>Text length</li>
     * <li>Quantile</li>
     * <li>Summary (min, max, mean, variance)</li>
     * <li>Number Histogram</li>
     * <li>Date Histogram</li>
     * </ul>
     *
     * @param columns the columns to analyze.
     * @return Return analyzers for a "advanced" analysis of a dataset.
     */
    public Analyzer<Analyzers.Result> advancedAnalysis(final List<ColumnMetadata> columns) {
        return build(columns, Analysis.LENGTH, Analysis.QUANTILES, Analysis.SUMMARY, Analysis.HISTOGRAM);
    }

    public Analyzer<Analyzers.Result> qualityAnalysis(List<ColumnMetadata> columns) {
        return build(columns, Analysis.QUALITY, Analysis.SUMMARY, Analysis.SEMANTIC, Analysis.TYPE);
    }

    /**
     * <p>
     * Analyse the... Schema !
     * </p>
     * <ul>
     * <li>ValueQuality</li>
     * <li>Semantic</li>
     * <li>DataType</li>
     * </ul>
     *
     * @param columns the columns to analyze.
     * @return the analyzers to perform for the schema.
     */
    public Analyzer<Analyzers.Result> schemaAnalysis(List<ColumnMetadata> columns) {
        return build(columns, Analysis.QUALITY, Analysis.SEMANTIC, Analysis.TYPE);
    }

    /**
     * @see DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception {
        LOGGER.info("Clean up analyzers...");
        final Map<String, Analyzer<Analyzers.Result>> cache = ActionMetadataUtils.getAnalyzerCache();
        for (Map.Entry<String, Analyzer<Analyzers.Result>> entry : cache.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception e) {
                LOGGER.error("Unable to close analyzer for type '" + entry.getKey() + "'.", e);
            }
        }
        ClassPathDirectory.destroy();
        LOGGER.info("Clean up analyzers done.");
    }

    public enum Analysis {
                          /**
                           * Basic type discovery (integer, string...).
                           */
        TYPE(DataTypeOccurences.class),
                          /**
                           * Semantic type discovery (us_code, fr_phone...)
                           */
        SEMANTIC(SemanticType.class),
                          /**
                           * Histogram computation.
                           */
        HISTOGRAM(StreamDateHistogramStatistics.class),
                          /**
                           * Data quality (empty, invalid, valid...)
                           */
        QUALITY(ValueQualityStatistics.class),
                          /**
                           * Cardinality (distinct, duplicates)
                           */
        CARDINALITY(CardinalityStatistics.class),
                          /**
                           * String patterns
                           */
        PATTERNS(PatternFrequencyStatistics.class),
                          /**
                           * Text length (min / max length)
                           */
        LENGTH(TextLengthStatistics.class),
                          /**
                           * Quantiles
                           */
        QUANTILES(QuantileStatistics.class),
                          /**
                           * Min / Max / Variance for numeric values
                           */
        SUMMARY(SummaryStatistics.class),
                          /**
                           * Value to frequency map
                           */
        FREQUENCY(DataTypeFrequencyStatistics.class);

        private final Class resultClass;

        private final Analysis[] dependencies;

        Analysis(Class resultClass, Analysis... dependencies) {
            this.resultClass = resultClass;
            this.dependencies = dependencies;
        }

        public Class getResultClass() {
            return resultClass;
        }
    }

}
