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

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.tql.api.TqlBuilder.eq;

import java.util.Locale;

import org.slf4j.Logger;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.actions.date.ExtractDateTokens;

public class AddLanguageToActionExtractDatePartUpgrade {

    private static final Logger LOGGER = getLogger(AddLanguageToActionExtractDatePartUpgrade.class);

    private AddLanguageToActionExtractDatePartUpgrade() {
    }

    public static void upgradeActions(PreparationRepository preparationRepository) {
        preparationRepository.list(PreparationActions.class) //
                .filter(pa -> !PreparationActions.ROOT_ACTIONS.id().equals(pa.id()) && pa.getActions() != null
                        && !pa.getActions().isEmpty())
                .peek(action -> {
                    final String beforeUpdateId = action.id();
                    action.getActions().stream().filter(a -> ExtractDateTokens.ACTION_NAME.equals(a.getName()))
                            .forEach(AddLanguageToActionExtractDatePartUpgrade::updateAction);
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

    /**
     * Only actions that does not have an explicit "create column" parameter should be passed to the update method that will add
     * the parameter for all actions that should have it.
     */
    private static boolean shouldUpdateAction(Action action) {
        return !action.getParameters().containsKey(ExtractDateTokens.LANGUAGE);
    }

    private static void updateAction(Action action) {
        action.getParameters().put(ExtractDateTokens.LANGUAGE, Locale.getDefault().getLanguage());
    }
}
