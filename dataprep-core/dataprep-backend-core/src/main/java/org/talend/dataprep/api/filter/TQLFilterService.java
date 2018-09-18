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

import static org.talend.dataprep.api.filter.DataSetRowFilters.createCompliesPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createContainsPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createEmptyPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createEqualsPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createGreaterOrEqualsPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createGreaterThanPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createInPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createInvalidPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createLowerOrEqualsPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createLowerThanPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createMatchesPredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createRangePredicate;
import static org.talend.dataprep.api.filter.DataSetRowFilters.createValidPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.tql.model.AllFields;
import org.talend.tql.model.AndExpression;
import org.talend.tql.model.ComparisonExpression;
import org.talend.tql.model.ComparisonOperator;
import org.talend.tql.model.Expression;
import org.talend.tql.model.FieldBetweenExpression;
import org.talend.tql.model.FieldCompliesPattern;
import org.talend.tql.model.FieldContainsExpression;
import org.talend.tql.model.FieldInExpression;
import org.talend.tql.model.FieldIsEmptyExpression;
import org.talend.tql.model.FieldIsInvalidExpression;
import org.talend.tql.model.FieldIsValidExpression;
import org.talend.tql.model.FieldMatchesRegex;
import org.talend.tql.model.FieldReference;
import org.talend.tql.model.FieldWordCompliesPattern;
import org.talend.tql.model.LiteralValue;
import org.talend.tql.model.NotExpression;
import org.talend.tql.model.OrExpression;
import org.talend.tql.model.TqlElement;
import org.talend.tql.parser.Tql;
import org.talend.tql.visitor.IASTVisitor;

/**
 * A {@link FilterService} implementation that parses TQL and builds a filter.
 */
public class TQLFilterService implements FilterService {

    @Override
    public Predicate<DataSetRow> build(String filterAsString, RowMetadata rowMetadata) {
        if (StringUtils.isEmpty(filterAsString)) {
            return row -> true;
        }
        final TqlElement parsedPredicate = Tql.parse(filterAsString);
        return parsedPredicate.accept(new DataSetPredicateVisitor(rowMetadata));
    }

