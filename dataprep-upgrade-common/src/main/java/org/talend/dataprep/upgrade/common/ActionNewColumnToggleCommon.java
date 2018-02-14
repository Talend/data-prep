/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.upgrade.common;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.CREATE_NEW_COLUMN;
import static org.talend.tql.api.TqlBuilder.eq;

import org.slf4j.Logger;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.actions.column.Concat;
import org.talend.dataprep.transformation.actions.conversions.TemperaturesConverter;
import org.talend.dataprep.transformation.actions.datablending.Lookup;
import org.talend.dataprep.transformation.actions.fill.GenerateSequence;
import org.talend.dataprep.transformation.actions.math.*;

public class ActionNewColumnToggleCommon {

    private static final Logger LOGGER = getLogger(ActionNewColumnToggleCommon.class);

    private ActionNewColumnToggleCommon() {}

    public static void upgradeActions(PreparationRepository preparationRepository) {
        preparationRepository
                .list(PreparationActions.class) //
                .peek(action -> {
                    final String beforeUpdateId = action.id();
                    action.getActions().forEach(ActionNewColumnToggleCommon::updateAction);
                    final String afterUpdateId = action.id();

                    if (!beforeUpdateId.equals(afterUpdateId)) {
                        LOGGER.debug("Migration changed action id from '{}' to '{}', updating steps", beforeUpdateId, afterUpdateId);
                        preparationRepository.list(Step.class, eq("content", beforeUpdateId)) //
                                .peek(s -> s.setContent(afterUpdateId)) //
                                .forEach(preparationRepository::add);
                    }
                }) //
                .forEach(preparationRepository::add); //
    }

    private static void updateAction(Action action) {
        final Boolean newDefaultBehavior; // true/false: change needed, null mean no default behavior change
        switch (action.getName()) {
        case GenerateSequence.ACTION_NAME:
            newDefaultBehavior = TRUE;
            break;
        case NumericOperations.ACTION_NAME:
        case Logarithm.LOGARITHM_NAME:
        case Concat.CONCAT_ACTION_NAME:
        case TemperaturesConverter.ACTION_NAME:
        case Cos.COS_NAME:
        case Exponential.EXPONENTIAL_NAME:
        case Lookup.LOOKUP_ACTION_NAME:
        case Max.MAX_NAME:
        case Min.MIN_NAME:
        case NaturalLogarithm.NATURAL_LOGARITHM_NAME:
        case Negate.NEGATE_NAME:
        case Pow.POW_NAME:
        case Sin.SIN_NAME:
        case SquareRoot.SQRT_NAME:
        case Tan.TAN_NAME:
            newDefaultBehavior = FALSE;
            break;
        default:
            LOGGER.debug("Action {} had no default column creation ", action.getName());
            newDefaultBehavior = null; // no change to do
        }
        if (newDefaultBehavior != null) {
            action.getParameters().put(CREATE_NEW_COLUMN, newDefaultBehavior.toString());
        }
    }
}
