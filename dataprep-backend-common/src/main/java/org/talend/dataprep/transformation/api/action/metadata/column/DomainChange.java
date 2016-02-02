//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.column;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Change the domain of a column. <b>This action is not displayed in the UI it's here to ease recording it as a Step
 * It's available from column headers</b>
 */
@Component(DomainChange.ACTION_BEAN_PREFIX + DomainChange.DOMAIN_CHANGE_ACTION_NAME)
public class DomainChange extends ActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String DOMAIN_CHANGE_ACTION_NAME = "domain_change"; //$NON-NLS-1$

    public static final String NEW_DOMAIN_ID_PARAMETER_KEY = "new_domain_id";

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainChange.class);

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return DOMAIN_CHANGE_ACTION_NAME;
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
        final ColumnMetadata columnMetadata = row.getRowMetadata().getById(columnId);
        final String newDomainId = parameters.get(NEW_DOMAIN_ID_PARAMETER_KEY);
        if (StringUtils.isNotEmpty(newDomainId)) {
            columnMetadata.setDomain(newDomainId);
            columnMetadata.setDomainFrequency(0);
            columnMetadata.setDomainForced(true);
        }
        context.setActionStatus(ActionContext.ActionStatus.DONE);
    }

    @Override
    public ActionMetadata adapt(ColumnMetadata column) {
        return this;
    }
}
