//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.filter;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.date.DateManipulator;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.number.BigDecimalParser;
import org.talend.dataprep.transformation.api.action.metadata.date.DateParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SimpleFilterService implements FilterService {

    private static final String EQ = "eq";

    private static final String GT = "gt";

    private static final String LT = "lt";

    private static final String GTE = "gte";

    private static final String LTE = "lte";

    private static final String CONTAINS = "contains";

    private static final String MATCHES = "matches";

    private static final String INVALID = "invalid";

    private static final String VALID = "valid";

    private static final String EMPTY = "empty";

    private static final String RANGE = "range";

    private static final String AND = "and";

    private static final String OR = "or";

    private static final String NOT = "not";

    final private DateManipulator dateManipulator = new DateManipulator();

    @Autowired
    private DateParser dateParser;

    @Override
    public Predicate<DataSetRow> build(String filterAsString) {
        if (isEmpty(filterAsString)) {
            return r -> true;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.reader().readTree(filterAsString);
            final Iterator<JsonNode> elements = root.elements();
            if (!elements.hasNext()) {
                throw new IllegalArgumentException("Malformed filter: " + filterAsString);
            } else {
                return buildFilter(root);
            }
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_FILTER, e);
        }
    }

    private Predicate<DataSetRow> buildFilter(JsonNode currentNode) {
        final Iterator<JsonNode> children = currentNode.elements();
        final JsonNode currentNodeContent = children.next();
        final String columnId = currentNodeContent.has("field") ? currentNodeContent.get("field").asText() : null;
        final String value = currentNodeContent.has("value") ? currentNodeContent.get("value").asText() : null;

        final Iterator<String> propertiesIterator = currentNode.fieldNames();
        if (!propertiesIterator.hasNext()) {
            throw new UnsupportedOperationException("Unsupported query, empty filter definition: " + currentNode.toString());
        }

        final String currentNodeName = propertiesIterator.next();
        switch (currentNodeName) {
        case EQ:
            return createEqualsPredicate(currentNode, columnId, value);
        case GT:
            return createGreaterThanPredicate(currentNode, columnId, value);
        case LT:
            return createLowerThanPredicate(currentNode, columnId, value);
        case GTE:
            return createGreaterOrEqualsPredicate(currentNode, columnId, value);
        case LTE:
            return createLowerOrEqualsPredicate(currentNode, columnId, value);
        case CONTAINS:
            return createContainsPredicate(currentNode, columnId, value);
        case MATCHES:
            return createMatchesPredicate(currentNode, columnId, value);
        case INVALID:
            return createInvalidPredicate(columnId);
        case VALID:
            return createValidPredicate(columnId);
        case EMPTY:
            return createEmptyPredicate(columnId);
        case RANGE:
            return createRangePredicate(columnId, currentNodeContent);
        case AND:
            return createAndPredicate(currentNodeContent);
        case OR:
            return createOrPredicate(currentNodeContent);
        case NOT:
            return createNotPredicate(currentNodeContent);
        default:
            throw new UnsupportedOperationException(
                    "Unsupported query, unknown filter '" + currentNodeName + "': " + currentNode.toString());
        }
    }

    /**
     * Create a predicate that do a logical AND between 2 filters
     *
     * @param nodeContent The node content
     * @return the AND predicate
     */
    private Predicate<DataSetRow> createAndPredicate(final JsonNode nodeContent) {
        checkValidMultiPredicate(nodeContent);
        final Predicate<DataSetRow> leftFilter = buildFilter(nodeContent.get(0));
        final Predicate<DataSetRow> rightFilter = buildFilter(nodeContent.get(1));
        final Predicate<DataSetRow> andFilter = leftFilter.and(rightFilter);
        return andFilter::test;
    }

    /**
     * Create a predicate that do a logical OR between 2 filters
     *
     * @param nodeContent The node content
     * @return the OR predicate
     */
    private Predicate<DataSetRow> createOrPredicate(final JsonNode nodeContent) {
        checkValidMultiPredicate(nodeContent);
        final Predicate<DataSetRow> leftFilter = buildFilter(nodeContent.get(0));
        final Predicate<DataSetRow> rightFilter = buildFilter(nodeContent.get(1));
        final Predicate<DataSetRow> orFilter = leftFilter.or(rightFilter);
        return orFilter::test;
    }

    /**
     * Create a predicate that negates a filter
     *
     * @param nodeContent The node content
     * @return The NOT predicate
     */
    private Predicate<DataSetRow> createNotPredicate(final JsonNode nodeContent) {
        if (!nodeContent.isObject()) {
            throw new IllegalArgumentException("Unsupported query, malformed 'not' (expected 1 object child).");
        }
        if (nodeContent.size() == 0) {
            throw new IllegalArgumentException("Unsupported query, malformed 'not' (object child is empty).");
        }
        return buildFilter(nodeContent).negate();
    }

    /**
     * Create a predicate that checks if the var is equals to a value.
     *
     * It first tries String comparison, and if not 'true' uses number comparison.
     *
     * @param node The filter node
     * @param columnId The column id
     * @param value The compare value
     * @return The eq predicate
     */
    private Predicate<DataSetRow> createEqualsPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return safeNumber(
                r -> StringUtils.equals(r.get(columnId), value) || toBigDecimal(r.get(columnId)) == toBigDecimal(value));
    }

    /**
     * Create a predicate that checks if the var is greater than a value
     *
     * @param node The filter node
     * @param columnId The column id
     * @param value The compare value
     * @return The gt predicate
     */
    private Predicate<DataSetRow> createGreaterThanPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return safeNumber(r -> toBigDecimal(r.get(columnId)) > toBigDecimal(value));
    }

    /**
     * Create a predicate that checks if the var is lower than a value
     *
     * @param node The filter node
     * @param columnId The column id
     * @param value The compare value
     * @return The lt predicate
     */
    private Predicate<DataSetRow> createLowerThanPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return safeNumber(r -> toBigDecimal(r.get(columnId)) < toBigDecimal(value));
    }

    /**
     * Create a predicate that checks if the var is greater than or equals to a value
     *
     * @param node The filter node
     * @param columnId The column id
     * @param value The compare value
     * @return The gte predicate
     */
    private Predicate<DataSetRow> createGreaterOrEqualsPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return safeNumber(r -> toBigDecimal(r.get(columnId)) >= toBigDecimal(value));
    }

    /**
     * Create a predicate that checks if the var is lower than or equals to a value
     *
     * @param node The filter node
     * @param columnId The column id
     * @param value The compare value
     * @return The lte predicate
     */
    private Predicate<DataSetRow> createLowerOrEqualsPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return safeNumber(r -> toBigDecimal(r.get(columnId)) <= toBigDecimal(value));
    }

    /**
     * Create a predicate that checks if the var contains a value
     *
     * @param node The filter node
     * @param columnId The column id
     * @param value The contained value
     * @return The contains predicate
     */
    private Predicate<DataSetRow> createContainsPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return r -> StringUtils.containsIgnoreCase(r.get(columnId), value);
    }

    /**
     * Create a predicate that checks if the var match a value
     *
     * @param node The filter node
     * @param columnId The column id
     * @param value The value to match
     * @return The match predicate
     */
    private Predicate<DataSetRow> createMatchesPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return r -> matches(r.get(columnId), value);
    }

    /**
     * Create a predicate that checks if the value is invalid
     *
     * @param columnId The column id
     * @return The invalid value predicate
     */
    private Predicate<DataSetRow> createInvalidPredicate(final String columnId) {
        return r -> {
            final Set<String> invalidValues = getInvalidValues(r, columnId);
            final String columnValue = r.get(columnId);
            return invalidValues.contains(columnValue);
        };
    }

    /**
     * Create a predicate that checks if the value is value (not empty and not invalid)
     *
     * @param columnId The column id
     * @return The valid value predicate
     */
    private Predicate<DataSetRow> createValidPredicate(final String columnId) {
        return r -> {
            final Set<String> invalidValues = getInvalidValues(r, columnId);
            final String columnValue = r.get(columnId);
            return isNotEmpty(columnValue) && !invalidValues.contains(columnValue);
        };
    }

    /**
     * Create a predicate that checks if the value is empty
     *
     * @param columnId The column id
     * @return The empty value predicate
     */
    private Predicate<DataSetRow> createEmptyPredicate(final String columnId) {
        return r -> isEmpty(r.get(columnId));
    }

    /**
     * Create a predicate that checks if the value is within a range [min, max[
     *
     * @param columnId The column id
     * @param nodeContent The node content that contains min/max values
     * @return The range predicate
     */
    private Predicate<DataSetRow> createRangePredicate(final String columnId, final JsonNode nodeContent) {
        final String start = nodeContent.get("start").asText();
        final String end = nodeContent.get("end").asText();
        return r -> {
            final String columnType = r.getRowMetadata().getById(columnId).getType();
            Type parsedType = Type.get(columnType);
            if (Type.DATE.isAssignableFrom(parsedType)) {
                return createDateRangePredicate(columnId, start, end).test(r);
            } else if (Type.NUMERIC.isAssignableFrom(parsedType)) {
                return createNumberRangePredicate(columnId, start, end).test(r);
            } else {
                throw new IllegalArgumentException("Type '" + parsedType.getName() + "' is not a valid type for range.");
            }
        };
    }

    /**
     * Create a predicate that checks if the date value is within a range [min, max[
     *
     * @param columnId The column id
     * @param start The start value
     * @param end The end value
     * @return The date range predicate
     */
    private Predicate<DataSetRow> createDateRangePredicate(final String columnId, final String start, final String end) {
        try {
            final long minTimestamp = Long.parseLong(start);
            final long maxTimestamp = Long.parseLong(end);

            final LocalDateTime minDate = dateManipulator.fromEpochMillisecondsWithSystemOffset(minTimestamp);
            final LocalDateTime maxDate = dateManipulator.fromEpochMillisecondsWithSystemOffset(maxTimestamp);

            return safeDate(r -> {
                final ColumnMetadata columnMetadata = r.getRowMetadata().getById(columnId);
                final LocalDateTime columnValue = dateParser.parse(r.get(columnId), columnMetadata);
                return minDate.compareTo(columnValue) == 0 || (minDate.isBefore(columnValue) && maxDate.isAfter(columnValue));
            });
        } catch (final Exception e) {
            throw new IllegalArgumentException(
                    "Unsupported query, malformed date 'range' (expected timestamps in min and max properties).");
        }
    }

    /**
     * Create a predicate that checks if the number value is within a range [min, max[
     *
     * @param columnId The column id
     * @param start The start value
     * @param end The end value
     * @return The number range predicate
     */
    private Predicate<DataSetRow> createNumberRangePredicate(final String columnId, final String start, final String end) {
        try {
            final double min = toBigDecimal(start);
            final double max = toBigDecimal(end);
            return safeNumber(r -> {
                final double columnValue = toBigDecimal(r.get(columnId));
                return NumberUtils.compare(columnValue, min) == 0 || (columnValue > min && columnValue < max);
            });
        } catch (final Exception e) {
            throw new IllegalArgumentException("Unsupported query, malformed 'range' (expected number min and max properties).");
        }
    }

    /**
     * Get the invalid value collection on a specific column
     *
     * @param row The dataset row
     * @param columnId The column id
     * @return The invalid values for the specified column
     */
    private Set<String> getInvalidValues(final DataSetRow row, final String columnId) {
        final ColumnMetadata column = row.getRowMetadata().getById(columnId);
        if (column != null) {
            return column.getQuality().getInvalidValues();
        }
        return Collections.<String> emptySet();
    }

    /**
     * check if the node has a non null value
     *
     * @param node The node to test
     * @param value The node 'value' property
     * @throws IllegalArgumentException If the node has not a 'value' property
     */
    private void checkValidValue(final JsonNode node, final String value) {
        if (value == null) {
            throw new UnsupportedOperationException("Unsupported query, the filter needs a value : " + node.toString());
        }
    }

    /**
     * Check if the node has exactly 2 children. Used to safe check binary operator (and, or)
     *
     * @param nodeContent The node content
     * @throws IllegalArgumentException If the node has not exactly 2 children
     */
    private void checkValidMultiPredicate(final JsonNode nodeContent) {
        if (nodeContent.size() != 2) {
            throw new IllegalArgumentException("Unsupported query, malformed 'and' (expected 2 children).");
        }
    }

    /**
     * Test a string value against a pattern returned during value analysis.
     *
     * @param value A string value. May be null.
     * @param pattern A pattern as returned in value analysis.
     * @return <code>true</code> if value matches, <code>false</code> otherwise.
     */
    private boolean matches(String value, String pattern) {
        if (value == null && pattern == null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        // Character based patterns
        if (StringUtils.containsAny(pattern, new char[] { 'A', 'a', '9' })) {
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

    private static Predicate<DataSetRow> safeNumber(Predicate<DataSetRow> inner) {
        return r -> {
            try {
                return inner.test(r);
            } catch (NumberFormatException e) {
                // BigDecimalParser.toBigDecimal throws NumberFormatException when parsing null or NaN strings.
                return false;
            }
        };
    }

    private static Predicate<DataSetRow> safeDate(Predicate<DataSetRow> inner) {
        return r -> {
            try {
                return inner.test(r);
            } catch (DateTimeException e) { // thrown by DateParser
                return false;
            }
        };
    }

    /**
     * Simple wrapper to call BigDecimalParser to simplify code above.
     */
    private double toBigDecimal(String value) {
        return BigDecimalParser.toBigDecimal(value).doubleValue();
    }

    // Intentionally left with package modifier since only used by unit test (in same package)
    void setDateParser(final DateParser dateParser) {
        this.dateParser = dateParser;
    }
}
