package org.talend.dataprep.transformation.api.action.metadata.math;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class NumericOperations extends ActionMetadata implements ColumnAction {
    private static final String PLUS = "+";
    private static final String MINUS = "-";
    private static final String MULTIPLY = "x";
    private static final String DIVIDE = "/";

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
    public void applyOnColumn(final DataSetRow row, final TransformationContext context, final Map<String, String> parameters, final String columnId) {
        checkParameters(parameters, row);

        final RowMetadata rowMetadata = row.getRowMetadata();
        final ColumnMetadata sourceColumn = rowMetadata.getById(columnId);

        // extract transformation parameters
        final String operator = parameters.get(OPERATOR_PARAMETER);
        String operand;
        String operandName;
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            operand = parameters.get(OPERAND_PARAMETER);
            operandName = operand;
        }
        else {
            final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            operand = row.get(selectedColumn.getId());
            operandName = selectedColumn.getName();
        }

        // column creation
        final ColumnMetadata newColumn = createNewColumn(sourceColumn, operator, operandName);
        final String newColumnId = rowMetadata.insertAfter(columnId, newColumn);

        // set new column value
        final String sourceValue = row.get(columnId);
        final String newValue = compute(sourceValue, operator, operand);
        row.set(newColumnId, newValue);
    }

    protected String compute(final String operand_1_string, final String operator, final String operand_2_string) {
        try {
            final BigDecimal operand_1 = new BigDecimal(operand_1_string);
            final BigDecimal operand_2 = new BigDecimal(operand_2_string);

            BigDecimal toReturn;

            final int scale = 2;
            final RoundingMode rm = HALF_UP;

            switch (operator) {
                case PLUS:
                    toReturn = operand_1.add(operand_2);
                    break;
                case MULTIPLY:
                    toReturn = operand_1.multiply(operand_2);
                    break;
                case MINUS:
                    toReturn = operand_1.subtract(operand_2);
                    break;
                case DIVIDE:
                    toReturn = operand_1.divide(operand_2, scale, rm);
                    break;
                default:
                    return "";
            }

            // Format result:
            return toReturn.setScale(scale, rm).stripTrailingZeros().toPlainString();
        }
        catch (NumberFormatException | ArithmeticException | NullPointerException e) {
            return "";
        }
    }

    /**
     * Check that the selected column parameter is correct : defined in the parameters and there's a matching column. If
     * the parameter is invalid, an exception is thrown.
     *
     * @param parameters where to look the parameter value.
     * @param row        the row where to look for the column.
     */
    private void checkParameters(Map<String, String> parameters, DataSetRow row) {
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) && !parameters.containsKey(OPERAND_PARAMETER)) {
            throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                    OPERAND_PARAMETER));
        }
        else if (!parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) &&
                (!parameters.containsKey(SELECTED_COLUMN_PARAMETER) || row.getRowMetadata().getById(parameters.get(SELECTED_COLUMN_PARAMETER)) == null)) {
            throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                    SELECTED_COLUMN_PARAMETER));
        }
    }

    /**
     * Create the new result column
     */
    private ColumnMetadata createNewColumn(ColumnMetadata sourceColumn, String operator, String operand) {
        return ColumnMetadata.Builder //
                .column() //
                .name(sourceColumn.getName() + " " + operator + " " + operand) //
                .type(Type.DOUBLE) //
                .build();
    }
}
