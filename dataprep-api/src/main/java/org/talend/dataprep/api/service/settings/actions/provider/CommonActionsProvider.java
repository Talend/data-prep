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

package org.talend.dataprep.api.service.settings.actions.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Default actions settings provider
 */
@Component
public class CommonActionsProvider implements AppSettingsProvider<ActionSettings> {

    @Autowired
    private ExternalHelpActionsProvider externalHelpActionsProvider;

    @Override
    public List<ActionSettings> getSettings() {
        // @formatter:off
        return asList(
                DatasetActions.DATASET_CLONE,
                DatasetActions.DATASET_CREATE,
                DatasetActions.DATASET_DISPLAY_MODE,
                DatasetActions.DATASET_FAVORITE,
                DatasetActions.DATASET_FETCH,
                DatasetActions.DATASET_OPEN,
                DatasetActions.DATASET_PREPARATIONS,
                DatasetActions.DATASET_REMOVE,
                DatasetActions.DATASET_SORT,
                DatasetActions.DATASET_SUBMIT_EDIT,
                DatasetActions.DATASET_UPDATE,

                InventoryActions.INVENTORY_CANCEL_EDIT,
                InventoryActions.INVENTORY_EDIT,

                MenuActions.MENU_DATASETS,
                MenuActions.MENU_FOLDERS,
                MenuActions.MENU_PREPARATIONS,
                MenuActions.MENU_PLAYGROUND_PREPARATION,
                MenuActions.SIDE_PANEL_TOGGLE,

                PreparationActions.PREPARATION_COPY_MOVE,
                PreparationActions.PREPARATION_CREATE,
                PreparationActions.PREPARATION_FOLDER_CREATE,
                PreparationActions.PREPARATION_FOLDER_FETCH,
                PreparationActions.PREPARATION_FOLDER_REMOVE,
                PreparationActions.PREPARATION_DISPLAY_MODE,
                PreparationActions.PREPARATION_REMOVE,
                PreparationActions.PREPARATION_SUBMIT_EDIT,
                PreparationActions.PREPARATION_SORT,

                SearchActions.SEARCH_ALL,
                SearchActions.SEARCH_DOC,
                SearchActions.SEARCH_FOCUS,
                SearchActions.SEARCH_TOGGLE,

                WindowActions.EXTERNAL_DOCUMENTATION,
                externalHelpActionsProvider.getExternalHelpAction(),
                externalHelpActionsProvider.getExternalCommunityAction(),
                WindowActions.MODAL_ABOUT,
                WindowActions.MODAL_FEEDBACK,
                WindowActions.ONBOARDING_PREPARATION,
                WindowActions.ONBOARDING_PLAYGROUND,
                WindowActions.HEADERBAR_HELP,
                WindowActions.PLAYGROUND_HEADERBAR_HELP
        );
        // @formatter:on
    }
}
