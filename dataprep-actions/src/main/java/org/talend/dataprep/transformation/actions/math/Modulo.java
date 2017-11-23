package org.talend.dataprep.transformation.actions.math;

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
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.util.NumericHelper;

import java.util.*;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + Modulo.MODULO_NAME)
public class Modulo extends AbstractActionMetadata implements ColumnAction, OtherColumnParameters {

    protected static final String MODULO_NAME = "modulo";

    /**
     * Mode: tells if operand is taken from another column or is a constant
     */
    public static final String MODE_PARAMETER = "mode";

    /** Number of the divisor. */
    protected static final String DIVISOR = "divisor";

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Modulo.class);

    @Override
    public String getName() {
        return MODULO_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.MATH.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        return Type.NUMERIC.isAssignableFrom(columnType);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);

        parameters.add(SelectParameter
                .selectParameter(locale)
                .name(MODE_PARAMETER)
                .item(CONSTANT_MODE, CONSTANT_MODE,
                        Parameter
                                .parameter(locale)
                                .setName(DIVISOR)
                                .setType(ParameterType.STRING)
                                .setDefaultValue("2")
                                .build(this))
                .item(OTHER_COLUMN_MODE, OTHER_COLUMN_MODE,
                        Parameter
                                .parameter(locale)
                                .setName(SELECTED_COLUMN_PARAMETER)
                                .setType(ParameterType.COLUMN)
                                .setDefaultValue(StringUtils.EMPTY)
                                .setCanBeBlank(false)
                                .build(this)) //
                .defaultValue(CONSTANT_MODE)
                .build(this));

        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            checkParameters(actionContext.getParameters(), actionContext.getRowMetadata());

            // Create column
            final Map<String, String> parameters = actionContext.getParameters();
            final String columnId = actionContext.getColumnId();
            final RowMetadata rowMetadata = actionContext.getRowMetadata();
            final ColumnMetadata sourceColumn = rowMetadata.getById(columnId);
            String divisorName;
            if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
                if (actionContext.getParameters().get(DIVISOR).isEmpty()) {
                    LOGGER.debug("miss parameter: " + DIVISOR + " is empty.");
                    actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
                }
                divisorName = parameters.get(DIVISOR);
            } else {
                final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
                divisorName = selectedColumn.getName();
            }
            actionContext.column("result", r -> {
                final ColumnMetadata c = ColumnMetadata.Builder //
                        .column() //
                        .name(sourceColumn.getName() + " % " + divisorName) //
                        .type(Type.DOUBLE) //
                        .build();
                rowMetadata.insertAfter(columnId, c);
                return c;
            });
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {

        final Map<String, String> parameters = context.getParameters();
        final String columnId = context.getColumnId();
        final RowMetadata rowMetadata = context.getRowMetadata();
        final String value = row.get(columnId);
        String divisor;
        final String newColumnId = context.column("result");

        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            divisor = parameters.get(DIVISOR);
        } else {
            final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            divisor = row.get(selectedColumn.getId());
        }

        if (NumericHelper.isBigDecimal(value) && NumericHelper.isBigDecimal(divisor)) {
            row.set(newColumnId,
                    BigDecimalParser
                            .toBigDecimal(value)
                            .remainder(BigDecimalParser.toBigDecimal(divisor))
                            .abs()
                            .toString());
        } else {
            LOGGER.trace("value or " + DIVISOR + " is not a number.");
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
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) && !parameters.containsKey(DIVISOR)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put("paramName", DIVISOR));
        } else if (!parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)
                && (!parameters.containsKey(SELECTED_COLUMN_PARAMETER)
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
