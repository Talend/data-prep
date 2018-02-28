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

import java.util.Locale;

import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.actions.date.ExtractDateTokens;

public class AddLanguageToActionExtractDatePartUpgrade {

    private AddLanguageToActionExtractDatePartUpgrade() {
    }

    public static void upgradeActions(PreparationRepository preparationRepository) {
        ParameterMigration.upgradeParameters(preparationRepository,
                AddLanguageToActionExtractDatePartUpgrade::updateAction);
    }

    private static void updateAction(Action action) {
        if (ExtractDateTokens.ACTION_NAME.equals(action.getName())) {
            action.getParameters().put(ExtractDateTokens.LANGUAGE, Locale.getDefault().getLanguage());
        }
    }
}
