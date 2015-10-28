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
import java.math.RoundingMode;
import java.util.Map;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Abstract class for Math operation on {@link Type#NUMERIC} values
 */
public abstract class AbstractRound extends ActionMetadata implements ColumnAction {

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.MATH.getDisplayName();
    }

    @Override
    public void applyOnColumn(final DataSetRow row, final TransformationContext context, final Map<String, String> parameters,
            final String columnId) {
        final String value = row.get(columnId);
        if (value == null) {
            return;
        }

        try {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(0, getRoundingMode());
            long result = bd.longValue();
            row.set(columnId, String.valueOf(result));
        } catch (NumberFormatException nfe2) {
            // Nan: nothing to do, but fail silently (no change in value)
        }
    }

    protected abstract RoundingMode getRoundingMode();

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        // in order to 'clean' integer typed columns, this function needs to be allowed on any numeric types
        return Type.NUMERIC.isAssignableFrom(columnType);
    }
}
