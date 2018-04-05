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
import org.talend.dataprep.api.service.settings.views.api.sidepanel.SidePanelSettings;

/**
 * Home elements configuration for non TDP users (so that they get a nice UI)
 */
// @formatter:off
public abstract class HomeViewsForNonTDPUsers {

    public static ViewSettings appHeaderBarForNonTdpUsers() {
        return AppHeaderBarSettings.builder()
                .id("appheaderbar")
                .logo(
                        LinkSettings.builder()
                                .name("appheaderbar.logo")
                                .label("appheaderbar.logo.tooltip")
                                .build()
                )
                .brand(
                        LinkSettings.builder()
                                .label("appheaderbar.brand")
                                .build()
                )
                .help("external:help")
                .build();
    }

    public static ViewSettings sidePanel() {
        return SidePanelSettings.builder()
                .id("sidepanel")
                .onToggleDock("sidepanel:toggle")
                .build();
    }

}
// @formatter:on
