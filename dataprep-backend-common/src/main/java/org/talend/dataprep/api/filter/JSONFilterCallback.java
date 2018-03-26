package org.talend.dataprep.api.filter;

import com.fasterxml.jackson.databind.JsonNode;
import org.talend.dataprep.api.dataset.RowMetadata;

/**
 * An interface to be implemented to convert a JSON filter into T.
 * @param <T> The return type
 */
public interface JSONFilterCallback<T> {

    /**
     * @return The type to be used on empty expression.
     */
    T empty();

    /**
     * @param currentNode The current JSON expression node.
     * @param columnId the column id for the expression.
     * @param value The value for equals.
     * @return An equals expression T.
     */
    T createEqualsPredicate(JsonNode currentNode, String columnId, String value);

    /**
     * @param currentNode The current JSON expression node.
     * @param columnId the column id for the expression.
     * @param value The value for greater than.
     * @return An <i>greater than</i> expression T.
     */
    T createGreaterThanPredicate(JsonNode currentNode, String columnId, String value);

    /**
     * @param currentNode The current JSON expression node.
     * @param columnId the column id for the expression.
     * @param value The value for lower than.
     * @return An <i>lower than</i> expression T.
     */
    T createLowerThanPredicate(JsonNode currentNode, String columnId, String value);

    /**
     * @param currentNode The current JSON expression node.
     * @param columnId the column id for the expression.
     * @param value The value for greater or equals.
     * @return An <i>greater than or equals</i> expression T.
     */
    T createGreaterOrEqualsPredicate(JsonNode currentNode, String columnId, String value);

    /**
     * @param currentNode The current JSON expression node.
     * @param columnId the column id for the expression.
     * @param value The value for lower or equals.
     * @return An <i>lower than or equals</i> expression T.
     */
    T createLowerOrEqualsPredicate(JsonNode currentNode, String columnId, String value);

    /**
     * @param currentNode The current JSON expression node.
     * @param columnId the column id for the expression.
     * @param value The value for contains.
     * @return An <i>contains</i> expression T.
     */
    T createContainsPredicate(JsonNode currentNode, String columnId, String value);

    /**
     * @param currentNode The current JSON expression node.
     * @param columnId the column id for the expression.
     * @param value The value for matches.
     * @return An <i>matches</i> expression T.
     */
    T createCompliesPredicate(JsonNode currentNode, String columnId, String value);

    /**
     * @param columnId the column id for the expression.
     * @return An <i>invalid</i> expression T.
     */
    T createInvalidPredicate(String columnId);

    /**
     * @param columnId the column id for the expression.
     * @return An <i>valid</i> expression T.
     */
    T createValidPredicate(String columnId);

    /**
     * @param columnId the column id for the expression.
     * @return An <i>empty</i> expression T.
     */
    T createEmptyPredicate(String columnId);

    /**
     * @param columnId the column id for the expression.
     * @return An <i>range</i> expression T.
     */
    T createRangePredicate(String columnId, JsonNode node, RowMetadata rowMetadata);

    /**
     * Combines the 2 expressions for a AND.
     * @param left left expression.
     * @param right right expression.
     * @return A <i>and</i> between 2 expressions.
     */
    T or(T left, T right);

    /**
     * Combines the 2 expressions for a OR.
     * @param left left expression.
     * @param right right expression.
     * @return A <i>or</i> between 2 expressions.
     */
    T and(T left, T right);

    /**
     * Negates the expression.
     * @param expression the expression to negate.
     * @return A <i>not</i> between 2 expressions.
     */
    T not(T expression);
}
