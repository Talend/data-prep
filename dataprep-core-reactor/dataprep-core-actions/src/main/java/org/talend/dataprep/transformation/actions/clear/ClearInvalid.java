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

package org.talend.dataprep.transformation.actions.clear;

import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.transformation.actions.category.ActionScope.INVALID;

import java.util.*;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Clear cell when value is invalid.
 */
@Action(ClearInvalid.ACTION_NAME)
public class ClearInvalid extends AbstractClear implements ColumnAction {

    /** the action name. */
    public static final String ACTION_NAME = "clear_invalid"; //$NON-NLS-1$

    private static final List<String> ACTION_SCOPE = Collections.singletonList(INVALID.getDisplayName());

    @Override
    public String getName() {
        return ACTION_NAME;
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
    public List<String> getActionScope() {
        return ACTION_SCOPE;
    }

    @Override
    public boolean toClear(DataSetRow dataSetRow, String columnId, ActionContext actionContext) {
        return dataSetRow.isInvalid(columnId);
    }

    @Override
    public Set<Behavior> getBehavior() {
        final EnumSet<Behavior> behaviors = EnumSet.copyOf(super.getBehavior());
        behaviors.add(Behavior.NEED_STATISTICS_INVALID);
        return behaviors;
    }
}
