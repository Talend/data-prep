package org.talend.dataprep.api.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class SimpleFilterService implements FilterService {

    @Override
    public Predicate<DataSetRow> build(String filterAsString) {
        if (StringUtils.isEmpty(filterAsString)) {
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
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_FILTER, e);
        }
    }

    private Predicate<DataSetRow> buildFilter(JsonNode currentNode) {
        final Iterator<JsonNode> children = currentNode.elements();
        final JsonNode currentNodeContent = children.next();
        final String columnName = currentNodeContent.has("field") ? currentNodeContent.get("field").asText() : null;
        final String value = currentNodeContent.has("value") ? currentNodeContent.get("value").asText() : null;
        if (value != null) {
            if (currentNode.has("eq")) {
                return r -> StringUtils.equals(r.get(columnName), value);
            } else if (currentNode.has("gt")) {
                return safe(r -> Double.parseDouble(r.get(columnName)) > Double.parseDouble(value));
            } else if (currentNode.has("lt")) {
                return safe(r -> Double.parseDouble(r.get(columnName)) < Double.parseDouble(value));
            } else if (currentNode.has("gte")) {
                return safe(r -> Double.parseDouble(r.get(columnName)) >= Double.parseDouble(value));
            } else if (currentNode.has("lte")) {
                return safe(r -> Double.parseDouble(r.get(columnName)) <= Double.parseDouble(value));
            } else if (currentNode.has("contains")) {
                return r -> StringUtils.containsIgnoreCase(r.get(columnName), value);
            }
        } else {
            if (currentNode.has("invalid")) {
                return r -> {
                    final ColumnMetadata column = r.getRowMetadata().getById(columnName);
                    if (column != null) {
                        final Set<String> invalidValues = column.getQuality().getInvalidValues();
                        String columnValue = r.get(columnName);
                        return invalidValues.contains(columnValue);
                    }
                    return true;
                };
            } else if (currentNode.has("valid")) {
                return r -> {
                    final ColumnMetadata column = r.getRowMetadata().getById(columnName);
                    if (column != null) {
                        final Set<String> invalidValues = column.getQuality().getInvalidValues();
                        String columnValue = r.get(columnName);
                        return !StringUtils.isEmpty(columnValue) && !invalidValues.contains(columnValue);
                    }
                    return true;
                };
            } else if (currentNode.has("empty")) {
                return r -> StringUtils.isEmpty(r.get(columnName));
            } else if (currentNode.has("range")) {
                final String start = currentNodeContent.get("start").asText();
                final String end = currentNodeContent.get("end").asText();
                return safe(r -> {
                    final double columnValue = Double.parseDouble(r.get(columnName));
                    final double min = Double.parseDouble(start);
                    final double max = Double.parseDouble(end);
                    if (min != max) {
                        return columnValue >= min && columnValue < max;
                    } else {
                        return columnValue == min;
                    }
                });
            } else if (currentNode.has("and")) {
                if (currentNodeContent.size() != 2) {
                    throw new IllegalArgumentException("Malformed 'and' (expected 2 children).");
                }
                return r -> buildFilter(currentNodeContent.get(0)).and(buildFilter(currentNodeContent.get(1))).test(r);
            } else if (currentNode.has("or")) {
                if (currentNodeContent.size() != 2) {
                    throw new IllegalArgumentException("Malformed 'or' (expected 2 children).");
                }
                return r -> buildFilter(currentNodeContent.get(0)).or(buildFilter(currentNodeContent.get(1))).test(r);
            } else if (currentNode.has("not")) {
                if (!currentNodeContent.isObject()) {
                    throw new IllegalArgumentException("Malformed 'not' (expected 1 object child).");
                }
                if (currentNodeContent.size() == 0) {
                    throw new IllegalArgumentException("Malformed 'not' (object child is empty).");
                }
                return buildFilter(currentNodeContent).negate();
            }
        }
        throw new UnsupportedOperationException("Unsupported query: " + currentNode.toString());
    }

    private static Predicate<DataSetRow> safe(Predicate<DataSetRow> inner) {
        return r -> {
            try {
                return inner.test(r);
            } catch (NumberFormatException e) {
                return false;
            } catch (NullPointerException e) {
                // Double.parseDouble throws NPE when parsing null strings.
                return false;
            }
        };
    }
}
