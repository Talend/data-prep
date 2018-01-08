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

import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_ARGS_KEY;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.PAYLOAD_METHOD_KEY;
import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.builder;

import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;

/**
 * Actions on menus (side panel for example) and route change
 */
// @formatter:off
public interface MenuActions {
    ActionSettings SIDE_PANEL_TOGGLE = builder()
            .id("sidepanel:toggle")
            .name("sidepanel.toggle")
            .icon("talend-arrow-left")
            .type("@@sidepanel/TOGGLE")
            .payload(PAYLOAD_METHOD_KEY, "toggleHomeSidepanel")
            .build();

    ActionSettings MENU_PREPARATIONS = builder()
            .id("menu:preparations")
            .name("menu.preparations")
            .icon("talend-dataprep")
            .type("@@router/GO_CURRENT_FOLDER")
            .payload(PAYLOAD_METHOD_KEY, "go")
            .payload(PAYLOAD_ARGS_KEY, new String[]{"home.preparations"})
            .build();

    ActionSettings MENU_FOLDERS = builder()
            .id("menu:folders")
            .name("menu.folders")
            .icon("talend-folder")
            .type("@@router/GO_FOLDER")
            .payload(PAYLOAD_METHOD_KEY, "go")
            .payload(PAYLOAD_ARGS_KEY, new String[]{"home.preparations"})
            .build();

    ActionSettings MENU_DATASETS = builder()
            .id("menu:datasets")
            .name("menu.datasets")
            .icon("talend-datastore")
            .type("@@router/GO")
            .payload(PAYLOAD_METHOD_KEY, "go")
            .payload(PAYLOAD_ARGS_KEY, new String[]{"home.datasets"})
            .build();

    ActionSettings MENU_PLAYGROUND_PREPARATION = builder()
            .id("menu:playground:preparation")
            .name("menu.playground.preparation")
            .icon("talend-dataprep")
            .type("@@router/GO_PREPARATION")
            .payload(PAYLOAD_METHOD_KEY, "go")
            .payload(PAYLOAD_ARGS_KEY, new String[]{"playground.preparation"})
            .build();
}
// @formatter:on
