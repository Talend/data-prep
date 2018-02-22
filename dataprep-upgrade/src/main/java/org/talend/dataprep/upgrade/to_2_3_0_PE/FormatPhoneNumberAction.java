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

package org.talend.dataprep.upgrade.to_2_3_0_PE;

import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.tql.api.TqlBuilder.eq;

public final class FormatPhoneNumberAction {

    private static final Logger LOGGER = getLogger(FormatPhoneNumberAction.class);

    private static final String REGIONS_PARAMETER_CONSTANT_MODE = "region_code";

    private static final String FORMAT_TYPE_PARAMETER = "format_type";

    private FormatPhoneNumberAction() {
    }

    public static void upgradeActions(PreparationRepository preparationRepository) {
        preparationRepository.list(PreparationActions.class, eq("actions.action", FormatPhoneNumber.ACTION_NAME)) //
                .peek(action -> {
                    final String beforeUpdateId = action.id();
                    action.getActions().forEach(FormatPhoneNumberAction::updateAction);
                    action.setId(null);
                    final String afterUpdateId = action.id();

                    if (!beforeUpdateId.equals(afterUpdateId)) {
                        LOGGER.debug("Migration changed action id from '{}' to '{}', updating steps", beforeUpdateId,
                                afterUpdateId);
                        preparationRepository.list(PersistentStep.class, eq("contentId", beforeUpdateId)) //
                                .filter(s -> !Step.ROOT_STEP.id().equals(s.id())) //
                                .peek(s -> s.setContent(afterUpdateId)) //
                                .forEach(preparationRepository::add);
                    }
                }) //
                .forEach(preparationRepository::add); //
    }

    private static void updateAction(Action action) {
        if (!FormatPhoneNumber.ACTION_NAME.equals(action.getName())) {
            return;
        }

        Map<String, String> actionParameters = action.getParameters();

        String formatTypeParameter = actionParameters.get(FORMAT_TYPE_PARAMETER);
        if (Arrays.asList("International", "National").contains(formatTypeParameter)) {
            actionParameters.put(FORMAT_TYPE_PARAMETER, formatTypeParameter.toLowerCase());
        }

        if ("other (region)".equals(actionParameters.get(REGIONS_PARAMETER_CONSTANT_MODE))) {
            actionParameters.put(REGIONS_PARAMETER_CONSTANT_MODE, "other_region");
        }
    }
}
