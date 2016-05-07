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

package org.talend.dataprep.transformation.api.action.metadata.date;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.datepattern.DatePattern;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataquality.statistics.frequency.pattern.PatternFrequencyStatistics;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

/**
 * Component in charge of parsing dates.
 */
@Component
public class DateParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateParser.class);

    @Autowired
    private AnalyzerService analyzerService;

    /**
     * Returns the most frequent pattern. If few patterns are equally frequent, no guaranty of which one is returned.
     *
     * @param column the column to analyse.
     * @return the most frequent pattern or null if no pattern at all.
     */
    public DatePattern getMostFrequentPattern( ColumnMetadata column) {
        List<DatePattern> patterns = getPatterns(column.getStatistics().getPatternFrequencies());
        if (!patterns.isEmpty()) {
            return patterns.get(0);
        } else {
            return null;
        }
    }

    /**
     * Parse the date time out of the given value based on the column date pattern.
     * <p>
     * At first uses the known date patterns from the column statistics. If it fails, the DQ library is called to try to
     * get the pattern.
     *
     * @param value the value to get the date time from. Value can't be be empty or null/
     * @param column the column to get the date patterns from.
     * @return the parsed date time. For date only value, time is set to 00:00:00.
     * @throws DateTimeException if the date cannot be parsed, or if value is empty or null.
     */
    public LocalDateTime parse(String value, ColumnMetadata column) {
        try {
            return parseDateFromPatterns(value, getPatterns(column.getStatistics().getPatternFrequencies()));
        } catch (DateTimeException e) {
            LOGGER.debug("Unable to parse date '{}'", value, e);
            return guessAndParse(value, column);
        }
    }

    /**
     * Try to guess the pattern from the value. If the date is successfully parsed, the column statistics is updated
     * with the new pattern.
     *
     * @param value the date to parse.
     * @param column the column.
     * @return the parsed date.
     * @throws DateTimeException if the date cannot be parsed.
     */
    LocalDateTime guessAndParse(String value, ColumnMetadata column) {
        final DatePattern guessedPattern = guessPattern(value, column);
        LocalDateTime result = parseDateFromPatterns(value, Collections.singletonList(guessedPattern));

        // update the column statistics to prevent future DQ calls
        final List<PatternFrequency> patternFrequencies = column.getStatistics().getPatternFrequencies();
        patternFrequencies.add(new PatternFrequency(guessedPattern.getPattern(), guessedPattern.getOccurrences()));

        return result;
    }

    /**
     * Guess the pattern from the given value.
     *
     * @param value the value to get the date time from.
     * @param column the column metadata
     * @return the wanted parsed date time. For date only value, time is set to 00:00:00.
     */
    DatePattern guessPattern(String value, ColumnMetadata column) {
        if (StringUtils.isEmpty(value)) {
            throw new DateTimeException("No pattern can be found out of '" + value + "'");
        }
        // call DQ on the given value
        try (Analyzer<Analyzers.Result> analyzer = analyzerService.build(column, AnalyzerService.Analysis.PATTERNS)) {
            analyzer.analyze(value);
            analyzer.end();

            // only one value --> only one result
            final Analyzers.Result result = analyzer.getResult().get(0);
            if (result.exist(PatternFrequencyStatistics.class)) {
                final PatternFrequencyStatistics patternFrequencyStatistics = result.get(PatternFrequencyStatistics.class);
                final Map<String, Long> topTerms = patternFrequencyStatistics.getTopK(1);
                List<PatternFrequency> patterns = new ArrayList<>(1);
                topTerms.forEach((s, o) -> patterns.add(new PatternFrequency(s, o)));

                // get & check the results
                final List<DatePattern> results = getPatterns(patterns);
                if (results.isEmpty()) {
                    throw new DateTimeException("DQ did not find any pattern for '" + value + "'");
                }

                // as Christopher L. said : "there can be only one" :-)
                return getPatterns(patterns).get(0);
            } else {
                throw new DateTimeException("DQ did not find any pattern for '" + value + "'");
            }
        } catch (Exception e) {
            throw new DateTimeException("Unable to close analyzer after analyzing value '" + value + "'", e);
        }
    }

    /**
     * Parse the date from the given patterns.
     *
     * @param value the text to parse.
     * @param patterns the patterns to use.
     * @return the parsed date-time
     */
    LocalDateTime parseDateFromPatterns(String value, List<DatePattern> patterns) {

        // take care of the null value
        if (value == null) {
            throw new DateTimeException("cannot parse null");
        }

        for (DatePattern pattern : patterns) {
            final DateTimeFormatter formatter = pattern.getFormatter();

            // first try to parse directly as LocalDateTime
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeException e) {
                LOGGER.debug("Unable to parse date '{}' using LocalDateTime.", value, e);
                // if it fails, let's try the LocalDate first
                try {
                    LocalDate temp = LocalDate.parse(value, formatter);
                    return temp.atStartOfDay();
                } catch (DateTimeException e2) {
                    LOGGER.debug("Unable to parse date '{}' using LocalDate.", value, e2);
                    // nothing to do here, just try the next formatter
                }
            }
        }
        throw new DateTimeException("'" + value + "' does not match any known pattern");
    }

    /**
     * Utility method to read/parse/create DateFormatter and sort the given patterns.
     *
     * @param patternsFrequency the column to get the patterns from.
     */
    List<DatePattern> getPatterns(List<PatternFrequency> patternsFrequency) {
        final Set<String> distinctPatterns = new HashSet<>(patternsFrequency.size());

        return patternsFrequency.stream().filter(patternFreqItem -> isNotEmpty(patternFreqItem.getPattern()))
                .filter(patternFreqItem -> distinctPatterns.add(patternFreqItem.getPattern())) // use Set<> to detect if
                                                                                               // pattern is a duplicate
                .map(patternFreqItem -> {
                    try {
                        return new DatePattern(patternFreqItem.getPattern(), patternFreqItem.getOccurrences());
                    } catch (final IllegalArgumentException e) {
                        // thrown when pattern is not a valid date pattern
                        LOGGER.debug("Unable to parse pattern '{}'", patternFreqItem.getPattern(), e);
                        return null;
                    }
                }).filter(datePattern -> datePattern != null) // remove non valid date patterns
                .sorted().collect(toList());
    }
}
