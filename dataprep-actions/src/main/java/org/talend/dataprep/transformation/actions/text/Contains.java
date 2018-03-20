// ============================================================================
//
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

package org.talend.dataprep.transformation.actions.text;

import static org.apache.commons.lang.BooleanUtils.toStringTrueFalse;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.COLUMN;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.util.*;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
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

/**
 * Create a new column with Boolean result <code>true</code> if the Levenstein distance is less or equals the parameter
 */
@Action(Contains.ACTION_NAME)
public class Contains extends AbstractActionMetadata implements ColumnAction, OtherColumnParameters {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "contains";

    /**
     * The column appendix.
     */
    public static final String APPENDIX = "_contains_";

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.STRINGS.getDisplayName(locale);
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
        public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);

        parameters.add(selectParameter(locale) //
                .name(MODE_PARAMETER) //
                .item(CONSTANT_MODE, CONSTANT_MODE,//
                        parameter(locale).setName(CONSTANT_VALUE)
                                .setType(STRING)
                                .setDefaultValue(EMPTY)
                                .build(this)) //
                .item(OTHER_COLUMN_MODE, OTHER_COLUMN_MODE,//
                        parameter(locale).setName(SELECTED_COLUMN_PARAMETER)
                                .setType(COLUMN)
                                .setDefaultValue(EMPTY)
                                .setCanBeBlank(false)
                                .build(this)) //
                .defaultValue(CONSTANT_MODE).build(this));

        return parameters;
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        final List<ActionsUtils.AdditionalColumn> additionalColumns = new ArrayList<>();

        final Map<String, String> parameters = context.getParameters();
        final RowMetadata rowMetadata = context.getRowMetadata();
        final String sourceColumnName = context.getColumnName();
        String prefix;
        if (parameters.get(MODE_PARAMETER).equals(OTHER_COLUMN_MODE)) {
            final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            prefix = selectedColumn.getName();
        } else {
            prefix = parameters.get(CONSTANT_VALUE);
        }
        additionalColumns.add(ActionsUtils.additionalColumn().withName(sourceColumnName + APPENDIX + prefix).withType(Type.BOOLEAN));

        return additionalColumns;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), true)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
        if (context.getActionStatus() == OK) {
            checkSelectedColumnParameter(context.getParameters(), context.getRowMetadata());
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final RowMetadata rowMetadata = context.getRowMetadata();
        Map<String, String> parameters = context.getParameters();

        final String containsColumn = ActionsUtils.getTargetColumnId(context);

        String value = row.get(context.getColumnId());
        String referenceValue;

        if (parameters.get(MODE_PARAMETER).equals(OTHER_COLUMN_MODE)) {
            final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            referenceValue = row.get(selectedColumn.getId());
        } else {
            referenceValue = parameters.get(CONSTANT_VALUE);
        }

        boolean contains = value.contains(referenceValue);
        row.set(containsColumn, toStringTrueFalse(contains));
    }

    /**
     * Check that the selected column parameter is correct in case we check contains with another column: defined in the
     * parameters and there's a matching column. If the parameter is invalid, an exception is thrown.
     *
     * @param parameters where to look the parameter value.
     * @param rowMetadata the row metadata where to look for the column.
     */
    private void checkSelectedColumnParameter(Map<String, String> parameters, RowMetadata rowMetadata) {
        if (!parameters.containsKey(MODE_PARAMETER)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                    MODE_PARAMETER));
        }
        if (parameters.get(MODE_PARAMETER).equals(OTHER_COLUMN_MODE)
                && (!parameters.containsKey(SELECTED_COLUMN_PARAMETER) || rowMetadata.getById(parameters
                        .get(SELECTED_COLUMN_PARAMETER)) == null)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                    SELECTED_COLUMN_PARAMETER));
        }
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) && (!parameters.containsKey(CONSTANT_VALUE))) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                    CONSTANT_VALUE));
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
