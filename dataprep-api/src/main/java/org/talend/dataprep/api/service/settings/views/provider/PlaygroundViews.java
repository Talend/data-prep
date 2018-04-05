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

import static org.talend.dataprep.api.service.settings.actions.provider.MenuActions.MENU_PREPARATIONS;
import static org.talend.dataprep.api.service.settings.actions.provider.SearchActions.SEARCH_DOC;
import static org.talend.dataprep.api.service.settings.actions.provider.SearchActions.SEARCH_FOCUS;
import static org.talend.dataprep.api.service.settings.actions.provider.SearchActions.SEARCH_TOGGLE;
import static org.talend.dataprep.api.service.settings.actions.provider.WindowActions.EXTERNAL_DOCUMENTATION;
import static org.talend.dataprep.api.service.settings.actions.provider.WindowActions.HEADERBAR_INFORMATION_PLAYGROUND;

import org.talend.dataprep.api.service.settings.views.api.ViewSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.AppHeaderBarSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.LinkSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.SearchSettings;

/**
 * Playground elements configuration
 */
// @formatter:off
public abstract class PlaygroundViews {

    public static ViewSettings playgroundAppHeaderBar() {
        return AppHeaderBarSettings.builder()
                .id("appheaderbar:playground")
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
                                .onChange(SEARCH_DOC.getId())
                                .onKeyDown(SEARCH_FOCUS.getId())
                                .onToggle(SEARCH_TOGGLE.getId())
                                .onSelect("documentation", EXTERNAL_DOCUMENTATION.getId())
                                .placeholder("appheaderbar.search.playground.placeholder")
                                .build()
                )
                .help("external:help")
                .information(HEADERBAR_INFORMATION_PLAYGROUND.getId())
                .build();
    }
}
// @formatter:on
