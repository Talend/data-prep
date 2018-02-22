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

import org.slf4j.Logger;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.actions.column.CreateNewColumn;

public class ActionCreateNewColumn {

    private static final Logger LOGGER = getLogger(ActionNewColumnToggleCommon.class);

    private ActionCreateNewColumn() {
    }

    public static void upgradeActions(PreparationRepository preparationRepository) {
        preparationRepository.list(PreparationActions.class, eq("actions.action", CreateNewColumn.ACTION_NAME)) //
                .filter(pa -> !PreparationActions.ROOT_ACTIONS.id().equals(pa.id()) && pa.getActions() != null
                        && !pa.getActions().isEmpty()) //
                .peek(action -> {
                    final String beforeUpdateId = action.id();
                    action.getActions().forEach(ActionCreateNewColumn::updateAction);
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

    private static boolean shouldUpdateAction(Action action) {
        return action.getName().equals(CreateNewColumn.ACTION_NAME);
    }

    private static void updateAction(Action action) {
        if (shouldUpdateAction(action)) {
            String mode = action.getParameters().get(CreateNewColumn.MODE_PARAMETER);
            String newMode = null;
            switch (mode) {
            case CreateNewColumn.OLD_COLUMN_MODE:
                newMode = CreateNewColumn.COLUMN_MODE;
                break;
            case CreateNewColumn.OLD_CONSTANT_MODE:
                newMode = CreateNewColumn.CONSTANT_MODE;
                break;
            case CreateNewColumn.OLD_EMPTY_MODE:
                newMode = CreateNewColumn.EMPTY_MODE;
                break;
            }
            if (newMode != null) {
                action.getParameters().put(CreateNewColumn.MODE_PARAMETER, newMode);
            }
        }
    }
}
