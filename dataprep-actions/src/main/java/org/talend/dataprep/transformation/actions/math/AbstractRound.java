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

import static org.talend.dataprep.parameters.ParameterType.INTEGER;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Abstract class for Math operation on {@link Type#NUMERIC} values
 */
public abstract class AbstractRound extends AbstractActionMetadata implements ColumnAction {

    /** Number of digit after the decimal symbol. */
    protected static final String PRECISION = "precision"; //$NON-NLS-1$

    @Override
    public String getCategory() {
        return ActionCategory.NUMBERS.getDisplayName();
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(PRECISION, INTEGER, "0"));
        return ActionsBundle.attachToAction(parameters, this);
    }

    @Override
    public void applyOnColumn(final DataSetRow row, final ActionContext context) {
        final String precisionAsString = context.getParameters().get(PRECISION);

        int precision = 0;

        try {
            precision = Integer.parseInt(precisionAsString);
        } catch (Exception e) {
            // Nothing to do, precision cannot be parsed to integer, in this case we keep 0
        }

        if (precision < 0) {
            precision = 0;
        }

        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        if (value == null) {
            return;
        }

        try {
            BigDecimal bd = BigDecimalParser.toBigDecimal(value);
            bd = bd.setScale(precision, getRoundingMode());
            row.set(columnId, String.valueOf(bd));
        } catch (NumberFormatException nfe2) {
            // Nan: nothing to do, but fail silently (no change in value)
        }
    }

    protected abstract RoundingMode getRoundingMode();

    @Override
    public boolean acceptField(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        // in order to 'clean' integer typed columns, this function needs to be allowed on any numeric types
        return Type.NUMERIC.isAssignableFrom(columnType);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }
}
