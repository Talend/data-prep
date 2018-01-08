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

import static org.talend.dataprep.api.service.settings.actions.api.ActionDropdownSettings.dropdownBuilder;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_METHOD_KEY;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.builder;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSplitDropdownSettings.splitDropdownBuilder;
import static org.talend.dataprep.api.service.settings.actions.provider.MenuActions.MENU_PLAYGROUND_PREPARATION;

import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;

/**
 * Actions on datasets settings
 */
// @formatter:off
public interface DatasetActions {
    ActionSettings DATASET_OPEN = builder()
            .id("dataset:open")
            .icon("talend-datastore")
            .name("dataset.open")
            .type("@@dataset/OPEN")
            .build();

    ActionSettings DATASET_PREPARATIONS = dropdownBuilder()
            .id("list:dataset:preparations")
            .name("dataset.preparations")
            .icon("talend-dataprep")
            .items("preparations")
            .dynamicAction(MENU_PLAYGROUND_PREPARATION.getId())
            .staticAction(DATASET_OPEN.getId())
            .build();

    ActionSettings DATASET_DISPLAY_MODE = builder()
            .id("dataset:display-mode")
            .name("dataset.displaymode")
            .type("@@inventory/DISPLAY_MODE")
            .payload(PAYLOAD_METHOD_KEY, "setDatasetsDisplayMode")
            .build();

    ActionSettings DATASET_SORT = builder()
            .id("dataset:sort")
            .name("dataset.sort")
            .type("@@dataset/SORT")
            .payload(PAYLOAD_METHOD_KEY, "changeSort")
            .build();

    ActionSettings DATASET_SUBMIT_EDIT = builder()
            .id("dataset:submit-edit")
            .name("dataset.submitedit")
            .type("@@dataset/SUBMIT_EDIT")
            .build();

    ActionSettings DATASET_REMOVE = builder()
            .id("dataset:remove")
            .name("dataset.remove")
            .icon("talend-trash")
            .type("@@dataset/REMOVE")
            .build();

    ActionSettings DATASET_CLONE = builder()
            .id("dataset:clone")
            .name("dataset.copy")
            .icon("talend-files-o")
            .type("@@dataset/CLONE")
            .payload(PAYLOAD_METHOD_KEY, "clone")
            .build();

    ActionSettings DATASET_FAVORITE = builder()
            .id("dataset:favorite")
            .name("dataset.favorite")
            .icon("talend-star")
            .type("@@dataset/FAVORITE")
            .payload(PAYLOAD_METHOD_KEY, "toggleFavorite")
            .build();

    ActionSettings DATASET_UPDATE = builder()
            .id("dataset:update")
            .name("dataset.update")
            .icon("talend-file-move")
            .type("@@dataset/UPDATE")
            .build();

    ActionSettings DATASET_FETCH = builder()
            .id("datasets:fetch")
            .name("dataset.fetch")
            .type("@@dataset/DATASET_FETCH")
            .build();

    ActionSettings DATASET_CREATE = splitDropdownBuilder()
            .id("dataset:create")
            .name("dataset.create")
            .icon("talend-plus-circle")
            .type("@@dataset/CREATE")
            .bsStyle("primary")
            .build();
}
// @formatter:on
