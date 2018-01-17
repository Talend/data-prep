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

import static org.talend.daikon.number.BigDecimalParser.toBigDecimal;

import java.util.List;
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

/**
 * A {@link FilterService} implementation that parses TQL and builds a filter.
 */
public class TQLFilterService implements FilterService {

    @Override
    public Predicate<DataSetRow> build(String filterAsString, RowMetadata rowMetadata) {
        final TqlElement parsedPredicate = Tql.parse(filterAsString);
        return (Predicate<DataSetRow>) parsedPredicate.accept(new DataSetPredicateVisitor());
    }

    private static class DataSetPredicateVisitor implements IASTVisitor {

        @Override
        public Object visit(TqlElement tqlElement) {
            return null;
        }

        @Override
        public Object visit(ComparisonOperator comparisonOperator) {
            throw new NotImplementedException();
        }

        @Override
        public Object visit(LiteralValue literalValue) {
            return literalValue.getValue();
        }

        @Override
        public Object visit(FieldReference fieldReference) {
            return fieldReference.getPath();
        }

        @Override
        public Object visit(Expression expression) {
            throw new NotImplementedException();
        }

        @Override
        public Object visit(AndExpression andExpression) {
            Predicate<DataSetRow> predicate = null;
            for (Expression expression : andExpression.getExpressions()) {
                if (predicate != null) {
                    predicate = predicate.and((Predicate<DataSetRow>) expression.accept(this));
                } else {
                    predicate = (Predicate<DataSetRow>) expression.accept(this);
                }
            }
            return predicate;
        }

        @Override
        public Object visit(OrExpression orExpression) {
            Predicate<DataSetRow> predicate = null;
            for (Expression expression : orExpression.getExpressions()) {
                if (predicate != null) {
                    predicate = predicate.or((Predicate<DataSetRow>) expression.accept(this));
                } else {
                    predicate = (Predicate<DataSetRow>) expression.accept(this);
                }
            }
            return predicate;
        }

        @Override
        public Predicate<DataSetRow> visit(ComparisonExpression comparisonExpression) {
            final ComparisonOperator.Enum operator = comparisonExpression.getOperator().getOperator();
            final String columnName = (String) comparisonExpression.getField().accept(this);
            final String value = (String) comparisonExpression.getValueOrField().accept(this);

            switch (operator) {
            case EQ:
                return row -> StringUtils.equals(row.get(columnName), value);
            case LT:
                return row -> toBigDecimal(row.get(columnName)).compareTo(toBigDecimal(value)) < 0;
            case GT:
                return row -> toBigDecimal(row.get(columnName)).compareTo(toBigDecimal(value)) > 0;
            case NEQ:
                return row -> !StringUtils.equals(row.get(columnName), value);
            case LET:
                return row -> toBigDecimal(row.get(columnName)).compareTo(toBigDecimal(value)) <= 0;
            case GET:
                return row -> toBigDecimal(row.get(columnName)).compareTo(toBigDecimal(value)) >= 0;
            }
            return null;
        }

        @Override
        public Predicate<DataSetRow> visit(FieldInExpression fieldInExpression) {
            final String fieldName = ((FieldReference) fieldInExpression.getField()).getPath();
            final List<String> collect =
                    Stream.of(fieldInExpression.getValues()).map(LiteralValue::getValue).collect(Collectors.toList());

            return row -> collect.contains(row.get(fieldName));
        }

        @Override
        public Predicate<DataSetRow> visit(FieldIsEmptyExpression fieldIsEmptyExpression) {
            final String fieldName = ((FieldReference) fieldIsEmptyExpression.getField()).getPath();
            return row -> StringUtils.isBlank(row.get(fieldName));
        }

        @Override
        public Predicate<DataSetRow> visit(FieldIsValidExpression fieldIsValidExpression) {
            final String fieldName = ((FieldReference) fieldIsValidExpression.getField()).getPath();
            return row -> row.isInvalid(fieldName);
        }

        @Override
        public Predicate<DataSetRow> visit(FieldIsInvalidExpression fieldIsInvalidExpression) {
            final String fieldName = ((FieldReference) fieldIsInvalidExpression.getField()).getPath();
            return row -> !row.isInvalid(fieldName);
        }

        @Override
        public Predicate<DataSetRow> visit(FieldMatchesRegex fieldMatchesRegex) {
            final String fieldName = ((FieldReference) fieldMatchesRegex.getField()).getPath();
            final String regex = fieldMatchesRegex.getRegex();
            final Pattern pattern = Pattern.compile(regex);

            return row -> pattern.matcher(row.get(fieldName)).matches();
        }

        @Override
        public Object visit(FieldCompliesPattern fieldCompliesPattern) {
            throw new NotImplementedException();
        }

        @Override
        public Predicate<DataSetRow> visit(FieldBetweenExpression fieldBetweenExpression) {
            final String fieldName = ((FieldReference) fieldBetweenExpression.getField()).getPath();
            final String low = fieldBetweenExpression.getLeft().getValue();
            final String high = fieldBetweenExpression.getRight().getValue();

            return row -> toBigDecimal(row.get(fieldName)).compareTo(toBigDecimal(low)) < 0
                    && toBigDecimal(row.get(fieldName)).compareTo(toBigDecimal(high)) > 0;
        }

        @Override
        public Predicate<DataSetRow> visit(NotExpression notExpression) {
            return ((Predicate<DataSetRow>) notExpression.accept(this)).negate();
        }

        @Override
        public Predicate<DataSetRow> visit(FieldContainsExpression fieldContainsExpression) {
            final String fieldName = ((FieldReference) fieldContainsExpression.getField()).getPath();
            final String value = fieldContainsExpression.getValue();

            return row -> StringUtils.contains(row.get(fieldName), value);
        }

        @Override
        public Object visit(AllFields allFields) {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }
}
