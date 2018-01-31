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

import org.slf4j.Logger;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.preparation.store.PreparationRepository;

public class ActionNewColumnToggleCommon {

    private static final Logger LOGGER = getLogger(ActionNewColumnToggleCommon.class);

    private ActionNewColumnToggleCommon() {}

    public static void upgradeActions(PreparationRepository preparationRepository) {
        preparationRepository
                .list(PreparationActions.class) //
                .peek(action -> action.getActions().forEach(ActionNewColumnToggleCommon::updateAction)) //
                .forEach(preparationRepository::add); //
    }

    private static void updateAction(Action action) {
        Boolean newDefaultBehavior; // true/false: change needed, null mean no default behavior change
        switch (action.getName()) {
        case "generate_a_sequence":
            newDefaultBehavior = TRUE;
            break;
        case "numeric_ops":
        case "logarithm_numbers":
        case "concat":
        case "temperatures_converter":
        case "cos_numbers":
        case "exponential_numbers":
        case "lookup":
        case "max_numbers":
        case "min_numbers":
        case "natural_logarithm_numbers":
        case "negate_numbers":
        case "pow_numbers":
        case "sin_numbers":
        case "square_root_numbers":
        case "tan_numbers":
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
