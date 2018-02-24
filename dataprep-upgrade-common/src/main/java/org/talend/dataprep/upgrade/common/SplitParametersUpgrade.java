// ============================================================================
//
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

package org.talend.dataprep.upgrade.common;

import static org.talend.tql.api.TqlBuilder.eq;

import java.util.Map;

import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.actions.text.Split;

public class SplitParametersUpgrade {

    private SplitParametersUpgrade() {
    }

    public static void upgradeActions(PreparationRepository preparationRepository) {
        ParameterMigration.upgradeParameters(preparationRepository, eq("actions.action", //
                Split.SPLIT_ACTION_NAME), //
                SplitParametersUpgrade::updateAction);
    }

    private static void updateAction(Action action) {
        final Map<String, String> parameters = action.getParameters();
        final String separator = parameters.get(Split.SEPARATOR_PARAMETER);
        if (Split.OLD_OTHER_REGEX.equals(separator)) {
            parameters.put(Split.SEPARATOR_PARAMETER, Split.OTHER_REGEX);
        } else if (Split.OLD_OTHER_STRING.equals(separator)) {
            parameters.put(Split.SEPARATOR_PARAMETER, Split.OTHER_STRING);
        }
    }
}
