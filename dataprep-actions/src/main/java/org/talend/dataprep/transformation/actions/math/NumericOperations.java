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

package org.talend.dataprep.transformation.actions.math;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.COLUMN;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.util.NumericHelper;

/**
 * Concat action concatenates 2 columns into a new one. The new column name will be "column_source + selected_column."
 * The new column content is "prefix + column_source + separator + selected_column + suffix"
 */
@Action(NumericOperations.ACTION_NAME)
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

    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(NumericOperations.class);

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.MATH.getDisplayName(locale);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));

        //@formatter:off
        parameters.add(selectParameter(locale)
                        .name(OPERATOR_PARAMETER)
                        .item(PLUS)
                        .item(MULTIPLY)
                        .item(MINUS)
                        .item(DIVIDE)
                        .defaultValue(MULTIPLY)
                        .build(this )
        );
        //@formatter:on

        //@formatter:off
        parameters.add(selectParameter(locale)
                        .name(MODE_PARAMETER)
                        .item(CONSTANT_MODE, CONSTANT_MODE, parameter(locale).setName(OPERAND_PARAMETER).setType(STRING).setDefaultValue("2").build(this))
                        .item(OTHER_COLUMN_MODE, OTHER_COLUMN_MODE,
                              parameter(locale).setName(SELECTED_COLUMN_PARAMETER).setType(COLUMN).setDefaultValue(EMPTY).setCanBeBlank(false).build(this)) //
                        .defaultValue(CONSTANT_MODE)
                        .build(this )
        );
        //@formatter:on

        return parameters;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        return Type.NUMERIC.isAssignableFrom(columnType);
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        final List<ActionsUtils.AdditionalColumn> additionalColumns = new ArrayList<>();
        final Map<String, String> parameters = context.getParameters();
        final RowMetadata rowMetadata = context.getRowMetadata();
        final String operator = parameters.get(OPERATOR_PARAMETER);
        String operandName;
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            operandName = parameters.get(OPERAND_PARAMETER);
        } else {
            final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            operandName = selectedColumn.getName();
        }
        additionalColumns.add(ActionsUtils.additionalColumn()
                .withName(context.getColumnName() + " " + operator + " " + operandName)
                .withType(Type.DOUBLE));
        return additionalColumns;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
        if (context.getActionStatus() == OK) {
            checkParameters(context.getParameters(), context.getRowMetadata());
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

        // set new column value
        final String sourceValue = row.get(columnId);
        final String newValue = compute(sourceValue, operator, operand);
        row.set(ActionsUtils.getTargetColumnId(context), newValue);
    }

    protected String compute(final String stringOperandOne, final String operator, final String stringOperandTwo) {
        if (!NumericHelper.isBigDecimal(stringOperandOne) || !NumericHelper.isBigDecimal(stringOperandTwo)) {
            return StringUtils.EMPTY;
        }

        try {
            final BigDecimal operandOne = BigDecimalParser.toBigDecimal(stringOperandOne);
            final BigDecimal operandTwo = BigDecimalParser.toBigDecimal(stringOperandTwo);

            BigDecimal toReturn;

            final int scale = ConstantUtilMath.SCALE_PRECISION;
            final RoundingMode rm = ConstantUtilMath.ROUNDING_MODE;

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
        } catch (ArithmeticException | NumberFormatException | NullPointerException e) {
            LOGGER.debug("Unable to compute with operands {}, {} and operator {} due to exception {}.", stringOperandOne,
                    stringOperandTwo, operator, e);
            return StringUtils.EMPTY;
        } catch (Exception e) {
            LOGGER.debug("Unable to compute with operands {}, {} and operator {} due to an unknown exception {}.",
                    stringOperandOne, stringOperandTwo, operator, e);
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
