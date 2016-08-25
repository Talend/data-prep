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
package org.talend.dataprep.transformation.actions.math;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * This will compute the absolute value for numerical columns.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + Absolute.ABSOLUTE_ACTION_NAME)
public class Absolute extends AbstractActionMetadata implements ColumnAction {

    public static final String ABSOLUTE_ACTION_NAME = "absolute"; //$NON-NLS-1$

    private final Type type;

    public Absolute() {
        type = Type.INTEGER;
    }

    public Absolute(Type type) {
        this.type = type;
    }

    /**
     * Try to parse and return the absolute value of a long value as string
     *
     * @param value The value to execute action
     * @return the absolute value or null
     */
    private String executeOnLong(final String value) {
        try {
            long longValue = Long.parseLong(value);
            return Long.toString(Math.abs(longValue));
        } catch (NumberFormatException nfe1) {
            return null;
        }
    }

    /**
     * Try to parse and return the absolute value of a long value as string
     *
     * @param value The value to execute action
     * @return the absolute value or null
     */
    private String executeOnFloat(final String value) {
        try {
            BigDecimal bd = BigDecimalParser.toBigDecimal(value);
            return bd.abs().toPlainString();
        } catch (NumberFormatException nfe2) {
            return null;
        }
    }

    @Override
    public String getName() {
        return ABSOLUTE_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.MATH.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.FLOAT.equals(Type.get(column.getType())) //
                || Type.DOUBLE.equals(Type.get(column.getType())) //
                || Type.INTEGER.equals(Type.get(column.getType()));
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        if (value == null) {
            return;
        }
        String absValueStr = null;
        switch (type) {
        case INTEGER:
            absValueStr = executeOnLong(value);
            if (absValueStr == null) {
                absValueStr = executeOnFloat(value);
            }
            break;
        case DOUBLE:
        case FLOAT:
            absValueStr = executeOnFloat(value);
            if (absValueStr == null) {
                absValueStr = executeOnLong(value);
            }
            break;
        }
        if (absValueStr != null) {
            row.set(columnId, absValueStr);
        }
    }

    @Override
    public ActionDefinition adapt(ColumnMetadata column) {
        if (column == null || !acceptField(column)) {
            return this;
        }
        return new Absolute(type);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
