// ============================================================================
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

import static org.talend.dataprep.transformation.actions.category.ActionCategory.FILTERED;

import java.util.EnumSet;
import java.util.Set;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

public abstract class AbstractFilteringAction extends AbstractActionMetadata implements ColumnAction {

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public boolean implicitFilter() {
        return false;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_ALL);
    }

    @Override
    public String getCategory() {
        return FILTERED.getDisplayName();
    }
}
