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

package org.talend.dataprep.transformation.actions.delete;

import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.transformation.actions.category.ScopeCategory.LINE;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.RowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Delete the line which id matches TdpId in context. This id/filtering is managed by ActionMetadata.
 */
@Action(Delete.DELETE_ACTION_NAME)
public class Delete extends AbstractActionMetadata implements RowAction {

    public static final String DELETE_ACTION_NAME = "delete";

    public static final String DELETE_SINGLE_LINE = "delete_single_line";

    public static final String DELETE_COLUMN = "delete_column";

    private final ScopeCategory scope;

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
    public String getDescription(Locale locale) {
        switch (scope) {
        case LINE:
            return ActionsBundle.actionDescription(this, locale, DELETE_SINGLE_LINE);
        case COLUMN:
            return ActionsBundle.actionDescription(this, locale, DELETE_COLUMN);
        default:
            return null;
        }
    }

    @Override
    public String getLabel(Locale locale) {
        switch (scope) {
        case LINE:
            return ActionsBundle.actionLabel(this, locale, DELETE_SINGLE_LINE);
        case COLUMN:
            return ActionsBundle.actionLabel(this, locale, DELETE_COLUMN);
        default:
            return null;
        }
    }

    @Override
    public String getCategory(Locale locale) {
        return DATA_CLEANSING.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public void applyOnLine(DataSetRow row, ActionContext context) {
        row.setDeleted(true);
    }

    @Override
    public ActionDefinition adapt(ScopeCategory scope) {
        return new Delete(scope);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_ALL, Behavior.FORBID_DISTRIBUTED, Behavior.VALUES_DELETE_ROWS);
    }
}
