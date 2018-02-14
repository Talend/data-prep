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
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;

public class ActionNewColumnToggleCommon {

    private static final Logger LOGGER = getLogger(ActionNewColumnToggleCommon.class);

    private ActionNewColumnToggleCommon() {
    }

    public static void upgradeActions(PreparationRepository preparationRepository) {
        preparationRepository
                .list(PreparationActions.class) //
                .filter(pa -> !PreparationActions.ROOT_ACTIONS.id().equals(pa.id()) && pa.getActions() != null
                        && !pa.getActions().isEmpty()) //
                .peek(action -> {
                    final String beforeUpdateId = action.id();
                    action.getActions().forEach(ActionNewColumnToggleCommon::updateAction);
                    action.setId(null);
                    final String afterUpdateId = action.id();

                    if (!beforeUpdateId.equals(afterUpdateId)) {
                        LOGGER.debug("Migration changed action id from '{}' to '{}', updating steps", beforeUpdateId,
                                afterUpdateId);
                        preparationRepository
                                .list(PersistentStep.class, eq("contentId", beforeUpdateId)) //
                                .filter(s -> !Step.ROOT_STEP.id().equals(s.id())) //
                                .peek(s -> s.setContent(afterUpdateId)) //
                                .forEach(preparationRepository::add);
                    }
                }) //
                .forEach(preparationRepository::add); //
    }

    /**
     * Only actions that does not have an explicit "create column" parameter should be passed to the update method that will add
     * the parameter for all actions that should have it.
     */
    private static boolean shouldAddCreateNewColumnParameter(Action action) {
        return !action.getParameters().containsKey(CREATE_NEW_COLUMN);
    }

    private static void updateAction(Action action) {
        if (shouldAddCreateNewColumnParameter(action)) {

            Boolean oldBehaviour; // true/false: change needed, null mean no default behavior change
            switch (action.getName()) {
            case "padding":
            case "absolute":
            case "change_date_pattern":
            case "lowercase":
            case "propercase":
            case "uppercase":
            case "date_calendar_converter":
            case "distance_converter":
            case "duration_converter":
            case "delete":
            case "delete_lines":
            case "change_number_format":
            case "format_phone_number":
            case "generate_a_sequence":
            case "modify_date":
            case "negate":
            case "remove_non_alpha_num_chars":
            case "remove_non_num_chars":
            case "remove_repeated_chars":
            case "round_down":
            case "cut":
            case "trim":
            case "replace_cell_value":
            case "ceil":
            case "round_down_real":
            case "floor":
            case "round":
            case "normalize":
                oldBehaviour = FALSE;
                break;
            case "numeric_ops":
            case "logarithm_numbers":
            case "compute_time_since":
            case "timestamp_to_date":
            case "compare_dates":
            case "compare_numbers":
            case "concat":
            case "temperatures_converter":
            case "cos_numbers":
            case "exponential_numbers":
            case "substring":
            case "max_numbers":
            case "min_numbers":
            case "natural_logarithm_numbers":
            case "negate_numbers":
            case "pow_numbers":
            case "sin_numbers":
            case "square_root_numbers":
            case "tan_numbers":
                oldBehaviour = TRUE;
                break;
            default:
                LOGGER.debug("Action {} had no default column creation ", action.getName());
                oldBehaviour = null; // no change to do
            }
            if (oldBehaviour != null) {
                action.getParameters().put(CREATE_NEW_COLUMN, oldBehaviour.toString());
            }
        }
    }
}
