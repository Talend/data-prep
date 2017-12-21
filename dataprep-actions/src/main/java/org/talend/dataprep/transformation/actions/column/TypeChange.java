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
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.talend.dataprep.transformation.actions.category.ActionScope.HIDDEN_IN_ACTION_LIST;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.DONE;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Change the type of a column <b>This action is not displayed in the UI it's here to ease recording it as a Step It's
 * available from column headers</b>
 */
@Action(TypeChange.TYPE_CHANGE_ACTION_NAME)
public class TypeChange extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String TYPE_CHANGE_ACTION_NAME = "type_change"; //$NON-NLS-1$

    public static final String NEW_TYPE_PARAMETER_KEY = "new_type";

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeChange.class);

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return TYPE_CHANGE_ACTION_NAME;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.COLUMN_METADATA.getDisplayName(locale);
    }

    @Override
    public List<String> getActionScope() {
        return singletonList(HIDDEN_IN_ACTION_LIST.getDisplayName());
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, singletonList(ActionsUtils.additionalColumn()));
        }
        if (context.getActionStatus() == OK) {
            final String columnId = context.getColumnId();
            final Map<String, String> parameters = context.getParameters();
            LOGGER.debug("TypeChange for columnId {} with parameters {} ", columnId, parameters);
            final RowMetadata rowMetadata = context.getRowMetadata();
            final ColumnMetadata columnMetadata = rowMetadata.getById(columnId);
            final String newType = parameters.get(NEW_TYPE_PARAMETER_KEY);
            if (isNotEmpty(newType)) {
                columnMetadata.setType(newType);
                columnMetadata.setTypeForced(true);
                // erase domain
                columnMetadata.setDomain("");
                columnMetadata.setDomainLabel("");
                columnMetadata.setDomainFrequency(0);
                // We must set this to fix TDP-838: we force the domain to empty
                columnMetadata.setDomainForced(true);
            }
            rowMetadata.update(columnId, columnMetadata);
            context.setActionStatus(DONE);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CHANGE_TYPE);
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        // Nothing to do.
    }
}
