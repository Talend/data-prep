package org.talend.dataprep.upgrade.common;

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
import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.FORMAT_TYPE_PARAMETER;
import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.OTHER_REGION_TO_BE_SPECIFIED;
import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE;
import static org.talend.tql.api.TqlBuilder.eq;

public final class ActionFormatPhoneNumber {

    private static final Logger LOGGER = getLogger(ActionFormatPhoneNumber.class);

    private ActionFormatPhoneNumber() {
    }

    public static void upgradeActions(PreparationRepository preparationRepository) {
        preparationRepository.list(PreparationActions.class, eq("actions.action", FormatPhoneNumber.ACTION_NAME)) //
                .peek(action -> {
                    final String beforeUpdateId = action.id();
                    action.getActions().forEach(ActionFormatPhoneNumber::updateAction);
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
        Map<String, String> actionParameters = action.getParameters();

        String formatTypeParameter = actionParameters.get(FORMAT_TYPE_PARAMETER);
        if (Arrays.asList("International", "National").contains(formatTypeParameter)) {
            actionParameters.put(FORMAT_TYPE_PARAMETER, formatTypeParameter.toLowerCase());
        }

        if ("other (region)".equals(actionParameters.get(REGIONS_PARAMETER_CONSTANT_MODE))) {
            actionParameters.put(REGIONS_PARAMETER_CONSTANT_MODE, OTHER_REGION_TO_BE_SPECIFIED);
        }
    }
}
