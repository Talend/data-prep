package org.talend.dataprep.transformation.api.action.metadata.math;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

/**
 * Concat action concatenates 2 columns into a new one. The new column name will be "column_source + selected_column."
 * The new column content is "prefix + column_source + separator + selected_column + suffix"
 */
@Component(NumericOperations.ACTION_BEAN_PREFIX + NumericOperations.ACTION_NAME)
public class NumericOperations extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "numeric_ops"; //$NON-NLS-1$

    /**
     * Mode: tells if operand is taken from another column or is a constant
     */
    public static final String MODE_PARAMETER = "mode"; //$NON-NLS-1$

    /**
     * The selected column id.
     */
    public static final String SELECTED_COLUMN_PARAMETER = "selected_column"; //$NON-NLS-1$

    /**
     * The operator to use.
     */
    public static final String OPERATOR_PARAMETER = "operator"; //$NON-NLS-1$

    /**
     * The operand to use.
     */
    public static final String OPERAND_PARAMETER = "operand"; //$NON-NLS-1$

    /**
     * Constant to represents mode where we compute against a constant.
     */
    public static final String CONSTANT_MODE = "Constant";

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.MATH.getDisplayName();
    }

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(OPERATOR_PARAMETER)
                        .item("+")
                        .item("x")
                        .item("-")
                        .item("/")
                        .defaultValue("x")
                        .build()
        );
        //@formatter:on

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(MODE_PARAMETER)
                        .item(CONSTANT_MODE, new Parameter(OPERAND_PARAMETER, ParameterType.INTEGER, "2"))
                        .item("Another column", new Parameter(SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, StringUtils.EMPTY, false, false))
                        .defaultValue(CONSTANT_MODE)
                        .build()
        );
        //@formatter:on

        return parameters;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    public boolean acceptColumn(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        return Type.NUMERIC.isAssignableFrom(columnType);
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata sourceColumn = rowMetadata.getById(columnId);

        checkSelectedColumnParameter(parameters, row);

        String operator = parameters.get(OPERATOR_PARAMETER);

        String operand = null;
        String operandName = null;
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            operand = parameters.get(OPERAND_PARAMETER);
            operandName = operand;
        } else {
            ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            operand = row.get(selectedColumn.getId());
            operandName = selectedColumn.getName();
        }

        ColumnMetadata newColumn = createNewColumn(sourceColumn, operator, operandName);
        String newColumnId = rowMetadata.insertAfter(columnId, newColumn);

        // Set new column value
        String sourceValue = row.get(columnId);

        String newValue = compute(sourceValue, operator, operand);

        row.set(newColumnId, newValue);
    }

    protected String compute(String operand_1_string, String operator, String operand_2_string) {
        try {
            BigDecimal operand_1 = new BigDecimal(operand_1_string);
            BigDecimal operand_2 = new BigDecimal(operand_2_string);

            switch (operator) {
            case "+":
                return operand_1.add(operand_2).toString();
            case "x":
                return operand_1.multiply(operand_2).toString();
            case "-":
                return operand_1.subtract(operand_2).toString();
            case "/":
                return operand_1.divide(operand_2).toString();
            default:
                return "";
            }
        } catch (NumberFormatException | ArithmeticException | NullPointerException e) {
            return "";
        }
    }

    /**
     * Check that the selected column parameter is correct : defined in the parameters and there's a matching column. If
     * the parameter is invalid, an exception is thrown.
     *
     * @param parameters where to look the parameter value.
     * @param row the row where to look for the column.
     */
    private void checkSelectedColumnParameter(Map<String, String> parameters, DataSetRow row) {
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            if (!parameters.containsKey(OPERAND_PARAMETER)) {
                throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                        OPERAND_PARAMETER));
            }
        } else {
            if (!parameters.containsKey(SELECTED_COLUMN_PARAMETER)
                    || row.getRowMetadata().getById(parameters.get(SELECTED_COLUMN_PARAMETER)) == null) {
                throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                        SELECTED_COLUMN_PARAMETER));
            }
        }
    }

    /**
     * Create the new result column
     */
    private ColumnMetadata createNewColumn(ColumnMetadata sourceColumn, String operator, String operand) {
        return ColumnMetadata.Builder //
                .column() //
                .name(sourceColumn.getName() + " " + operator + " " + operand) //
                .type(Type.NUMERIC) //
                .build();
    }
}
