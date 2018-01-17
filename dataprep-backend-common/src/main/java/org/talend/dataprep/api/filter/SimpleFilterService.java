// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.talend.dataprep.util.NumericHelper.isBigDecimal;

import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.date.DateManipulator;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.date.DateParser;
import org.talend.dataprep.util.NumericHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFilterService.class);

    private DateParser dateParser;

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

    @Override
    public Predicate<DataSetRow> build(String filterAsString, RowMetadata rowMetadata) {
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
                return buildFilter(root, rowMetadata);
            }
        } catch (Exception e) {
            throw new TalendRuntimeException(BaseErrorCodes.UNABLE_TO_PARSE_FILTER, e);
        }
    }

    private Predicate<DataSetRow> buildFilter(JsonNode currentNode, RowMetadata rowMetadata) {
        final Iterator<JsonNode> children = currentNode.elements();
        final JsonNode operationContent = children.next();
        final String columnId = operationContent.has("field") ? operationContent.get("field").asText() : null;
        final String value = operationContent.has("value") ? operationContent.get("value").asText() : null;

        final Iterator<String> propertiesIterator = currentNode.fieldNames();
        if (!propertiesIterator.hasNext()) {
            throw new UnsupportedOperationException("Unsupported query, empty filter definition: " + currentNode.toString());
        }

        final String operation = propertiesIterator.next();
        if (columnId == null && allowFullFilter(operation)) {
            // Full data set filter (no column)
            final List<ColumnMetadata> columns = rowMetadata.getColumns();
            Predicate<DataSetRow> predicate;
            if (!columns.isEmpty()) {
                predicate = buildOperationFilter(currentNode, rowMetadata, columns.get(0).getId(), operation, value);
                for (int i = 1; i < columns.size(); i++) {
                    predicate = predicate
                            .or(buildOperationFilter(currentNode, rowMetadata, columns.get(i).getId(), operation, value));
                }
            } else {
                // We can't return a null filter, default to the neutral value
                predicate = dsr -> true;
            }
            return predicate;
        } else {
            return buildOperationFilter(currentNode, rowMetadata, columnId, operation, value);
        }
    }

    private static boolean allowFullFilter(String operation) {
        switch (operation) {
        case EQ:
        case GT:
        case LT:
        case GTE:
        case LTE:
        case CONTAINS:
        case MATCHES:
        case INVALID:
        case VALID:
        case EMPTY:
        case RANGE:
            return true;
        case AND:
        case OR:
        case NOT:
        default:
            return false;
        }
    }

    private Predicate<DataSetRow> buildOperationFilter(JsonNode currentNode, //
            RowMetadata rowMetadata, //
            String columnId, //
            String operation, //
            String value) {
        switch (operation) {
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
            return createRangePredicate(columnId, currentNode.elements().next(), rowMetadata);
        case AND:
            return createAndPredicate(currentNode.elements().next(), rowMetadata);
        case OR:
            return createOrPredicate(currentNode.elements().next(), rowMetadata);
        case NOT:
            return createNotPredicate(currentNode.elements().next(), rowMetadata);
        default:
            throw new UnsupportedOperationException(
                    "Unsupported query, unknown filter '" + operation + "': " + currentNode.toString());
        }
    }

    /**
     * Create a predicate that do a logical AND between 2 filters
     *
     * @param nodeContent The node content
     * @param rowMetadata Row metadata to used to obtain information (valid/invalid, types...)
     * @return the AND predicate
     */
    private Predicate<DataSetRow> createAndPredicate(final JsonNode nodeContent, RowMetadata rowMetadata) {
        checkValidMultiPredicate(nodeContent);
        final Predicate<DataSetRow> leftFilter = buildFilter(nodeContent.get(0), rowMetadata);
        final Predicate<DataSetRow> rightFilter = buildFilter(nodeContent.get(1), rowMetadata);
        return leftFilter.and(rightFilter);
    }

    /**
     * Create a predicate that do a logical OR between 2 filters
     *
     * @param nodeContent The node content
     * @param rowMetadata Row metadata to used to obtain information (valid/invalid, types...)
     * @return the OR predicate
     */
    private Predicate<DataSetRow> createOrPredicate(final JsonNode nodeContent, RowMetadata rowMetadata) {
        checkValidMultiPredicate(nodeContent);
        final Predicate<DataSetRow> leftFilter = buildFilter(nodeContent.get(0), rowMetadata);
        final Predicate<DataSetRow> rightFilter = buildFilter(nodeContent.get(1), rowMetadata);
        return leftFilter.or(rightFilter);
    }

    /**
     * Create a predicate that negates a filter
     *
     * @param nodeContent The node content
     * @param rowMetadata Row metadata to used to obtain information (valid/invalid, types...)
     * @return The NOT predicate
     */
    private Predicate<DataSetRow> createNotPredicate(final JsonNode nodeContent, RowMetadata rowMetadata) {
        if (!nodeContent.isObject()) {
            throw new IllegalArgumentException("Unsupported query, malformed 'not' (expected 1 object child).");
        }
        if (nodeContent.size() == 0) {
            throw new IllegalArgumentException("Unsupported query, malformed 'not' (object child is empty).");
        }
        return buildFilter(nodeContent, rowMetadata).negate();
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
        return r -> {
            if (StringUtils.equals(r.get(columnId), value)) {
                return true;
            } else {
                return isBigDecimal(r.get(columnId)) //
                        && isBigDecimal(value) //
                        && NumberUtils.compare(toBigDecimal(r.get(columnId)), toBigDecimal(value)) == 0;
            }
        };
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
        return r -> isBigDecimal(r.get(columnId)) //
                && isBigDecimal(value) //
                && toBigDecimal(r.get(columnId)) > toBigDecimal(value);
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
        return r -> isBigDecimal(r.get(columnId)) //
                && isBigDecimal(value) //
                && toBigDecimal(r.get(columnId)) < toBigDecimal(value);
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
        return r -> isBigDecimal(r.get(columnId)) //
                && isBigDecimal(value) //
                && toBigDecimal(r.get(columnId)) >= toBigDecimal(value);
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
        return r -> isBigDecimal(r.get(columnId)) //
                && isBigDecimal(value) //
                && toBigDecimal(r.get(columnId)) <= toBigDecimal(value);
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
        return r -> r.isInvalid(columnId);
    }

    /**
     * Create a predicate that checks if the value is value (not empty and not invalid)
     *
     * @param columnId The column id
     * @return The valid value predicate
     */
    private Predicate<DataSetRow> createValidPredicate(final String columnId) {
        return r -> !r.isInvalid(columnId) && !isEmpty(r.get(columnId));
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
    private Predicate<DataSetRow> createRangePredicate(final String columnId, final JsonNode nodeContent,
            final RowMetadata rowMetadata) {
        final String start = nodeContent.get("start").asText();
        final String end = nodeContent.get("end").asText();
        return r -> {
            final String columnType = rowMetadata.getById(columnId).getType();
            Type parsedType = Type.get(columnType);
            if (Type.DATE.isAssignableFrom(parsedType)) {
                return createDateRangePredicate(columnId, start, end, rowMetadata).test(r);
            } else {
                // Assume range can be parsed as number (may happen if column is currently marked as string, but will
                // contain some numbers).
                return createNumberRangePredicate(columnId, start, end).test(r);
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
    private Predicate<DataSetRow> createDateRangePredicate(final String columnId, final String start, final String end,
            final RowMetadata rowMetadata) {
        try {
            final long minTimestamp = Long.parseLong(start);
            final long maxTimestamp = Long.parseLong(end);

            final LocalDateTime minDate = DateManipulator.fromEpochMillisecondsWithSystemOffset(minTimestamp);
            final LocalDateTime maxDate = DateManipulator.fromEpochMillisecondsWithSystemOffset(maxTimestamp);

            return safeDate(r -> {
                final ColumnMetadata columnMetadata = rowMetadata.getById(columnId);
                final LocalDateTime columnValue = getDateParser().parse(r.get(columnId), columnMetadata);
                return minDate.compareTo(columnValue) == 0 || (minDate.isBefore(columnValue) && maxDate.isAfter(columnValue));
            });
        } catch (Exception e) {
            LOGGER.debug("Unable to create date range predicate.", e);
            throw new IllegalArgumentException(
                    "Unsupported query, malformed date 'range' (expected timestamps in min and max properties).");
        }
    }

    private synchronized DateParser getDateParser() {
        if (dateParser == null) {
            dateParser = new DateParser(Providers.get(AnalyzerService.class));
        }
        return dateParser;
    }

    // Intentionally left with package modifier since only used by unit test (in same package)
    void setDateParser(final DateParser dateParser) {
        this.dateParser = dateParser;
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
            return r -> {
                final String value = r.get(columnId);
                if (NumericHelper.isBigDecimal(value)) {
                    final double columnValue = toBigDecimal(value);
                    return NumberUtils.compare(columnValue, min) == 0 || (columnValue > min && columnValue < max);
                } else {
                    return false;
                }
            };
        } catch (Exception e) {
            LOGGER.debug("Unable to create number range predicate.", e);
            throw new IllegalArgumentException("Unsupported query, malformed 'range' (expected number min and max properties).");
        }
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

    /**
     * Simple wrapper to call BigDecimalParser to simplify code above.
     */
    private double toBigDecimal(String value) {
        return BigDecimalParser.toBigDecimal(value).doubleValue();
    }
}
