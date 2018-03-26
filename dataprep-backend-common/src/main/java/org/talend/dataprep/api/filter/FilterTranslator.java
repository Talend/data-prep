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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.RowMetadata;

import java.util.Optional;

import static org.talend.dataprep.api.filter.JSONFilterWalker.walk;

/**
 * Translate legacy JSON filters to TQL filters.
 */
public class FilterTranslator {

    /**
     * Translates the JSON <code>filter</code> into TQL. If the <code>filter</code> is already a TQL query, this method
     * is a no op.
     *
     * @param filter The filter to be translated to TQL.
     * @return The <code>filter</code>
     */
    public String toTQL(final String filter) {
        if (StringUtils.isBlank(filter) || !filter.startsWith("{")) {
            return filter;
        }
        return walk(filter, new RowMetadata(), new ToTQLCallback());
    }

    private static class ToTQLCallback implements JSONFilterCallback<String> {

        @Override
        public String empty() {
            return StringUtils.EMPTY;
        }

        @Override
        public String createEqualsPredicate(JsonNode currentNode, String columnId, String value) {
            return columnId + " = '" + value + "'";
        }

        @Override
        public String createGreaterThanPredicate(JsonNode currentNode, String columnId, String value) {
            return columnId + " > '" + value + "'";
        }

        @Override
        public String createLowerThanPredicate(JsonNode currentNode, String columnId, String value) {
            return columnId + " < '" + value + "'";
        }

        @Override
        public String createGreaterOrEqualsPredicate(JsonNode currentNode, String columnId, String value) {
            return columnId + " >= '" + value + "'";
        }

        @Override
        public String createLowerOrEqualsPredicate(JsonNode currentNode, String columnId, String value) {
            return columnId + " <= '" + value + "'";
        }

        @Override
        public String createContainsPredicate(JsonNode currentNode, String columnId, String value) {
            return columnId + " contains '" + value + "'";
        }

        @Override
        public String createCompliesPredicate(JsonNode currentNode, String columnId, String value) {
            return columnId + " matches " + value;
        }

        @Override
        public String createInvalidPredicate(String columnId) {
            return columnId + " is invalid";
        }

        @Override
        public String createValidPredicate(String columnId) {
            return columnId + " is valid";
        }

        @Override
        public String createEmptyPredicate(String columnId) {
            return columnId + " is empty";
        }

        @Override
        public String createRangePredicate(String columnId, JsonNode node, RowMetadata rowMetadata) {
            final boolean upperBoundOpen = Optional.ofNullable(node.get("upperOpen")).map(JsonNode::asBoolean).orElse(true);
            final boolean lowerBoundOpen = Optional.ofNullable(node.get("lowerOpen")).map(JsonNode::asBoolean).orElse(false);
            final String lowerBound = lowerBoundOpen ? "]" : "[";
            final String upperBound = upperBoundOpen ? "[" : "]";

            return columnId + " between " + lowerBound + node.get("start").asText() + ", " + node.get("end").asText() + upperBound;
        }

        @Override
        public String or(String left, String right) {
            return "(" + left + ") or (" + right + ")";
        }

        @Override
        public String and(String left, String right) {
            return "(" + left + ") and (" + right + ")";
        }

        @Override
        public String not(String expression) {
            return "!(" + expression + ")";
        }
    }
}
