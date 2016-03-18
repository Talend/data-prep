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

package org.talend.dataprep.transformation.api.action.metadata.delete;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory.LINE;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.RowAction;

/**
 * Delete the line which id matches TdpId in context. This id/filtering is managed by ActionMetadata.
 */
@Component(Delete.ACTION_BEAN_PREFIX + Delete.DELETE_ACTION_NAME)
@Scope(value = "prototype")
public class Delete extends ActionMetadata implements RowAction {

    public static final String DELETE_ACTION_NAME = "delete";
    public static final String DELETE_SINGLE_LINE = "delete_single_line";
    public static final String DELETE_COLUMN = "delete_column";
    private final ScopeCategory scope;

    @Autowired
    private ApplicationContext applicationContext;

    public Delete() {
        this(LINE);
    }

    public Delete(final ScopeCategory scope) {
        this.scope = scope;
    }

    @Override
    public String getName() {
        return DELETE_ACTION_NAME;
    }

    @Override
    public String getDescription() {
        switch (scope) {
            case LINE:
                return getMessagesBundle().getString("action." + DELETE_SINGLE_LINE + ".desc");
            case COLUMN:
                return getMessagesBundle().getString("action." + DELETE_COLUMN + ".desc");
        default:
            return null;
        }
    }

    @Override
    public String getLabel() {
        switch (scope) {
            case LINE:
                return getMessagesBundle().getString("action." + DELETE_SINGLE_LINE + ".label");
            case COLUMN:
                return getMessagesBundle().getString("action." + DELETE_COLUMN + ".label");
        default:
            return null;
        }
    }

    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    @Override
    public void applyOnLine(DataSetRow row, ActionContext context) {
        row.setDeleted(true);
    }

    @Override
    public ActionMetadata adapt(ScopeCategory scope) {
        return applicationContext.getBean(getClass(), scope);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_ALL);
    }
}
