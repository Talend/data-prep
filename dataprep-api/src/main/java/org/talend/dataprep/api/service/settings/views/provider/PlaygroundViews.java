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

import org.talend.dataprep.api.service.settings.views.api.ViewSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.AppHeaderBarSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.LinkSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.SearchSettings;

import static org.talend.dataprep.api.service.settings.actions.provider.DatasetActions.DATASET_OPEN;
import static org.talend.dataprep.api.service.settings.actions.provider.MenuActions.*;
import static org.talend.dataprep.api.service.settings.actions.provider.SearchActions.*;
import static org.talend.dataprep.api.service.settings.actions.provider.WindowActions.*;

/**
 * Playground elements configuration
 */
// @formatter:off
public interface PlaygroundViews {
    ViewSettings PLAYGROUND_APP_HEADER_BAR = AppHeaderBarSettings.builder()
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
            .help(PLAYGROUND_HEADERBAR_HELP.getId())
            .build();
}
// @formatter:on
