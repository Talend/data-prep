package org.talend.dataprep.api.dataset.statistics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.Quality;
import org.talend.dataprep.api.dataset.location.SemanticDomain;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.api.type.TypeUtils;
import org.talend.dataquality.semantic.recognizer.CategoryFrequency;
import org.talend.dataquality.semantic.statistics.SemanticType;
import org.talend.dataquality.statistics.cardinality.CardinalityStatistics;
import org.talend.dataquality.statistics.frequency.DataFrequencyStatistics;
import org.talend.dataquality.statistics.frequency.PatternFrequencyStatistics;
import org.talend.dataquality.statistics.numeric.histogram.HistogramStatistics;
import org.talend.dataquality.statistics.numeric.quantile.QuantileStatistics;
import org.talend.dataquality.statistics.numeric.summary.SummaryStatistics;
import org.talend.dataquality.statistics.quality.ValueQualityStatistics;
import org.talend.dataquality.statistics.text.TextLengthStatistics;
import org.talend.datascience.common.inference.Analyzers;
import org.talend.datascience.common.inference.type.DataType;

@Component
public class StatisticsAdapter {

    /**
     * Defines the minimum threshold for a semantic type suggestion. Defaults to 40% if not defined.
     */
    @Value("#{'${semantic.threshold:40}'}")
    private int semanticThreshold;

