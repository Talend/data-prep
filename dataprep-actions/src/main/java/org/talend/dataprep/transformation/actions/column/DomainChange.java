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

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Change the domain of a column. <b>This action is not displayed in the UI it's here to ease recording it as a Step
 * It's available from column headers</b>
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + DomainChange.DOMAIN_CHANGE_ACTION_NAME)
public class DomainChange extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String DOMAIN_CHANGE_ACTION_NAME = "domain_change"; //$NON-NLS-1$

    public static final String NEW_DOMAIN_ID_PARAMETER_KEY = "new_domain_id";

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainChange.class);

    @Override
    public String getName() {
        return DOMAIN_CHANGE_ACTION_NAME;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public String getCategory() {
        return ActionCategory.COLUMN_METADATA.getDisplayName();
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final Map<String, String> parameters = context.getParameters();
        LOGGER.debug("DomainChange for columnId {} with parameters {} ", columnId, parameters);
        final RowMetadata rowMetadata = context.getRowMetadata();
        final ColumnMetadata columnMetadata = rowMetadata.getById(columnId);
        final String newDomainId = parameters.get(NEW_DOMAIN_ID_PARAMETER_KEY);
        if (StringUtils.isNotEmpty(newDomainId)) {
            columnMetadata.setDomain(newDomainId);
            columnMetadata.setDomainFrequency(0);
            columnMetadata.setDomainForced(true);
        }
        rowMetadata.update(columnId, columnMetadata);
        context.setActionStatus(ActionContext.ActionStatus.DONE);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CHANGE_TYPE);
    }
}
