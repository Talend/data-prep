//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.settings.actions.provider;

import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;

import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_METHOD_KEY;

public interface PreparationActions {
    ActionSettings PREPARATION_DISPLAY_MODE = ActionSettings.builder()
            .id("preparation:display-mode")
            .name("Change preparation display mode")
            .type("@@inventory/DISPLAY_MODE")
            .payload(PAYLOAD_METHOD_KEY, "setPreparationsDisplayMode")
            .build();

    ActionSettings PREPARATION_SORT = ActionSettings.builder()
            .id("preparation:sort")
            .name("Change preparation sort")
            .type("@@preparation/SORT")
            .payload(PAYLOAD_METHOD_KEY, "setPreparationsSortFromIds")
            .build();

    ActionSettings PREPARATION_CREATE = ActionSettings.builder()
            .id("preparation:create")
            .name("Create preparation")
            .icon("talend-plus")
            .type("@@preparation/CREATE")
            .bsStyle("primary")
            .payload(PAYLOAD_METHOD_KEY, "togglePreparationCreator")
            .build();

    ActionSettings PREPARATION_COPY_MOVE = ActionSettings.builder()
            .id("preparation:copy-move")
            .name("Copy/Move preparation")
            .icon("talend-files-o")
            .type("@@preparation/COPY_MOVE")
            .payload(PAYLOAD_METHOD_KEY, "copyMove")
            .build();

    ActionSettings PREPARATION_SUBMIT_EDIT = ActionSettings.builder()
            .id("preparation:submit-edit")
            .name("Submit name edition")
            .type("@@preparation/SUBMIT_EDIT")
            .build();

    ActionSettings PREPARATION_REMOVE = ActionSettings.builder()
            .id("preparation:remove")
            .name("Remove preparation")
            .icon("talend-trash")
            .type("@@preparation/REMOVE")
            .payload(PAYLOAD_METHOD_KEY, "remove")
            .build();

    ActionSettings PREPARATION_FOLDER_CREATE = ActionSettings.builder()
            .id("preparation:folder:create")
            .name("Create folder")
            .icon("talend-folder")
            .type("@@preparation/CREATE")
            .payload(PAYLOAD_METHOD_KEY, "toggleFolderCreator")
            .build();

    ActionSettings PREPARATION_FOLDER_FETCH = ActionSettings.builder()
            .id("preparations:folder:fetch")
            .name("Fetch preparations from current folder")
            .icon("talend-dataprep")
            .type("@@preparation/FOLDER_FETCH")
            .payload(PAYLOAD_METHOD_KEY, "init")
            .build();

    ActionSettings PREPARATION_FOLDER_REMOVE = ActionSettings.builder()
            .id("preparation:folder:remove")
            .name("Remove folder")
            .icon("talend-trash")
            .type("@@preparation/FOLDER_REMOVE")
            .payload(PAYLOAD_METHOD_KEY, "removeFolder")
            .build();
}
