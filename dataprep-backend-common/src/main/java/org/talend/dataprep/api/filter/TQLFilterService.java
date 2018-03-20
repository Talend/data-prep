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

import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.tql.model.*;
import org.talend.tql.parser.Tql;
import org.talend.tql.visitor.IASTVisitor;

import static org.talend.dataprep.api.filter.DataSetRowFilters.*;

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
            throw new NotImplementedException();
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
            throw new NotImplementedException();
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
            final List<String> collect = Stream.of(fieldInExpression.getValues()).map(LiteralValue::getValue).collect(Collectors.toList());

            return row -> collect.contains(row.get(columnName));
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
            final Pattern pattern = Pattern.compile(regex);

            return row -> pattern.matcher(row.get(columnName)).matches();
        }

        @Override
        public Predicate<DataSetRow> visit(FieldCompliesPattern fieldCompliesPattern) {
            fieldCompliesPattern.getField().accept(this);
            final String columnName = fields.pop();
            final String pattern = fieldCompliesPattern.getPattern();

            return createCompliesPredicate(columnName, pattern);
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
            fields.push("*");
            return null;
        }
    }
}
