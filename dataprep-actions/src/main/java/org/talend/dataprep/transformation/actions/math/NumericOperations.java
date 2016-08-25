// ============================================================================
//
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

package org.talend.dataprep.transformation.actions.math;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.error.ActionErrorCodes;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Concat action concatenates 2 columns into a new one. The new column name will be "column_source + selected_column."
 * The new column content is "prefix + column_source + separator + selected_column + suffix"
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + NumericOperations.ACTION_NAME)
public class NumericOperations extends AbstractActionMetadata implements ColumnAction, OtherColumnParameters {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "numeric_ops"; //$NON-NLS-1$

    /**
     * Mode: tells if operand is taken from another column or is a constant
     */
    public static final String MODE_PARAMETER = "mode"; //$NON-NLS-1$

    /**
     * The operator to use.
     */
    public static final String OPERATOR_PARAMETER = "operator"; //$NON-NLS-1$

    /**
     * The operand to use.
     */
    public static final String OPERAND_PARAMETER = "operand"; //$NON-NLS-1$

    private static final String PLUS = "+";

    private static final String MINUS = "-";

    private static final String MULTIPLY = "x";

    private static final String DIVIDE = "/";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.MATH.getDisplayName();
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(OPERATOR_PARAMETER)
                        .item(PLUS)
                        .item(MULTIPLY)
                        .item(MINUS)
                        .item(DIVIDE)
                        .defaultValue(MULTIPLY)
                        .build()
        );
        //@formatter:on

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(MODE_PARAMETER)
                        .item(CONSTANT_MODE, new Parameter(OPERAND_PARAMETER, ParameterType.STRING, "2"))
                        .item(OTHER_COLUMN_MODE,
                              new Parameter(SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, //
                                            StringUtils.EMPTY, false, false, StringUtils.EMPTY)) //
                        .defaultValue(CONSTANT_MODE)
                        .build()
        );
        //@formatter:on

        return parameters;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        return Type.NUMERIC.isAssignableFrom(columnType);
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            checkParameters(context.getParameters(), context.getRowMetadata());
            // Create column
            final Map<String, String> parameters = context.getParameters();
            final String columnId = context.getColumnId();
            final RowMetadata rowMetadata = context.getRowMetadata();
            final ColumnMetadata sourceColumn = rowMetadata.getById(columnId);
            final String operator = parameters.get(OPERATOR_PARAMETER);
            String operandName;
            if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
                operandName = parameters.get(OPERAND_PARAMETER);
            } else {
                final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
                operandName = selectedColumn.getName();
            }
            context.column("result", r -> {
                final ColumnMetadata c = ColumnMetadata.Builder //
                        .column() //
                        .name(sourceColumn.getName() + " " + operator + " " + operandName) //
                        .type(Type.DOUBLE) //
                        .build();
                rowMetadata.insertAfter(columnId, c);
                return c;
            });

        }
    }

    @Override
    public void applyOnColumn(final DataSetRow row, final ActionContext context) {
        final Map<String, String> parameters = context.getParameters();
        final String columnId = context.getColumnId();

        final RowMetadata rowMetadata = context.getRowMetadata();

        // extract transformation parameters
        final String operator = parameters.get(OPERATOR_PARAMETER);
        String operand;
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            operand = parameters.get(OPERAND_PARAMETER);
        } else {
            final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            operand = row.get(selectedColumn.getId());
        }

        // column creation
        final String newColumnId = context.column("result");

        // set new column value
        final String sourceValue = row.get(columnId);
        final String newValue = compute(sourceValue, operator, operand);
        row.set(newColumnId, newValue);
    }

    protected String compute(final String stringOperandOne, final String operator, final String stringOperandTwo) {
        try {
            final BigDecimal operandOne = BigDecimalParser.toBigDecimal(stringOperandOne);
            final BigDecimal operandTwo = BigDecimalParser.toBigDecimal(stringOperandTwo);

            BigDecimal toReturn;

            final int scale = 2;
            final RoundingMode rm = HALF_UP;

            switch (operator) {
            case PLUS:
                toReturn = operandOne.add(operandTwo);
                break;
            case MULTIPLY:
                toReturn = operandOne.multiply(operandTwo);
                break;
            case MINUS:
                toReturn = operandOne.subtract(operandTwo);
                break;
            case DIVIDE:
                toReturn = operandOne.divide(operandTwo, scale, rm);
                break;
            default:
                return "";
            }

            // Format result:
            return toReturn.setScale(scale, rm).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException | ArithmeticException | NullPointerException e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Check that the selected column parameter is correct : defined in the parameters and there's a matching column. If
     * the parameter is invalid, an exception is thrown.
     *
     * @param parameters where to look the parameter value.
     * @param rowMetadata the row where to look for the column.
     */
    private void checkParameters(Map<String, String> parameters, RowMetadata rowMetadata) {
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) && !parameters.containsKey(OPERAND_PARAMETER)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put("paramName", OPERAND_PARAMETER));
        } else if (!parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) && (!parameters.containsKey(SELECTED_COLUMN_PARAMETER)
                || rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER)) == null)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put("paramName", SELECTED_COLUMN_PARAMETER));
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
