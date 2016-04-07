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

package org.talend.dataprep.transformation.api.action.metadata.column;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;

/**
 * Swap columns values
 */
@Component(Swap.ACTION_BEAN_PREFIX + Swap.SWAP_COLUMN_ACTION_NAME)
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Swap extends ActionMetadata implements ColumnAction, OtherColumnParameters {

    /**
     * The action name.
     */
    public static final String SWAP_COLUMN_ACTION_NAME = "swap_column"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(Swap.class);

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return SWAP_COLUMN_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.COLUMNS.getDisplayName();
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameters = super.getParameters();

        parameters.add(new Parameter(SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, StringUtils.EMPTY, false, false));

        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);

        Map<String, String> parameters = actionContext.getParameters();

        RowMetadata rowMetadata = actionContext.getRowMetadata();

        ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));

        if (selectedColumn == null) {
            return;
        }

        String domain = selectedColumn.getDomain();
        String type = selectedColumn.getType();

        String columnId = parameters.get(ImplicitParameters.COLUMN_ID.getKey());

        ColumnMetadata originColumn = rowMetadata.getById(columnId);

        selectedColumn.setDomain(originColumn.getDomain());
        selectedColumn.setType(originColumn.getType());

        originColumn.setDomain(domain);
        originColumn.setType(type);
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
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
        return EnumSet.of(Behavior.VALUES_COLUMN, Behavior.METADATA_CHANGE_TYPE);
    }

}
