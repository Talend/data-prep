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
package org.talend.dataprep.transformation.actions.fill;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Generate a sequence on a column based on start value and step value.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + GenerateSequence.ACTION_NAME)
public class GenerateSequence extends AbstractActionMetadata implements ColumnAction {

    public static final String ACTION_NAME = "generate_a_sequence";
    /** The starting value of sequence.*/
    protected static final String START_VALUE = "start_value";

    /** The step value of sequence.*/
    protected static final String STEP_VALUE = "step_value";

    @Override
    public String getName() {
        return GenerateSequence.ACTION_NAME;
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        Parameter startParameter = new Parameter(START_VALUE, ParameterType.INTEGER, "1");
        parameters.add(startParameter);
        Parameter stepParameter = new Parameter(STEP_VALUE, ParameterType.INTEGER, "1");
        parameters.add(stepParameter);
        return ActionsBundle.attachToAction(parameters, this);
    }

    @Override
    public String getCategory() {
        return ActionCategory.NUMBERS.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return  true;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN, Behavior.FORBID_DISTRIBUTED);
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        String startValue = context.getParameters().get(START_VALUE);
        String stepValue = context.getParameters().get(STEP_VALUE);
        if (startValue.isEmpty() || stepValue.isEmpty()) {
            return;
        }
        final String columnId = context.getColumnId();
        BigInteger start = new BigInteger(startValue);
        BigInteger step = new BigInteger(stepValue);
        BigInteger currentValue = start.add(step.multiply(BigInteger.valueOf(row.getTdpId() - 1)));
        row.set(columnId, currentValue.toString());
    }
}