    public void adapt(List<ColumnMetadata> columns, List<Analyzers.Result> results) {
        final Iterator<ColumnMetadata> columnIterator = columns.iterator();
        for (Analyzers.Result result : results) {
            final ColumnMetadata currentColumn = columnIterator.next();
            final boolean isNumeric = Type.NUMERIC.isAssignableFrom(Type.get(currentColumn.getType()));
            final boolean isString = Type.STRING.isAssignableFrom(Type.get(currentColumn.getType()));
            final Statistics statistics = currentColumn.getStatistics();
            // Data type
            if (result.exist(DataType.class) && !currentColumn.isTypeForced()) {
                final DataType dataType = result.get(DataType.class);
                final Map<DataType.Type, Long> frequencies = dataType.getTypeFrequencies();
                frequencies.remove(DataType.Type.EMPTY); // TDP-226: Don't take into account EMPTY values.
                // Look at type frequencies distribution (if not spread enough, fall back to STRING).
                StandardDeviation standardDeviation = new StandardDeviation();
                double[] values = new double[frequencies.size()];
                int i = 0;
                for (Long frequency : frequencies.values()) {
                    values[i++] = frequency;
                }
                final double stdDev = standardDeviation.evaluate(values);
                final Type type;
                if (stdDev < 1 && frequencies.size() > 1) {
                    type = Type.STRING;
                } else {
                    final DataType.Type suggestedType = dataType.getSuggestedType();
                    type = Type.get(suggestedType.name());
                }
                currentColumn.setType(type.getName());
            }
            // Value quality (empty / invalid / ...)
            if (result.exist(ValueQualityStatistics.class)) {
                final Quality quality = currentColumn.getQuality();
                final ValueQualityStatistics valueQualityStatistics = result.get(ValueQualityStatistics.class);
                // Set in column quality...
                quality.setEmpty((int) valueQualityStatistics.getEmptyCount());
                quality.setValid((int) valueQualityStatistics.getValidCount());
                quality.setInvalid((int) valueQualityStatistics.getInvalidCount());
                quality.setInvalidValues(valueQualityStatistics.getInvalidValues());
                // ... and statistics
                statistics.setCount(valueQualityStatistics.getCount());
                statistics.setEmpty(valueQualityStatistics.getEmptyCount());
                statistics.setInvalid(valueQualityStatistics.getInvalidCount());
                statistics.setValid(valueQualityStatistics.getValidCount());
            }
            // Semantic types
            if (result.exist(SemanticType.class) && !currentColumn.isDomainForced()) {
                final SemanticType semanticType = result.get(SemanticType.class);
                final Map<CategoryFrequency, Long> foundSemanticTypes = semanticType.getCategoryToCount();
                // TDP-471: Don't pick semantic type if lower than a threshold.
                final Optional<Map.Entry<CategoryFrequency, Long>> entry = foundSemanticTypes.entrySet().stream()
                        .filter(e -> !e.getKey().getCategoryName().isEmpty())
                        .max((o1, o2) -> o1.getValue().intValue() - o2.getValue().intValue());
                if (entry.isPresent()) {
                    // TODO (TDP-734) Take into account limit of the semantic analyzer.
                    final long percentage;
                    final long count = statistics.getCount();
                    if (count < 100 && count > 0) {
                        percentage = (entry.get().getValue() * 100) / count;
                    } else {
                        percentage = entry.get().getValue();
                    }
                    if (percentage > semanticThreshold) {
                        currentColumn.setDomain(semanticType.getSuggestedCategory());
                        currentColumn.setDomainLabel(TypeUtils.getDomainLabel(semanticType));
                        currentColumn.setDomainFrequency(entry.get().getValue());
                    } else {
                        // Ensure the domain is cleared if percentage is lower than threshold (earlier analysis - e.g.
                        // on the first 20 lines - may be over threshold, but full scan may decide otherwise.
                        currentColumn.setDomain(StringUtils.EMPTY);
                        currentColumn.setDomainLabel(StringUtils.EMPTY);
                        currentColumn.setDomainFrequency(0);
                    }
                }
                // Remembers all suggested semantic categories
                Map<CategoryFrequency, Long> altCategoryCounts = semanticType.getCategoryToCount();
                if (!altCategoryCounts.isEmpty()) {
                    List<SemanticDomain> semanticDomains = new ArrayList<>(altCategoryCounts.size());
                    for (Map.Entry<CategoryFrequency, Long> current : altCategoryCounts.entrySet()) {
                        // Find category display name
                        final String id = current.getKey().getCategoryId();
                        final String categoryDisplayName = TypeUtils.getDomainLabel(id);
                        semanticDomains.add(new SemanticDomain(id, categoryDisplayName, current.getKey().getFrequency()));
                    }
                    currentColumn.setSemanticDomains(semanticDomains);
                }
            }
            // Cardinality (distinct + duplicates)
            if (result.exist(CardinalityStatistics.class)) {
                final CardinalityStatistics cardinalityStatistics = result.get(CardinalityStatistics.class);
                statistics.setDistinctCount(cardinalityStatistics.getDistinctCount());
                statistics.setDuplicateCount(cardinalityStatistics.getDuplicateCount());
            }
            // Frequencies (data)
            if (result.exist(DataFrequencyStatistics.class)) {
                final DataFrequencyStatistics dataFrequencyStatistics = result.get(DataFrequencyStatistics.class);
                final Map<String, Long> topTerms = dataFrequencyStatistics.getTopK(15);
                if (topTerms != null) {
                    statistics.getDataFrequencies().clear();
                    topTerms.forEach((s, o) -> statistics.getDataFrequencies().add(new DataFrequency(s, o)));
                }
            }
            // Frequencies (pattern)
            if (result.exist(PatternFrequencyStatistics.class)) {
                final PatternFrequencyStatistics patternFrequencyStatistics = result.get(PatternFrequencyStatistics.class);
                final Map<String, Long> topTerms = patternFrequencyStatistics.getTopK(15);
                if (topTerms != null) {
                    statistics.getPatternFrequencies().clear();
                    topTerms.forEach((s, o) -> statistics.getPatternFrequencies().add(new PatternFrequency(s, o)));
                }
            }
            // Quantiles
            if (result.exist(QuantileStatistics.class)) {
                final QuantileStatistics quantileStatistics = result.get(QuantileStatistics.class);
                final Quantiles quantiles = statistics.getQuantiles();
                quantiles.setLowerQuantile(quantileStatistics.getLowerQuantile());
                quantiles.setMedian(quantileStatistics.getMedian());
                quantiles.setUpperQuantile(quantileStatistics.getUpperQuantile());
            }
            // Summary (min, max, mean, variance)
            if (result.exist(SummaryStatistics.class)) {
                final SummaryStatistics summaryStatistics = result.get(SummaryStatistics.class);
                statistics.setMax(summaryStatistics.getMax());
                statistics.setMin(summaryStatistics.getMin());
                statistics.setMean(summaryStatistics.getMean());
                statistics.setVariance(summaryStatistics.getVariance());
            }
            // Histogram
            if (isNumeric && result.exist(HistogramStatistics.class)) {
                final HistogramStatistics histogramStatistics = result.get(HistogramStatistics.class);
                // Build a decimal format based on most frequent pattern
                final String pattern = statistics.getPatternFrequencies().get(0).getPattern().replace('9', '#');
                final NumberFormat format;
                if (pattern.indexOf('a') < 0) {
                    format = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                } else {
                    format = DecimalFormat.getInstance(Locale.ENGLISH);
                }
                // Set histogram ranges
                statistics.getHistogram().clear();
                histogramStatistics.getHistogram().forEach((r, v) -> {
                    final HistogramRange range = new HistogramRange();
                    if (pattern.isEmpty()) {
                        range.getRange().setMax(r.getUpper());
                        range.getRange().setMin(r.getLower());
                    } else {
                        try {
                            range.getRange().setMax(new Double(format.format(r.getUpper())));
                            range.getRange().setMin(new Double(format.format(r.getLower())));
                        } catch (NumberFormatException e) {
                            // Fallback to unformatted numbers (unable to parse numbers).
                            range.getRange().setMax(r.getUpper());
                            range.getRange().setMin(r.getLower());
                        }
                    }
                    range.setOccurrences(v);
                    statistics.getHistogram().add(range);
                });
            }
            // Text length
            if (isString && result.exist(TextLengthStatistics.class)) {
                final TextLengthStatistics textLengthStatistics = result.get(TextLengthStatistics.class);
                final TextLengthSummary textLengthSummary = statistics.getTextLengthSummary();
                textLengthSummary.setAverageLength(textLengthStatistics.getAvgTextLength());
                textLengthSummary.setMinimalLength(textLengthStatistics.getMinTextLength());
                textLengthSummary.setMaximalLength(textLengthStatistics.getMaxTextLength());
            }
        }
    }
}
