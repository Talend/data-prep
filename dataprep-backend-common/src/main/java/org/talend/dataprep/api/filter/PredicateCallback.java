package org.talend.dataprep.api.filter;

import java.util.Optional;
import java.util.function.Predicate;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * An implementation of {@link JSONFilterCallback} that builds a {@link Predicate} for {@link DataSetRow}.
 *
 * @see SimpleFilterService
 */
public class PredicateCallback implements JSONFilterCallback<Predicate<DataSetRow>> {

    @Override
    public Predicate<DataSetRow> and(Predicate<DataSetRow> left, Predicate<DataSetRow> right) {
        return left.and(right);
    }

    @Override
    public Predicate<DataSetRow> not(Predicate<DataSetRow> expression) {
        return expression.negate();
    }

    @Override
    public Predicate<DataSetRow> empty() {
        return r -> true;
    }

    @Override
    public Predicate<DataSetRow> or(Predicate<DataSetRow> left, Predicate<DataSetRow> right) {
        return left.or(right);
    }

    /**
     * Create a predicate that checks if the var is equals to a value.
     * <p>
     * It first tries String comparison, and if not 'true' uses number comparison.
     *
     * @param node     The filter node
     * @param columnId The column id
     * @param value    The compare value
     * @return The eq predicate
     */
    @Override
    public Predicate<DataSetRow> createEqualsPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return DataSetRowFilters.createEqualsPredicate(columnId, value);
    }

    /**
     * Create a predicate that checks if the var is greater than a value
     *
     * @param node     The filter node
     * @param columnId The column id
     * @param value    The compare value
     * @return The gt predicate
     */
    @Override
    public Predicate<DataSetRow> createGreaterThanPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return DataSetRowFilters.createGreaterThanPredicate(columnId, value);
    }

    /**
     * Create a predicate that checks if the var is lower than a value
     *
     * @param node     The filter node
     * @param columnId The column id
     * @param value    The compare value
     * @return The lt predicate
     */
    @Override
    public Predicate<DataSetRow> createLowerThanPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return DataSetRowFilters.createLowerThanPredicate(columnId, value);
    }

    /**
     * Create a predicate that checks if the var is greater than or equals to a value
     *
     * @param node     The filter node
     * @param columnId The column id
     * @param value    The compare value
     * @return The gte predicate
     */
    @Override
    public Predicate<DataSetRow> createGreaterOrEqualsPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return DataSetRowFilters.createGreaterOrEqualsPredicate(columnId, value);
    }

    /**
     * Create a predicate that checks if the var is lower than or equals to a value
     *
     * @param node     The filter node
     * @param columnId The column id
     * @param value    The compare value
     * @return The lte predicate
     */
    @Override
    public Predicate<DataSetRow> createLowerOrEqualsPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return DataSetRowFilters.createLowerOrEqualsPredicate(columnId, value);
    }

    /**
     * Create a predicate that checks if the var contains a value
     *
     * @param node     The filter node
     * @param columnId The column id
     * @param value    The contained value
     * @return The contains predicate
     */
    @Override
    public Predicate<DataSetRow> createContainsPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return DataSetRowFilters.createContainsPredicate(columnId, value);
    }

    /**
     * Create a predicate that checks if the var matches a value
     *
     * @param node     The filter node
     * @param columnId The column id
     * @param value    The value to match
     * @return The match predicate
     */
    @Override
    public Predicate<DataSetRow> createCompliesPredicate(final JsonNode node, final String columnId, final String value) {
        checkValidValue(node, value);
        return DataSetRowFilters.createCompliesPredicate(columnId, value);
    }

    /**
     * Create a predicate that checks if the value is invalid
     *
     * @param columnId The column id
     * @return The invalid value predicate
     */
    @Override
    public Predicate<DataSetRow> createInvalidPredicate(final String columnId) {
        return DataSetRowFilters.createInvalidPredicate(columnId);
    }

    /**
     * Create a predicate that checks if the value is value (not empty and not invalid)
     *
     * @param columnId The column id
     * @return The valid value predicate
     */
    @Override
    public Predicate<DataSetRow> createValidPredicate(final String columnId) {
        return DataSetRowFilters.createValidPredicate(columnId);
    }

    /**
     * Create a predicate that checks if the value is empty
     *
     * @param columnId The column id
     * @return The empty value predicate
     */
    @Override
    public Predicate<DataSetRow> createEmptyPredicate(final String columnId) {
        return DataSetRowFilters.createEmptyPredicate(columnId);
    }

    /**
     * Create a predicate that checks if the value is within a range [min, max[
     *
     * @param columnId    The column id
     * @param node The node content that contains min/max values
     * @return The range predicate
     */
    @Override
    public Predicate<DataSetRow> createRangePredicate(final String columnId, final JsonNode node,
                                                      final RowMetadata rowMetadata) {
        final String start = node.get("start").asText();
        final String end = node.get("end").asText();
        final boolean upperBoundOpen = Optional.ofNullable(node.get("upperOpen")).map(JsonNode::asBoolean).orElse(true);
        final boolean lowerBoundOpen = Optional.ofNullable(node.get("lowerOpen")).map(JsonNode::asBoolean).orElse(false);
        return DataSetRowFilters.createRangePredicate(columnId, start, end, lowerBoundOpen, upperBoundOpen, rowMetadata);
    }

    /**
     * check if the node has a non null value
     *
     * @param node  The node to test
     * @param value The node 'value' property
     * @throws IllegalArgumentException If the node has not a 'value' property
     */
    private void checkValidValue(final JsonNode node, final String value) {
        if (value == null) {
            throw new UnsupportedOperationException("Unsupported query, the filter needs a value : " + node.toString());
        }
    }
}
