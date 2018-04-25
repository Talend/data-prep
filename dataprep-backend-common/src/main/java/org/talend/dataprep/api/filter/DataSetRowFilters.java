// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.filter;

import static org.talend.daikon.number.BigDecimalParser.toBigDecimal;
import static org.talend.dataprep.api.dataset.row.FlagNames.TDP_INVALID;
import static org.talend.dataprep.util.NumericHelper.isBigDecimal;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.date.DateManipulator;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.date.DateParser;
import org.talend.dataprep.util.NumericHelper;

/**
 * Common data set row filters.
 */
class DataSetRowFilters {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetRowFilters.class);

    private static DateParser dateParser;

    private DataSetRowFilters() {
    }

    /**
     * Create a predicate that checks if the var is equals to a value.
     * <p>
     * It first tries String comparison, and if not 'true' uses number comparison.
     *
     * @param columnId The column id
     * @param value    The compared value
     * @return The eq predicate
     */
    static Predicate<DataSetRow> createEqualsPredicate(final String columnId, final String value) {
        return r -> r.values().entrySet().stream() //
                .filter(keepConcernedColumns(columnId)) //
                .map(e -> String.valueOf(e.getValue())) //
                .anyMatch(s -> StringUtils.equals(s, value) || isBigDecimal(s) //
                        && isBigDecimal(value) //
                        && toBigDecimal(s).compareTo(toBigDecimal(value)) == 0);
    }

    private static Predicate<Map.Entry<String, Object>> keepConcernedColumns(String columnId) {
        if ("*".equals(columnId)) {
            return e -> !TDP_INVALID.equals(e.getKey());
        } else {
            return e -> StringUtils.equals(columnId, e.getKey());
        }
    }

    /**
     * Create a predicate that checks if the var is greater than a value.
     *
     * @param columnId The column id
     * @param value    The compared value
     * @return The gt predicate
     */
    static Predicate<DataSetRow> createGreaterThanPredicate(final String columnId, final String value) {
        return r -> r.values().entrySet().stream() //
                .filter(keepConcernedColumns(columnId)) //
                .map(e -> String.valueOf(e.getValue())) //
                .anyMatch(s -> isBigDecimal(s) //
                        && isBigDecimal(value) //
                        && toBigDecimal(s).compareTo(toBigDecimal(value)) > 0);
    }

    /**
     * Create a predicate that checks if the var is lower than a value.
     *
     * @param columnId The column id
     * @param value    The compared value
     * @return The lt predicate
     */
    static Predicate<DataSetRow> createLowerThanPredicate(final String columnId, final String value) {
        return r -> r.values().entrySet().stream() //
                .filter(keepConcernedColumns(columnId)) //
                .map(e -> String.valueOf(e.getValue())) //
                .anyMatch(s -> isBigDecimal(s) //
                        && isBigDecimal(value) //
                        && toBigDecimal(s).compareTo(toBigDecimal(value)) < 0);
    }

    /**
     * Create a predicate that checks if the var is greater than or equal to a value.
     *
     * @param columnId The column id
     * @param value    The compared value
     * @return The gte predicate
     */
    static Predicate<DataSetRow> createGreaterOrEqualsPredicate(final String columnId, final String value) {
        return r -> r.values().entrySet().stream() //
                .filter(keepConcernedColumns(columnId)) //
                .map(e -> String.valueOf(e.getValue())) //
                .anyMatch(s -> isBigDecimal(s) //
                        && isBigDecimal(value) //
                        && toBigDecimal(s).compareTo(toBigDecimal(value)) >= 0);
    }

    /**
     * Create a predicate that checks if the var is lower than or equals to a value.
     *
     * @param columnId The column id
     * @param value    The compared value
     * @return The lte predicate
     */
    static Predicate<DataSetRow> createLowerOrEqualsPredicate(final String columnId, final String value) {
        return r -> r.values().entrySet().stream() //
                .filter(keepConcernedColumns(columnId)) //
                .map(e -> String.valueOf(e.getValue())) //
                .anyMatch(s -> isBigDecimal(s) //
                        && isBigDecimal(value) //
                        && toBigDecimal(s).compareTo(toBigDecimal(value)) <= 0);
    }

    /**
     * Create a predicate that checks if the var is in a supplied set of values.
     *
     * @param columnId The column id
     * @param values The value set
     * @return The in predicate
     */
    static Predicate<DataSetRow> createInPredicate(final String columnId, final List<String> values) {
        return r -> r
                .values()
                .entrySet()
                .stream() //
                .filter(keepConcernedColumns(columnId)) //
                .map(e -> String.valueOf(e.getValue())) //
                .anyMatch(values::contains);
    }

    /**
     * Create a predicate that checks if the var contains a value.
     *
     * @param columnId The column id
     * @param value    The contained value
     * @return The contains predicate
     */
    static Predicate<DataSetRow> createContainsPredicate(final String columnId, final String value) {
        return r -> r.values().entrySet().stream() //
                .filter(keepConcernedColumns(columnId)) //
                .map(e -> String.valueOf(e.getValue())) //
                .anyMatch(s -> StringUtils.containsIgnoreCase(s, value));
    }

    /**
     * Create a predicate that checks if the var complies to a pattern containing only 'A', 'a', '9' and special chars.
     * It corresponds to the former JSON "matches" filter and to the TQL "complies" filter.
     * It DOES NOT correspond to the TQL "matches" filter.
     *
     * @param columnId The column id
     * @param value    The pattern to comply to
     * @return The complies predicate
     */
    static Predicate<DataSetRow> createCompliesPredicate(final String columnId, final String value) {
        return r -> r.values().entrySet().stream() //
                .filter(keepConcernedColumns(columnId)) //
                .map(e -> String.valueOf(e.getValue())) //
                .anyMatch(s -> complies(s, value));
    }

    /**
     * Test a string value against a pattern.
     *
     * @param value   A string value. May be null.
     * @param pattern A pattern as returned in value analysis.
     * @return <code>true</code> if value complies, <code>false</code> otherwise.
     */
    private static boolean complies(String value, String pattern) {
        if (value == null && pattern == null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        // Character based patterns
        if (StringUtils.containsAny(pattern, new char[]{'A', 'a', '9'})) {
            if (value.length() != pattern.length()) {
                return false;
            }
            final char[] valueArray = value.toCharArray();
            final char[] patternArray = pattern.toCharArray();
            for (int i = 0; i < valueArray.length; i++) {
                if (patternArray[i] == 'A') {
                    if (!Character.isUpperCase(valueArray[i])) {
                        return false;
                    }
                } else if (patternArray[i] == 'a') {
                    if (!Character.isLowerCase(valueArray[i])) {
                        return false;
                    }
                } else if (patternArray[i] == '9') {
                    if (!Character.isDigit(valueArray[i])) {
                        return false;
                    }
                } else {
                    if (valueArray[i] != patternArray[i]) {
                        return false;
                    }
                }
            }
        } else {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            try {
                formatter.toFormat().parseObject(value);
            } catch (ParseException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a predicate that checks if the var is invalid.
     *
     * @param columnId The column id
     * @return The invalid value predicate
     */
    static Predicate<DataSetRow> createInvalidPredicate(final String columnId) {
        return r -> r
                .values()
                .entrySet()
                .stream() //
                .filter(keepConcernedColumns(columnId)) //
                .map(e -> e.getKey()) //
                .anyMatch(colId -> r.isInvalid(colId));
    }

    /**
     * Create a predicate that checks if the var is valid (not empty and not invalid).
     *
     * @param columnId The column id
     * @return The valid value predicate
     */
    static Predicate<DataSetRow> createValidPredicate(final String columnId) {
        return r -> r
                .values()
                .entrySet()
                .stream() //
                .filter(keepValidConcernedColumns(columnId, r)) //
                .map(e -> String.valueOf(e.getValue())) //
                .anyMatch(value -> StringUtils.isNotEmpty(value));
    }

    private static Predicate<Map.Entry<String, Object>> keepValidConcernedColumns(String columnId, DataSetRow row) {
        if ("*".equals(columnId)) {
            return e -> !row.isInvalid(e.getKey()) && !TDP_INVALID.equals(e.getKey());
        } else {
            return e -> StringUtils.equals(columnId, e.getKey()) && !row.isInvalid(e.getKey());
        }
    }

    /**
     * Create a predicate that checks if the var is empty.
     *
     * @param columnId The column id
     * @return The empty value predicate
     */
    static Predicate<DataSetRow> createEmptyPredicate(final String columnId) {
        return r -> r.values().entrySet().stream() //
                .filter(keepConcernedColumns(columnId)) //
                .map(e -> String.valueOf(e.getValue())) //
                .anyMatch(StringUtils::isEmpty);
    }

    /**
     * Create a predicate that checks if the value is within a range [min, max[.
     *
     * @param columnId    The column id
     * @param min         The minimum
     * @param max         The maximum
     * @param lowerOpen   <code>true</code> if min number is excluded from range.
     * @param upperOpen   <code>true</code> if max number is excluded from range.
     * @param rowMetadata The row metadata
     * @return The range predicate
     */
    static Predicate<DataSetRow> createRangePredicate(final String columnId, final String min, final String max,
                                                      boolean lowerOpen, boolean upperOpen, final RowMetadata rowMetadata) {
        return r -> r.values().entrySet().stream() //
                .filter(keepConcernedColumns(columnId)) //
                .anyMatch(e -> {
                    final String columnType = rowMetadata.getById(e.getKey()).getType();
                    Type parsedType = Type.get(columnType);
                    if (Type.DATE.isAssignableFrom(parsedType)) {
                        return createDateRangePredicate(e.getKey(), min, lowerOpen, max, upperOpen, rowMetadata).test(r);
                    } else {
                        // Assume range can be parsed as number (may happen if column is currently marked as string, but will
                        // contain some numbers).
                        return createNumberRangePredicate(e.getKey(), min, lowerOpen, max, upperOpen).test(r);
                    }
                });
    }

    /**
     * Create a predicate that checks if the date value is within a range [min, max[.
     *
     * @param columnId The column id
     * @param start    The start value
     * @param end      The end value
     * @param lowerOpen   <code>true</code> if start is excluded from range.
     * @param upperOpen   <code>true</code> if end is excluded from range.
     * @return The date range predicate
     */
    private static Predicate<DataSetRow> createDateRangePredicate(final String columnId, final String start, boolean lowerOpen, final String end,
                                                                  boolean upperOpen, final RowMetadata rowMetadata) {
        try {
            final long minTimestamp = Long.parseLong(start);
            final long maxTimestamp = Long.parseLong(end);

            final LocalDateTime minDate = DateManipulator.fromEpochMillisecondsWithSystemOffset(minTimestamp);
            final LocalDateTime maxDate = DateManipulator.fromEpochMillisecondsWithSystemOffset(maxTimestamp);

            return safeDate(r -> {
                final ColumnMetadata columnMetadata = rowMetadata.getById(columnId);
                final LocalDateTime columnValue = getDateParser().parse(r.get(columnId), columnMetadata);

                final boolean lowerBound;
                if (lowerOpen) {
                    lowerBound = minDate.compareTo(columnValue) != 0 && minDate.isBefore(columnValue);
                } else {
                    lowerBound = minDate.compareTo(columnValue) == 0 || minDate.isBefore(columnValue);
                }
                final boolean upperBound;
                if (upperOpen) {
                    upperBound = maxDate.compareTo(columnValue) != 0 && maxDate.isAfter(columnValue);
                } else {
                    upperBound = maxDate.compareTo(columnValue) == 0 || maxDate.isAfter(columnValue);
                }
                return lowerBound && upperBound;
            });
        } catch (Exception e) {
            LOGGER.debug("Unable to create date range predicate.", e);
            throw new IllegalArgumentException(
                    "Unsupported query, malformed date 'range' (expected timestamps in min and max properties).");
        }
    }

    private static Predicate<DataSetRow> safeDate(Predicate<DataSetRow> inner) {
        return r -> {
            try {
                return inner.test(r);
            } catch (DateTimeException e) { // thrown by DateParser
                LOGGER.debug("Unable to parse date.", e);
                return false;
            }
        };
    }

    private static synchronized DateParser getDateParser() {
        if (dateParser == null) {
            dateParser = new DateParser(Providers.get(AnalyzerService.class));
        }
        return dateParser;
    }

    /**
     * Create a predicate that checks if the number value is within a range [min, max[
     *
     * @param columnId The column id
     * @param min      The minimal value
     * @param max      The maximal value
     * @param lowerOpen   <code>true</code> if min number is excluded from range.
     * @param upperOpen   <code>true</code> if max number is excluded from range.
     * @return The number range predicate
     */
    private static Predicate<DataSetRow> createNumberRangePredicate(final String columnId, final String min, boolean lowerOpen, final String max, boolean upperOpen) {
        try {
            final BigDecimal low = toBigDecimal(min);
            final BigDecimal high = toBigDecimal(max);
            return row -> {
                final String value = row.get(columnId);
                if (!NumericHelper.isBigDecimal(value)) {
                    return false;
                }
                final BigDecimal cellValue = toBigDecimal(value);

                final boolean lowerBound;
                if (lowerOpen) {
                    lowerBound = cellValue.compareTo(low) > 0;
                } else {
                    lowerBound = cellValue.compareTo(low) >= 0;
                }
                final boolean upperBound;
                if (upperOpen) {
                    upperBound = cellValue.compareTo(high) < 0;
                } else {
                    upperBound = cellValue.compareTo(high) <= 0;
                }
                return lowerBound && upperBound;
            };
        } catch (Exception e) {
            LOGGER.debug("Unable to create number range predicate.", e);
            throw new IllegalArgumentException("Unsupported query, malformed 'range' (expected number min and max properties).");
        }
    }

    /**
     * Create a predicate that checks if the var matches the regex
     * It only corresponds correspond to the TQL "matches" filter, NOT the JSON one (which is corresponds to a "complies" one).
     *
     * @param columnId The column id
     * @param regex    The regex to comply to
     * @return The matches predicate
     */
    static Predicate<DataSetRow> createMatchesPredicate(final String columnId, final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        return r -> r.values().entrySet().stream() //
                .filter(keepConcernedColumns(columnId)) //
                .map(e -> String.valueOf(e.getValue())) //
                .anyMatch(s -> pattern.matcher(s).matches());
    }

}
