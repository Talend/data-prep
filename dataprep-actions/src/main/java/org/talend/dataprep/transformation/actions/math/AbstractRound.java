// ============================================================================
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

package org.talend.dataprep.transformation.actions.math;

import static java.util.Collections.singletonList;
import static org.talend.dataprep.api.type.Type.DOUBLE;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.INTEGER;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.util.NumericHelper;

/**
 * Abstract class for Math operation on {@link Type#NUMERIC} values
 */
public abstract class AbstractRound extends AbstractActionMetadata implements ColumnAction {

    /** Number of digit after the decimal symbol. */
    protected static final String PRECISION = "precision"; //$NON-NLS-1$

    protected static  final String NEW_COLUMN_SUFFIX = "_rounded";
    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.NUMBERS.getDisplayName(locale);
    }

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));
        if (hasPrecisionField()) {
            parameters.add(parameter(locale).setName(PRECISION).setType(INTEGER).setDefaultValue("0").build(this));
        }
        return parameters;
    }

    protected boolean hasPrecisionField() {
        return true;
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
        if (NumericHelper.isBigDecimal(value)) {
            BigDecimal bd = BigDecimalParser.toBigDecimal(value);
            bd = bd.setScale(precision, getRoundingMode());
            row.set(ActionsUtils.getTargetColumnId(context), String.valueOf(bd));
        }
    }

    protected abstract RoundingMode getRoundingMode();

    @Override
    public boolean acceptField(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        // in order to 'clean' integer typed columns, this function needs to be allowed on any numeric types
        return Type.NUMERIC.isAssignableFrom(columnType);
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(ActionsUtils.additionalColumn().withName(context.getColumnName() + NEW_COLUMN_SUFFIX).withType(DOUBLE));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }
}
