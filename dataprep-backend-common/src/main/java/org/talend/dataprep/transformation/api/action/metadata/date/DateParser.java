package org.talend.dataprep.transformation.api.action.metadata.date;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataquality.statistics.frequency.PatternFrequencyAnalyzer;
import org.talend.dataquality.statistics.frequency.PatternFrequencyStatistics;

/**
 * Component in charge of parsing dates.
 */
@Component
public class DateParser {

    /**
     * Returns the most frequent pattern. If few patterns are equally frequent, no guaranty of which one is returned.
     * 
     * @param column
     * @return the most frequent pattern or null if no pattern at all.
     */
    public DatePattern getMostFrequentPattern(ColumnMetadata column) {
        List<DatePattern> patterns = getPatterns(column.getStatistics().getPatternFrequencies());
        if (!patterns.isEmpty()) {
            return patterns.get(0);
        } else {
            return null;
        }
    }

    /**
     * Parse the date time out of the given value based on the column date pattern.
     *
     * At first uses the known date patterns from the column statistics. If it fails, the DQ library is called to try to
     * get the pattern.
     *
     * @param value the value to get the date time from.
     * @param column the column to get the date patterns from.
     * @return the parsed date time. For date only value, time is set to 00:00:00.
     * @throws DateTimeException if the date cannot be parsed.
     */
    public LocalDateTime parse(String value, ColumnMetadata column) {
        try {
            return parseDateFromPatterns(value, getPatterns(column.getStatistics().getPatternFrequencies()));
        } catch (DateTimeException e) {
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
    protected LocalDateTime guessAndParse(String value, ColumnMetadata column) {
        final DatePattern guessedPattern = guessPattern(value);
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
     * @return the wanted parsed date time. For date only value, time is set to 00:00:00.
     */
    protected DatePattern guessPattern(String value) {

        if (StringUtils.isEmpty(value)) {
            throw new DateTimeException("No pattern can be found out of '" + value + "'");
        }
        // call DQ on the given value
        final PatternFrequencyAnalyzer analyzer = new PatternFrequencyAnalyzer();
        analyzer.analyze(value);
        analyzer.end();

        // only one value --> only one result
        final PatternFrequencyStatistics patternFrequencyStatistics = analyzer.getResult().get(0);
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
    }

    /**
     * Parse the date from the given patterns.
     *
     * @param value the text to parse.
     * @param patterns the patterns to use.
     * @return the parsed date-time
     */
    protected LocalDateTime parseDateFromPatterns(String value, List<DatePattern> patterns) {

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
                // if it fails, let's try the LocalDate first
                try {
                    LocalDate temp = LocalDate.parse(value, formatter);
                    return temp.atStartOfDay();
                } catch (DateTimeException e2) {
                    // nothing to do here, just try the next formatter
                }
            }
        }
        throw new DateTimeException("'" + value + "' does not match any known pattern");
    }

    /**
     * Utility method to read and sort the given patterns.
     *
     * @param patternsFrequency the column to get the patterns from.
     */
    protected List<DatePattern> getPatterns(List<PatternFrequency> patternsFrequency) {
        // parse and checks the new date pattern
        // store the current pattern in the context
        final List<DatePattern> patterns = new ArrayList<>();
        for (PatternFrequency patternFrequency : patternsFrequency) {
            final String pattern = patternFrequency.getPattern();
            // skip empty patterns or existing ones
            if (StringUtils.isEmpty(pattern) || contains(pattern, patterns)) {
                continue;
            }
            patterns.add(new DatePattern(patternFrequency.getOccurrences(), pattern));
        }
        Collections.sort(patterns);
        return computeDateTimeFormatter(patterns);
    }

    /**
     * Return true if the given pattern is already held in the list of patterns.
     *
     * @param pattern the pattern to check.
     * @param patterns the list of patterns.
     * @return true if the given pattern is already held in the list of patterns.
     */
    private boolean contains(String pattern, List<DatePattern> patterns) {
        return patterns.stream().anyMatch(p -> StringUtils.equals(pattern, p.getPattern()));
    }

    /**
     * Giving a list of potential pattern as strings, validate them, and compute a list of DateTimeFormatter.
     *
     * @param patterns the list of potential patterns
     * @return a list that contains only valid and non null, non empty DateTimeFormatter
     */
    protected List<DatePattern> computeDateTimeFormatter(List<DatePattern> patterns) {

        DateTimeFormatterBuilder dtfb = new DateTimeFormatterBuilder();

        final Iterator<DatePattern> iterator = patterns.iterator();
        while (iterator.hasNext()) {
            final DatePattern nextPattern = iterator.next();
            String pattern = nextPattern.getPattern();
            // remove empty patterns
            if (StringUtils.isEmpty(pattern)) {
                iterator.remove();
                continue;
            }
            try {
                dtfb.appendPattern(pattern);
                nextPattern.setFormatter(DateTimeFormatter.ofPattern(pattern));
            } catch (IllegalArgumentException e) {
                // remove invalid patterns
                iterator.remove();
            }
        }

        return patterns;
    }
}
