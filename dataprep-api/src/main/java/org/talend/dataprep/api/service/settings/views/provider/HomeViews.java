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

package org.talend.dataprep.api.service.settings.views.provider;

import org.talend.dataprep.api.service.settings.views.api.ViewSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.AppHeaderBarSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.LinkSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.SearchSettings;
import org.talend.dataprep.api.service.settings.views.api.breadcrumb.BreadcrumbSettings;
import org.talend.dataprep.api.service.settings.views.api.sidepanel.SidePanelSettings;

import static org.talend.dataprep.api.service.settings.actions.provider.DatasetActions.*;
import static org.talend.dataprep.api.service.settings.actions.provider.MenuActions.*;
import static org.talend.dataprep.api.service.settings.actions.provider.SearchActions.*;
import static org.talend.dataprep.api.service.settings.actions.provider.WindowActions.*;

/**
 * Home elements configuration
 */
// @formatter:off
public interface HomeViews {
    ViewSettings APP_HEADER_BAR = AppHeaderBarSettings.builder()
            .id("appheaderbar")
            .app("Data Preparation")
            .brandLink(
                    LinkSettings.builder()
                            .title("Talend Data Preparation")
                            .onClick(MENU_PREPARATIONS.getId())
                            .build()
            )
            .search(
                    SearchSettings.builder()
                            .debounceTimeout(300)
                            .onBlur(SEARCH_TOGGLE.getId())
                            .onChange(SEARCH_ALL.getId())
                            .onKeyDown(SEARCH_FOCUS.getId())
                            .onToggle(SEARCH_TOGGLE.getId())
                            .onSelect("dataset", DATASET_OPEN.getId())
                            .onSelect("documentation", EXTERNAL_DOCUMENTATION.getId())
                            .onSelect("folder", MENU_FOLDERS.getId())
                            .onSelect("preparation", MENU_PLAYGROUND_PREPARATION.getId())
                            .placeholder("Search Talend Data Preparation and Documentation")
                            .build()
            )
            .action(ONBOARDING_PREPARATION.getId())
            .action(MODAL_FEEDBACK.getId())
            .action(EXTERNAL_HELP.getId())
            .action(MODAL_ABOUT.getId())
            .build();

    ViewSettings BREADCRUMB = BreadcrumbSettings.builder()
            .id("breadcrumb")
            .maxItems(5)
            .onItemClick(MENU_FOLDERS.getId())
            .build();

    ViewSettings SIDE_PANEL = SidePanelSettings.builder()
            .id("sidepanel")
            .onToggleDock("sidepanel:toggle")
            .action(MENU_PREPARATIONS.getId())
            .action(MENU_DATASETS.getId())
            .build();
}
// @formatter:on