    /**
     * Get the list of columns present in the filter expression.
     *
     * @param filterAsString The non-blank filter expression.
     * @param rowMetadata The rowMetaData to get the list of all columns of the dataSet.
     * @return List of columns present in the filter expression.
     */
    public List<ColumnMetadata> getFilterColumnsMetadata(String filterAsString, RowMetadata rowMetadata) {
        final TqlElement parsedTqlElement = Tql.parse(filterAsString);
        DatasetColumnVisitor datasetColumnVisitor = new DatasetColumnVisitor();
        parsedTqlElement.accept(datasetColumnVisitor);

        return rowMetadata
                .getColumns()
                .stream()
                .filter(column -> datasetColumnVisitor.getColumns().contains(column.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Visitor used to retrieve the column ids present in the filter expression.
     * For example (0001 > 5) returns {"0001"}.
     */
    private static class DatasetColumnVisitor implements IASTVisitor<List<String>> {

        private final List<String> columns = new ArrayList<>();

        private DatasetColumnVisitor() {
        }

        public List<String> getColumns() {
            return columns;
        }

        @Override
        public List<String> visit(TqlElement tqlElement) {
            return columns;
        }

        @Override
        public List<String> visit(ComparisonOperator comparisonOperator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> visit(LiteralValue literalValue) {
            return columns;
        }

        @Override
        public List<String> visit(FieldReference fieldReference) {
            columns.add(fieldReference.getPath());
            return columns;
        }

        @Override
        public List<String> visit(Expression expression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> visit(AndExpression andExpression) {
            for (Expression expression : andExpression.getExpressions()) {
                expression.accept(this);
            }
            return columns;
        }

        @Override
        public List<String> visit(OrExpression orExpression) {
            for (Expression expression : orExpression.getExpressions()) {
                expression.accept(this);
            }
            return columns;
        }

        @Override
        public List<String> visit(ComparisonExpression comparisonExpression) {
            comparisonExpression.getField().accept(this);
            return columns;
        }

        @Override
        public List<String> visit(FieldInExpression fieldInExpression) {
            fieldInExpression.getField().accept(this);
            return columns;
        }

        @Override
        public List<String> visit(FieldIsEmptyExpression fieldIsEmptyExpression) {
            fieldIsEmptyExpression.getField().accept(this);
            return columns;
        }

        @Override
        public List<String> visit(FieldIsValidExpression fieldIsValidExpression) {
            fieldIsValidExpression.getField().accept(this);
            return columns;
        }

        @Override
        public List<String> visit(FieldIsInvalidExpression fieldIsInvalidExpression) {
            fieldIsInvalidExpression.getField().accept(this);
            return columns;
        }

        @Override
        public List<String> visit(FieldMatchesRegex fieldMatchesRegex) {
            fieldMatchesRegex.getField().accept(this);
            return columns;
        }

        @Override
        public List<String> visit(FieldCompliesPattern fieldCompliesPattern) {
            fieldCompliesPattern.getField().accept(this);
            return columns;
        }

        @Override
        public List<String> visit(FieldWordCompliesPattern fieldWordCompliesPattern) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> visit(FieldBetweenExpression fieldBetweenExpression) {
            fieldBetweenExpression.getField().accept(this);
            return columns;
        }

        @Override
        public List<String> visit(NotExpression notExpression) {
            return columns;
        }

        @Override
        public List<String> visit(FieldContainsExpression fieldContainsExpression) {
            fieldContainsExpression.getField().accept(this);
            return columns;
        }

        @Override
        public List<String> visit(AllFields allFields) {
            return columns;
        }
    }

    /**
     * Apply the filter to a dataSet row.
     */
    private static class DataSetPredicateVisitor implements IASTVisitor<Predicate<DataSetRow>> {

        private final RowMetadata rowMetadata;

        private final Stack<String> values = new Stack<>();

        private final Stack<String> fields = new Stack<>();

        private DataSetPredicateVisitor(RowMetadata rowMetadata) {
            this.rowMetadata = rowMetadata;
        }

        @Override
        public Predicate<DataSetRow> visit(TqlElement tqlElement) {
            return r -> true;
        }

        @Override
        public Predicate<DataSetRow> visit(ComparisonOperator comparisonOperator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Predicate<DataSetRow> visit(LiteralValue literalValue) {
            values.push(literalValue.getValue());
            return null;
        }

        @Override
        public Predicate<DataSetRow> visit(FieldReference fieldReference) {
            fields.push(fieldReference.getPath());
            return null;
        }

        @Override
        public Predicate<DataSetRow> visit(Expression expression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Predicate<DataSetRow> visit(AndExpression andExpression) {
            Predicate<DataSetRow> predicate = null;
            for (Expression expression : andExpression.getExpressions()) {
                if (predicate != null) {
                    predicate = predicate.and(expression.accept(this));
                } else {
                    predicate = expression.accept(this);
                }
            }
            return predicate;
        }

        @Override
        public Predicate<DataSetRow> visit(OrExpression orExpression) {
            Predicate<DataSetRow> predicate = null;
            for (Expression expression : orExpression.getExpressions()) {
                if (predicate != null) {
                    predicate = predicate.or(expression.accept(this));
                } else {
                    predicate = expression.accept(this);
                }
            }
            return predicate;
        }

        @Override
        public Predicate<DataSetRow> visit(ComparisonExpression comparisonExpression) {
            final ComparisonOperator.Enum operator = comparisonExpression.getOperator().getOperator();
            comparisonExpression.getField().accept(this);
            final String columnName = fields.pop();
            comparisonExpression.getValueOrField().accept(this);
            final String value = values.pop();

            switch (operator) {
            case EQ:
                return createEqualsPredicate(columnName, value);
            case LT:
                return createLowerThanPredicate(columnName, value);
            case GT:
                return createGreaterThanPredicate(columnName, value);
            case NEQ:
                return createEqualsPredicate(columnName, value).negate();
            case LET:
                return createLowerOrEqualsPredicate(columnName, value);
            case GET:
                return createGreaterOrEqualsPredicate(columnName, value);
            }
            return null;
        }

        @Override
        public Predicate<DataSetRow> visit(FieldInExpression fieldInExpression) {
            fieldInExpression.getField().accept(this);
            final String columnName = fields.pop();
            final List<String> collect =
                    Stream.of(fieldInExpression.getValues()).map(LiteralValue::getValue).collect(Collectors.toList());

            return createInPredicate(columnName, collect);
        }

        @Override
        public Predicate<DataSetRow> visit(FieldIsEmptyExpression fieldIsEmptyExpression) {
            fieldIsEmptyExpression.getField().accept(this);
            final String columnName = fields.pop();
            return createEmptyPredicate(columnName);
        }

        @Override
        public Predicate<DataSetRow> visit(FieldIsValidExpression fieldIsValidExpression) {
            fieldIsValidExpression.getField().accept(this);
            final String columnName = fields.pop();
            return createValidPredicate(columnName);
        }

        @Override
        public Predicate<DataSetRow> visit(FieldIsInvalidExpression fieldIsInvalidExpression) {
            fieldIsInvalidExpression.getField().accept(this);
            final String columnName = fields.pop();
            return createInvalidPredicate(columnName);
        }

        @Override
        public Predicate<DataSetRow> visit(FieldMatchesRegex fieldMatchesRegex) {
            fieldMatchesRegex.getField().accept(this);
            final String columnName = fields.pop();
            final String regex = fieldMatchesRegex.getRegex();
            return createMatchesPredicate(columnName, regex);
        }

        @Override
        public Predicate<DataSetRow> visit(FieldCompliesPattern fieldCompliesPattern) {
            fieldCompliesPattern.getField().accept(this);
            final String columnName = fields.pop();
            final String pattern = fieldCompliesPattern.getPattern();

            return createCompliesPredicate(columnName, pattern);
        }

        @Override
        public Predicate<DataSetRow> visit(FieldWordCompliesPattern fieldWordCompliesPattern) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Predicate<DataSetRow> visit(FieldBetweenExpression fieldBetweenExpression) {
            fieldBetweenExpression.getField().accept(this);
            final String columnName = fields.pop();
            final String low = fieldBetweenExpression.getLeft().getValue();
            final String high = fieldBetweenExpression.getRight().getValue();
            final boolean lowerOpen = fieldBetweenExpression.isLowerOpen();
            final boolean upperOpen = fieldBetweenExpression.isUpperOpen();

            return createRangePredicate(columnName, low, high, lowerOpen, upperOpen, rowMetadata);
        }

        @Override
        public Predicate<DataSetRow> visit(NotExpression notExpression) {
            return notExpression.getExpression().accept(this).negate();
        }

        @Override
        public Predicate<DataSetRow> visit(FieldContainsExpression fieldContainsExpression) {
            fieldContainsExpression.getField().accept(this);
            final String columnName = fields.pop();
            final String value = fieldContainsExpression.getValue();

            return createContainsPredicate(columnName, value);
        }

        @Override
        public Predicate<DataSetRow> visit(AllFields allFields) {
            // character * represent all fields.
            fields.push("*");
            return null;
        }
    }
}
