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

package org.talend.dataprep.transformation.actions.bool;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Negate a boolean.
 *
 * @see Negate
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + Negate.NEGATE_ACTION_NAME)
public class Negate extends AbstractActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
    static final String NEGATE_ACTION_NAME = "negate";

    @Override
    public String getName() {
        return NEGATE_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.BOOLEAN.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.BOOLEAN.equals(Type.get(column.getType()));
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        if (isBoolean(value)) {
            final Boolean boolValue = Boolean.valueOf(value);
            row.set(columnId, WordUtils.capitalizeFully("" + !boolValue));
        }
    }

    private boolean isBoolean(final String value) {
        return value != null && ("true".equalsIgnoreCase(value.trim()) || "false".equalsIgnoreCase(value.trim()));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }
}
