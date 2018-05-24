// ============================================================================
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

package org.talend.dataprep.api.filter;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A walker for the JSON structure of a filter. This class walks the JSON structure and calls the corresponding methods
 * in {@link JSONFilterCallback}.
 *
 * @see JSONFilterCallback
 * @see PredicateCallback
 * @see #walk(String, RowMetadata, JSONFilterCallback)
 */
public class JSONFilterWalker {

    protected static final Logger LOG = LoggerFactory.getLogger(JSONFilterWalker.class);

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

    private static final List<String> ALLOWED =
            Arrays.asList(EQ, GT, LT, GTE, LTE, CONTAINS, MATCHES, INVALID, VALID, EMPTY, RANGE);

    private JSONFilterWalker() {
        // Utility class, no need for public constructor.
    }

    /**
     * Walks the JSON structure of <code>filterAsString</code> and calls the methods in <code>callback</code>
     *
     * @param filterAsString The JSON filter to be parsed.
     * @param rowMetadata An optional row metadata.
     * @param callback The {@link JSONFilterCallback callback} to be called during walk.
     * @param <T> The type of the return once walk is done.
     * @return An instance of T that corresponds to the filter as string.
     */
    public static <T> T walk(String filterAsString, RowMetadata rowMetadata, JSONFilterCallback<T> callback) {
        if (isEmpty(filterAsString)) {
            return callback.empty();
        }
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.reader().readTree(filterAsString);
            final Iterator<JsonNode> elements = root.elements();
            if (!elements.hasNext()) {
                throw new IllegalArgumentException("Malformed filter: " + filterAsString);
            } else {
                return buildFilter(root, rowMetadata, callback);
            }
        } catch (Exception e) {
            LOG.warn("Unable to parse filter {]", filterAsString, e);
            throw new TalendRuntimeException(BaseErrorCodes.UNABLE_TO_PARSE_FILTER, e);
        }
    }

    /**
     * Validate the filter as a correct json object.
     *
     * @param filterAsString The JSON filter to be parsed.
     * @throws TalendRuntimeException if filter is not valid.
     */
    public static void validate(String filterAsString) {
        if (!StringUtils.isEmpty(filterAsString)) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.reader().readTree(filterAsString);
            } catch (IOException e) {
                throw new TalendRuntimeException(BaseErrorCodes.UNABLE_TO_PARSE_FILTER, e);
            }
        }
    }

    // Internal method for walk structure
    private static <T> T buildFilter(JsonNode currentNode, RowMetadata rowMetadata, JSONFilterCallback<T> callBack) {
        final Iterator<JsonNode> children = currentNode.elements();
        final JsonNode operationContent = children.next();
        final String columnId = operationContent.has("field") ? operationContent.get("field").asText() : null;
        final String value = operationContent.has("value") ? operationContent.get("value").asText() : null;

        final Iterator<String> propertiesIterator = currentNode.fieldNames();
        if (!propertiesIterator.hasNext()) {
            throw new UnsupportedOperationException(
                    "Unsupported query, empty filter definition: " + currentNode.toString());
        }

        final String operation = propertiesIterator.next();
        if (columnId == null && allowFullFilter(operation)) {

            // Full data set filter (no column)
            final List<ColumnMetadata> columns = rowMetadata.getColumns();
            T predicate;
            if (!columns.isEmpty()) {
                predicate = buildOperationFilter(currentNode, rowMetadata, "*", operation, value, callBack);
            } else {
                // We can't return a null filter, default to the neutral value
                predicate = callBack.empty();
            }
            return predicate;
        } else {
            return buildOperationFilter(currentNode, rowMetadata, columnId, operation, value, callBack);
        }
    }

    private static boolean allowFullFilter(String operation) {
        return ALLOWED.contains(operation);
    }

    private static <T> T buildOperationFilter(JsonNode currentNode, //
            RowMetadata rowMetadata, //
            String columnId, //
            String operation, //
            String value, //
            JSONFilterCallback<T> callBack) {
        switch (operation) {
        case EQ:
            return callBack.createEqualsPredicate(currentNode, columnId, value);
        case GT:
            return callBack.createGreaterThanPredicate(currentNode, columnId, value);
        case LT:
            return callBack.createLowerThanPredicate(currentNode, columnId, value);
        case GTE:
            return callBack.createGreaterOrEqualsPredicate(currentNode, columnId, value);
        case LTE:
            return callBack.createLowerOrEqualsPredicate(currentNode, columnId, value);
        case CONTAINS:
            return callBack.createContainsPredicate(currentNode, columnId, value);
        case MATCHES:
            return callBack.createCompliesPredicate(currentNode, columnId, value);
        case INVALID:
            return callBack.createInvalidPredicate(columnId);
        case VALID:
            return callBack.createValidPredicate(columnId);
        case EMPTY:
            return callBack.createEmptyPredicate(columnId);
        case RANGE:
            return callBack.createRangePredicate(columnId, currentNode.elements().next(), rowMetadata);
        case AND:
            return createAndPredicate(currentNode.elements().next(), rowMetadata, callBack);
        case OR:
            return createOrPredicate(currentNode.elements().next(), rowMetadata, callBack);
        case NOT:
            return createNotPredicate(currentNode.elements().next(), rowMetadata, callBack);
        default:
            throw new UnsupportedOperationException(
                    "Unsupported query, unknown filter '" + operation + "': " + currentNode.toString());
        }
    }

    private static <T> T createAndPredicate(final JsonNode nodeContent, RowMetadata rowMetadata,
            JSONFilterCallback<T> callback) {
        final T leftFilter = walk(nodeContent.get(0).toString(), rowMetadata, callback);
        final T rightFilter = walk(nodeContent.get(1).toString(), rowMetadata, callback);
        return callback.and(leftFilter, rightFilter);
    }

    private static <T> T createOrPredicate(final JsonNode nodeContent, RowMetadata rowMetadata,
            JSONFilterCallback<T> callback) {
        final T leftFilter = walk(nodeContent.get(0).toString(), rowMetadata, callback);
        final T rightFilter = walk(nodeContent.get(1).toString(), rowMetadata, callback);
        return callback.or(leftFilter, rightFilter);
    }

    private static <T> T createNotPredicate(final JsonNode nodeContent, RowMetadata rowMetadata,
            JSONFilterCallback<T> callback) {
        if (!nodeContent.isObject()) {
            throw new IllegalArgumentException("Unsupported query, malformed 'not' (expected 1 object child).");
        }
        if (nodeContent.size() == 0) {
            throw new IllegalArgumentException("Unsupported query, malformed 'not' (object child is empty).");
        }
        return callback.not(walk(nodeContent.toString(), rowMetadata, callback));
    }
}
