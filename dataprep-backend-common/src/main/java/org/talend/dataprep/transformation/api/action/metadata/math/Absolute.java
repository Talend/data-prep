// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.transformation.api.action.metadata.math;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * This will compute the absolute value for numerical columns.
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + Absolute.ABSOLUTE_ACTION_NAME)
public class Absolute extends ActionMetadata implements ColumnAction {

    public static final String ABSOLUTE_ACTION_NAME = "absolute"; //$NON-NLS-1$

    private static final String ABSOLUTE_INT_ACTION_NAME = "absolute_int"; //$NON-NLS-1$

    private static final String ABSOLUTE_FLOAT_ACTION_NAME = "absolute_float"; //$NON-NLS-1$

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
            BigDecimal bd = new BigDecimal(value);
            return bd.abs().toPlainString();
        } catch (NumberFormatException nfe2) {
            return null;
        }
    }

    @Override
    public String getLabel() {
        switch (type) {
        case INTEGER:
            return MessagesBundle.getString("action." + ABSOLUTE_INT_ACTION_NAME + ".label");
        case DOUBLE:
        case FLOAT:
            return MessagesBundle.getString("action." + ABSOLUTE_FLOAT_ACTION_NAME + ".label");
        default:
            throw new UnsupportedOperationException("Type '" + type + "' is not supported.");
        }
    }

    @Override
    public String getDescription() {
        switch (type) {
        case INTEGER:
            return MessagesBundle.getString("action." + ABSOLUTE_INT_ACTION_NAME + ".desc");
        case DOUBLE:
        case FLOAT:
            return MessagesBundle.getString("action." + ABSOLUTE_FLOAT_ACTION_NAME + ".desc");
        default:
            throw new UnsupportedOperationException("Type '" + type + "' is not supported.");
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
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.FLOAT.equals(Type.get(column.getType())) //
                || Type.DOUBLE.equals(Type.get(column.getType())) //
                || Type.INTEGER.equals(Type.get(column.getType()));
    }

    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
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
    public ActionMetadata adapt(ColumnMetadata column) {
        if (column == null || !acceptColumn(column)) {
            return this;
        }
        return new Absolute(type);
    }
}
