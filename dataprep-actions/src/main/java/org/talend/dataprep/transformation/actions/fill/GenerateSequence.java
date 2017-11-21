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

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Generate a sequence on a column based on start value and step value.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + GenerateSequence.ACTION_NAME)
public class GenerateSequence extends AbstractActionMetadata implements ColumnAction {

    public static final String ACTION_NAME = "generate_a_sequence";

    /** The next value of sequence to calculate */
    protected static final String SEQUENCE = "sequence"; //$NON-NLS-1$

    /** The starting value of sequence */
    protected static final String START_VALUE = "start_value";

    /** The step value of sequence */
    protected static final String STEP_VALUE = "step_value";

    /** Class logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateSequence.class);

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
        return true;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN, Behavior.FORBID_DISTRIBUTED);
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        Map<String, String> parameters = actionContext.getParameters();
        if (StringUtils.isEmpty(parameters.get(START_VALUE)) || StringUtils.isEmpty(parameters.get(STEP_VALUE))) {
            LOGGER.warn("At least one of the parameters is invalid {}/{} {}/{} ", START_VALUE, parameters.get(START_VALUE),
                    STEP_VALUE, parameters.get(STEP_VALUE));
            actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
        }
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            actionContext.get(SEQUENCE, values -> new CalcSequence(parameters));
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        if (row.isDeleted()) {
            return;
        }
        final CalcSequence sequence = context.get(SEQUENCE);
        final String columnId = context.getColumnId();
        row.set(columnId, sequence.getNextValue());
    }

    /** this class is used to calculate the sequence next step */
    protected static class CalcSequence {

        BigInteger nextValue;

        BigInteger step;

        public CalcSequence(Map<String, String> parameters) {
            this.nextValue = new BigInteger(parameters.get(START_VALUE));
            this.step = new BigInteger(parameters.get(STEP_VALUE));
        }

        public String getNextValue() {
            String toReturn = nextValue.toString();
            nextValue = nextValue.add(step);
            return toReturn;
        }
    }
}
