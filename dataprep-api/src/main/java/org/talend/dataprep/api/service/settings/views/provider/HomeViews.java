// ============================================================================
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

package org.talend.dataprep.api.service.settings.views.provider;

import static org.talend.dataprep.api.service.settings.actions.provider.DatasetActions.DATASET_OPEN;
import static org.talend.dataprep.api.service.settings.actions.provider.MenuActions.MENU_DATASETS;
import static org.talend.dataprep.api.service.settings.actions.provider.MenuActions.MENU_FOLDERS;
import static org.talend.dataprep.api.service.settings.actions.provider.MenuActions.MENU_PLAYGROUND_PREPARATION;
import static org.talend.dataprep.api.service.settings.actions.provider.MenuActions.MENU_PREPARATIONS;
import static org.talend.dataprep.api.service.settings.actions.provider.SearchActions.SEARCH_ALL;
import static org.talend.dataprep.api.service.settings.actions.provider.SearchActions.SEARCH_FOCUS;
import static org.talend.dataprep.api.service.settings.actions.provider.SearchActions.SEARCH_TOGGLE;
import static org.talend.dataprep.api.service.settings.actions.provider.WindowActions.EXTERNAL_DOCUMENTATION;
import static org.talend.dataprep.api.service.settings.actions.provider.WindowActions.HEADERBAR_INFORMATION;

import org.talend.dataprep.api.service.settings.views.api.ViewSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.AppHeaderBarSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.LinkSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.SearchSettings;
import org.talend.dataprep.api.service.settings.views.api.breadcrumb.BreadcrumbSettings;
import org.talend.dataprep.api.service.settings.views.api.sidepanel.SidePanelSettings;

/**
 * Home elements configuration
 */
// @formatter:off
public abstract class HomeViews {

    public static ViewSettings appHeaderBar() {
        return AppHeaderBarSettings.builder()
                .id("appheaderbar")
                .brand(
                        LinkSettings.builder()
                                .label("appheaderbar.brand")
                                .onClick(MENU_PREPARATIONS.getId())
                                .build()
                )
                .logo(
                        LinkSettings.builder()
                                .name("appheaderbar.logo")
                                .label("appheaderbar.logo.tooltip")
                                .onClick(MENU_PREPARATIONS.getId())
                                .build())
                .search(
                        SearchSettings.builder()
                                .debounceTimeout(300)
                                .onBlur(SEARCH_TOGGLE.getId())
                                .onChange(SEARCH_ALL.getId())
                                .onKeyDown(SEARCH_FOCUS.getId())
                                .onToggle(SEARCH_TOGGLE.getId())
                                .onSelect("documentation", EXTERNAL_DOCUMENTATION.getId())
                                .onSelect("preparation", MENU_PLAYGROUND_PREPARATION.getId())
                                .onSelect("folder", MENU_FOLDERS.getId())
                                .onSelect("dataset", DATASET_OPEN.getId())
                                .placeholder("appheaderbar.search.placeholder")
                                .build()
                )
                .help("external:help")
                .information(HEADERBAR_INFORMATION.getId())
                .build();
    }

    public static ViewSettings breadcrumb() {
        return BreadcrumbSettings.builder()
                .id("breadcrumb")
                .maxItems(5)
                .onItemClick(MENU_FOLDERS.getId())
                .build();
    }

    public static ViewSettings sidePanel() {
        return SidePanelSettings.builder()
                .id("sidepanel")
                .onToggleDock("sidepanel:toggle")
                .action(MENU_PREPARATIONS.getId())
                .action(MENU_DATASETS.getId())
                .build();
    }

}
// @formatter:on
