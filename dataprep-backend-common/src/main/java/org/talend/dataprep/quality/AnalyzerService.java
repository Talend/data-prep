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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.dataset.statistics.date.StreamDateHistogramAnalyzer;
import org.talend.dataprep.api.dataset.statistics.date.StreamDateHistogramStatistics;
import org.talend.dataprep.api.dataset.statistics.number.StreamNumberHistogramAnalyzer;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.api.type.TypeUtils;
import org.talend.dataprep.transformation.actions.date.DateParser;
import org.talend.dataprep.transformation.api.transformer.json.NullAnalyzer;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;
import org.talend.dataquality.common.inference.ValueQualityStatistics;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.index.ClassPathDirectory;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;
import org.talend.dataquality.semantic.statistics.SemanticAnalyzer;
import org.talend.dataquality.semantic.statistics.SemanticQualityAnalyzer;
import org.talend.dataquality.semantic.statistics.SemanticType;
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

    /** Where the data quality indexes are extracted (default to ${java.io.tmpdir}/org.talend.dataquality.semantic). */
    @Value("${dataquality.indexes.file.location:${java.io.tmpdir}/org.talend.dataquality.semantic}")
    private String dataqualityIndexesLocation;

    private Set<Analyzer> openedAnalyzers = new HashSet<>();

    private CategoryRecognizerBuilder builder;

    /**
     * Initialize the AnalyzerService.
     */
    @PostConstruct
    public void init() {

        LOGGER.info("DataQuality indexes location : '{}'", dataqualityIndexesLocation);
        ClassPathDirectory.setLocalIndexFolder(dataqualityIndexesLocation);

        // Configure DQ index creation strategy (one copy per use or one copy shared by all calls).
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
        // Semantic builder (a single instance to be shared among all analyzers for proper index file management).
        builder = CategoryRecognizerBuilder.newBuilder().lucene();
    }

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

        return new CompositePatternFrequencyAnalyzer(patternFrequencyAnalyzers, TypeUtils.convert(columns));
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

        // Build all analyzers
        List<Analyzer> analyzers = new ArrayList<>();
        for (Analysis setting : settings) {
            switch (setting) {
            case SEMANTIC:
                final SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder);
                semanticAnalyzer.setLimit(Integer.MAX_VALUE);
                analyzers.add(semanticAnalyzer);
                break;
            case HISTOGRAM:
                analyzers.add(new StreamDateHistogramAnalyzer(columns, types, dateParser));
                analyzers.add(new StreamNumberHistogramAnalyzer(types));
                break;
            case QUALITY:
                final DataTypeQualityAnalyzer dataTypeQualityAnalyzer = new DataTypeQualityAnalyzer(types);
                columns.forEach(c -> dataTypeQualityAnalyzer.addCustomDateTimePattern(getMostUsedDatePattern(c)));
                analyzers.add(new ValueQualityAnalyzer(dataTypeQualityAnalyzer, new SemanticQualityAnalyzer(builder, domains, false), true)); // NOSONAR
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
                boolean acceptQuantiles = false;
                for (DataTypeEnum type : types) {
                    if (type == DataTypeEnum.INTEGER || type == DataTypeEnum.DOUBLE) {
                        acceptQuantiles = true;
                        break;
                    }
                }
                if (acceptQuantiles) {
                    analyzers.add(new QuantileAnalyzer(types));
                }
                break;
            case SUMMARY:
                analyzers.add(new SummaryAnalyzer(types));
                break;
            case TYPE:
                boolean shouldUseTypeAnalysis = true;
                for (Analysis analysis : settings) {
                    if (analysis == Analysis.QUALITY) {
                        shouldUseTypeAnalysis = false;
                        break;
                    }
                }
                if (shouldUseTypeAnalysis) {
                    final List<String> mostUsedDatePatterns = getMostUsedDatePatterns(columns);
                    analyzers.add(new DataTypeAnalyzer(mostUsedDatePatterns));
                } else {
                    LOGGER.warn("Disabled {} analysis (conflicts with {}).", setting, Analysis.QUALITY);
                }
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
        if (LOGGER.isDebugEnabled()) {
            // Wrap analyzer for usage monitoring (to diagnose non-closed analyzer issues).
            return new ResourceMonitoredAnalyzer(analyzer);
        } else {
            return analyzer;
        }
    }

    public Analyzer<Analyzers.Result> full(final List<ColumnMetadata> columns) {
        // Configure quality & semantic analysis (if column metadata information is present in stream).
        return build(columns, Analysis.QUALITY, Analysis.CARDINALITY, Analysis.FREQUENCY, Analysis.PATTERNS, Analysis.LENGTH,
                Analysis.SEMANTIC, Analysis.QUANTILES, Analysis.SUMMARY, Analysis.HISTOGRAM);
    }

    public Analyzer<Analyzers.Result> qualityAnalysis(List<ColumnMetadata> columns) {
        return build(columns, Analysis.QUALITY, Analysis.SUMMARY, Analysis.SEMANTIC);
    }

    /**
     * <p>
     * Analyse the... Schema !
     * </p>
     * <ul>
     * <li>Semantic</li>
     * <li>DataType</li>
     * </ul>
     *
     * @param columns the columns to analyze.
     * @return the analyzers to perform for the schema.
     */
    public Analyzer<Analyzers.Result> schemaAnalysis(List<ColumnMetadata> columns) {
        return build(columns, Analysis.SEMANTIC, Analysis.TYPE);
    }

    /**
     * @see DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception {
        LOGGER.info("Clean up analyzers...");
        ClassPathDirectory.destroy();
        LOGGER.info("Clean up analyzers done.");
    }

    @Scheduled(fixedDelay = 60000)
    public void resourceMonitor() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Opened analyzers: " + openedAnalyzers.size());
            final Iterator<Analyzer> iterator = openedAnalyzers.iterator();
            for (int i = 0; i < openedAnalyzers.size(); i++) {
                LOGGER.debug("Analyzer #{}: {}", i, iterator.next());
            }
        }
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

    private class ResourceMonitoredAnalyzer implements Analyzer<Analyzers.Result> {

        private final Analyzer<Analyzers.Result> analyzer;

        private Exception caller;
        private long lastCall;

        private ResourceMonitoredAnalyzer(Analyzer<Analyzers.Result> analyzer) {
            caller = new RuntimeException(); // NOSONAR
            openedAnalyzers.add(this);
            this.analyzer = analyzer;
        }

        @Override
        public void init() {
            analyzer.init();
        }

        @Override
        public boolean analyze(String... strings) {
            lastCall = System.currentTimeMillis();
            return analyzer.analyze(strings);
        }

        @Override
        public void end() {
            analyzer.end();
        }

        @Override
        public List<Analyzers.Result> getResult() {
            return analyzer.getResult();
        }

        @Override
        public Analyzer<Analyzers.Result> merge(Analyzer<Analyzers.Result> analyzer) {
            return analyzer.merge(analyzer);
        }

        @Override
        public void close() throws Exception {
            analyzer.close();
            openedAnalyzers.remove(this);
        }

        @Override
        public String toString() {
            StringBuilder toStringBuilder = new StringBuilder();
            toStringBuilder //
                    .append(analyzer.toString()).append(' ') //
                    .append(" last used (").append(System.currentTimeMillis() - lastCall) //
                    .append(" ms ago) ");

            final StringWriter toStringCaller = new StringWriter();
            this.caller.printStackTrace(new PrintWriter(toStringCaller)); // NOSONAR (stacktrace printed in a String)
            toStringBuilder.append("caller: ").append(toStringCaller.toString());
            return toStringBuilder.toString();
        }
    }


}
