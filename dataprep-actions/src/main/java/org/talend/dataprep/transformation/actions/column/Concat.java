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

package org.talend.dataprep.transformation.actions.column;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.COLUMN;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.exception.error.ActionErrorCodes;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Concat action concatenates 2 columns into a new one. The new column name will be "column_source + selected_column."
 * The new column content is "prefix + column_source + separator + selected_column + suffix"
 */
@Action(Concat.CONCAT_ACTION_NAME)
public class Concat extends AbstractActionMetadata implements ColumnAction, OtherColumnParameters {

    /**
     * The action name.
     */
    public static final String CONCAT_ACTION_NAME = "concat"; //$NON-NLS-1$

    /**
     * The optional new column prefix content.
     */
    public static final String PREFIX_PARAMETER = "prefix"; //$NON-NLS-1$

    /**
     * The optional new column separator.
     */
    public static final String SEPARATOR_PARAMETER = "concat_separator"; //$NON-NLS-1$

    /**
     * The optional new column suffix content.
     */
    public static final String SUFFIX_PARAMETER = "suffix"; //$NON-NLS-1$

    /**
     * The separator use in the new column name.
     */
    public static final String COLUMN_NAMES_SEPARATOR = "_"; //$NON-NLS-1$

    /**
     * The parameter used to defined the strategy to add or not the separator
     */
    public static final String SEPARATOR_CONDITION = "concat_separator_condition";

    public static final String BOTH_NOT_EMPTY = "concat_both_not_empty";

    public static final String ALWAYS = "concat_always";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return CONCAT_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.COLUMNS.getDisplayName(locale);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));

        parameters.add(parameter(locale).setName(PREFIX_PARAMETER)
                .setType(STRING)
                .setDefaultValue(EMPTY)
                .build(this));

        parameters.add(selectParameter(locale).name(MODE_PARAMETER)
                .item(OTHER_COLUMN_MODE, OTHER_COLUMN_MODE, parameter(locale).setName(SELECTED_COLUMN_PARAMETER)
                                .setType(COLUMN)
                                .setDefaultValue(EMPTY)
                                .setCanBeBlank(false)
                                .build(this), parameter(locale).setName(SEPARATOR_PARAMETER)
                                .setType(STRING)
                                .setDefaultValue(EMPTY)
                                .build(this), //
                        selectParameter(locale) //
                                .name(SEPARATOR_CONDITION) //
                                .item(BOTH_NOT_EMPTY, BOTH_NOT_EMPTY) //
                                .item(ALWAYS, ALWAYS) //
                                .defaultValue(BOTH_NOT_EMPTY) //
                                .build(this))//
                .item(CONSTANT_MODE, CONSTANT_MODE) //
                .defaultValue(OTHER_COLUMN_MODE) //
                .build(this));

        parameters.add(parameter(locale).setName(SUFFIX_PARAMETER)
                .setType(STRING)
                .setDefaultValue(EMPTY)
                .build(this));
        return parameters;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        // accept all types of columns
        return true;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
        if (context.getActionStatus() == OK) {
            checkSelectedColumnParameter(context.getParameters(), context.getRowMetadata());
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final RowMetadata rowMetadata = context.getRowMetadata();
        final String columnId = context.getColumnId();
        final Map<String, String> parameters = context.getParameters();

        String separatorCondition = parameters.get(SEPARATOR_CONDITION);

        // Set new column value
        String sourceValue = row.get(columnId);
        // 64 should be ok for most of values
        StringBuilder newValue = new StringBuilder(64);
        newValue.append(getParameter(parameters, PREFIX_PARAMETER, StringUtils.EMPTY));
        newValue.append(StringUtils.isBlank(sourceValue) ? StringUtils.EMPTY : sourceValue);

        if (parameters.get(MODE_PARAMETER).equals(OTHER_COLUMN_MODE)) {
            ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            String selectedColumnValue = row.get(selectedColumn.getId());

            // both not empty is default
            boolean addSeparator = StringUtils.equals(separatorCondition, ALWAYS) //
                    || ((StringUtils.equals(separatorCondition, BOTH_NOT_EMPTY) || StringUtils.isBlank(separatorCondition)) //
                            && StringUtils.isNotBlank(sourceValue) //
                            && StringUtils.isNotBlank(selectedColumnValue) //
            );

            if (addSeparator) {
                newValue.append(getParameter(parameters, SEPARATOR_PARAMETER, StringUtils.EMPTY));
            }
            if (StringUtils.isNotBlank(selectedColumnValue)) {
                newValue.append(selectedColumnValue);
            }

        }

        newValue.append(getParameter(parameters, SUFFIX_PARAMETER, StringUtils.EMPTY));

        row.set(ActionsUtils.getTargetColumnId(context), newValue.toString());
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        String result;
        ColumnMetadata selectedColumn = context.getRowMetadata().getById(context.getParameters().get(SELECTED_COLUMN_PARAMETER));
        String sourceColumnName = context.getColumnName();
        final Map<String, String> parameters = context.getParameters();
        final String prefix = getParameter(parameters, PREFIX_PARAMETER, StringUtils.EMPTY);
        final String suffix = getParameter(parameters, SUFFIX_PARAMETER, StringUtils.EMPTY);

        if (parameters.get(MODE_PARAMETER).equals(OTHER_COLUMN_MODE)) {
            result = sourceColumnName + COLUMN_NAMES_SEPARATOR + selectedColumn.getName();
        } else {
            result = prefix + sourceColumnName + suffix;
        }
        return singletonList(ActionsUtils.additionalColumn().withName(result));
    }

    /**
     * Check that the selected column parameter is correct in case we concatenate with another column: defined in the
     * parameters and there's a matching column. If the parameter is invalid, an exception is thrown.
     *
     * @param parameters where to look the parameter value.
     * @param rowMetadata the row metadata where to look for the column.
     */
    private void checkSelectedColumnParameter(Map<String, String> parameters, RowMetadata rowMetadata) {
        if (parameters.get(MODE_PARAMETER).equals(OTHER_COLUMN_MODE) && (!parameters.containsKey(SELECTED_COLUMN_PARAMETER)
                || rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER)) == null)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put("paramName", SELECTED_COLUMN_PARAMETER));
        }
    }

    /**
     * Return the parameter value or the default value if not found.
     *
     * @param parameters where to look.
     * @param parameterName the parameter name.
     * @param defaultValue the value to return if the parameter value is null or not found.
     * @return the parameter value or the default value if null or not found.
     */
    private String getParameter(Map<String, String> parameters, String parameterName, String defaultValue) {
        String value = parameters.get(parameterName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
