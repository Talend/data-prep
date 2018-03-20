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

import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_METHOD_KEY;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.builder;

import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;

/**
 * Actions on preparations settings
 */
// @formatter:off
public interface PreparationActions {
    ActionSettings PREPARATION_DISPLAY_MODE = builder()
            .id("preparation:display-mode")
            .name("preparation.displaymode")
            .type("@@inventory/DISPLAY_MODE")
            .payload(PAYLOAD_METHOD_KEY, "setPreparationsDisplayMode")
            .build();

    ActionSettings PREPARATION_SORT = builder()
            .id("preparation:sort")
            .name("preparation.sort")
            .type("@@preparation/SORT")
            .payload(PAYLOAD_METHOD_KEY, "changeSort")
            .build();

    ActionSettings PREPARATION_CREATE = builder()
            .id("preparation:create")
            .name("preparation.create")
            .icon("talend-plus-circle")
            .type("@@preparation/CREATE")
            .bsStyle("primary")
            .payload(PAYLOAD_METHOD_KEY, "togglePreparationCreator")
            .build();

    ActionSettings PREPARATION_COPY_MOVE = builder()
            .id("preparation:copy-move")
            .name("preparation.copymove")
            .icon("talend-files-o")
            .type("@@preparation/COPY_MOVE")
            .build();

    ActionSettings PREPARATION_SUBMIT_EDIT = builder()
            .id("preparation:submit-edit")
            .name("preparation.submitedit")
            .type("@@preparation/SUBMIT_EDIT")
            .build();

    ActionSettings PREPARATION_REMOVE = builder()
            .id("preparation:remove")
            .name("preparation.remove")
            .icon("talend-trash")
            .type("@@preparation/REMOVE")
            .build();

    ActionSettings PREPARATION_FOLDER_CREATE = builder()
            .id("preparation:folder:create")
            .name("preparation.folder.create")
            .icon("talend-folder")
            .type("@@preparation/CREATE")
            .payload(PAYLOAD_METHOD_KEY, "toggleFolderCreator")
            .build();

    ActionSettings PREPARATION_FOLDER_FETCH = builder()
            .id("preparations:folder:fetch")
            .name("preparations.folder.fetch")
            .icon("talend-dataprep")
            .type("@@preparation/FOLDER_FETCH")
            .build();

    ActionSettings PREPARATION_FOLDER_REMOVE = builder()
            .id("preparation:folder:remove")
            .name("preparation.folder.remove")
            .icon("talend-trash")
            .type("@@preparation/FOLDER_REMOVE")
            .payload(PAYLOAD_METHOD_KEY, "remove")
            .build();
}
// @formatter:on
