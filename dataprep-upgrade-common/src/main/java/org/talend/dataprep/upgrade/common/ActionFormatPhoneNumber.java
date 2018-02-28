package org.talend.dataprep.upgrade.common;

import static org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber.*;
import static org.talend.tql.api.TqlBuilder.eq;

import java.util.Arrays;
import java.util.Map;

import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.actions.phonenumber.FormatPhoneNumber;

public final class ActionFormatPhoneNumber {

    private ActionFormatPhoneNumber() {
    }

    public static void upgradeActions(PreparationRepository preparationRepository) {
        ParameterMigration.upgradeParameters(preparationRepository, //
                eq("actions.action", FormatPhoneNumber.ACTION_NAME), //
                ActionFormatPhoneNumber::updateAction //
        );
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
