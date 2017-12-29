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
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.COLUMN_ID;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Swap columns values
 */
@Action(Swap.SWAP_COLUMN_ACTION_NAME)
public class Swap extends AbstractActionMetadata implements ColumnAction, OtherColumnParameters {

    /**
     * The action name.
     */
    public static final String SWAP_COLUMN_ACTION_NAME = "swap_column"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(Swap.class);

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return SWAP_COLUMN_ACTION_NAME;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.COLUMNS.getDisplayName(locale);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        List<Parameter> parameters = super.getParameters(locale);

        parameters.add(Parameter.parameter(locale).setName(SELECTED_COLUMN_PARAMETER)
                .setType(ParameterType.COLUMN)
                .setDefaultValue(StringUtils.EMPTY)
                .setCanBeBlank(false)
                .build(this));

        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(actionContext, singletonList(ActionsUtils.additionalColumn()));
        }

        Map<String, String> parameters = actionContext.getParameters();

        RowMetadata rowMetadata = actionContext.getRowMetadata();

        ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));

        if (selectedColumn == null) {
            return;
        }

        String domain = selectedColumn.getDomain();
        String type = selectedColumn.getType();

        String columnId = parameters.get(COLUMN_ID.getKey());

        ColumnMetadata originColumn = rowMetadata.getById(columnId);

        selectedColumn.setDomain(originColumn.getDomain());
        selectedColumn.setType(originColumn.getType());

        originColumn.setDomain(domain);
        originColumn.setType(type);
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        RowMetadata rowMetadata = context.getRowMetadata();
        Map<String, String> parameters = context.getParameters();

        ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));

        if (selectedColumn == null) {
            return;
        }

        final String columnId = context.getColumnId();

        LOGGER.debug("swapping columns {} <-> {}", columnId, selectedColumn.getId());

        String columnValue = row.get(columnId);
        String selectedColumnValue = row.get(selectedColumn.getId());

        row.set(columnId, selectedColumnValue == null ? StringUtils.EMPTY : selectedColumnValue);
        row.set(selectedColumn.getId(), columnValue == null ? StringUtils.EMPTY : columnValue);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_MULTIPLE_COLUMNS, Behavior.METADATA_CHANGE_TYPE, Behavior.METADATA_CHANGE_ROW);
    }

}
