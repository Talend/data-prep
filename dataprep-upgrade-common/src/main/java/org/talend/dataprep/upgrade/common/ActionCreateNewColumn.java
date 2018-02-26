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

import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.actions.column.CreateNewColumn;

public class ActionCreateNewColumn {

    private ActionCreateNewColumn() {
    }

    public static void upgradeActions(PreparationRepository preparationRepository) {
        ParameterMigration.upgradeParameters(preparationRepository, //
                eq("actions.action", CreateNewColumn.ACTION_NAME), //
                ActionCreateNewColumn::updateAction //
        );
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
