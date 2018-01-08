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

import static org.talend.dataprep.api.service.settings.actions.provider.WindowActions.MODAL_ABOUT;

import org.talend.dataprep.api.service.settings.views.api.ViewSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.AppHeaderBarSettings;
import org.talend.dataprep.api.service.settings.views.api.appheaderbar.LinkSettings;
import org.talend.dataprep.api.service.settings.views.api.sidepanel.SidePanelSettings;

/**
 * Home elements configuration for non TDP users (so that they get a nice UI)
 */
// @formatter:off
public interface HomeViewsForNonTDPUsers {

    ViewSettings APP_HEADER_BAR_FOR_NON_TDP_USERS = AppHeaderBarSettings.builder()
        .id("appheaderbar")
        .logo(
                LinkSettings.builder()
                .name("appheaderbar.logo")
                .build()
        )
        .brand(
                LinkSettings.builder()
                        .label("appheaderbar.brand")
                        .build()
        )
        .build();

    ViewSettings SIDE_PANEL = SidePanelSettings.builder()
        .id("sidepanel")
        .onToggleDock("sidepanel:toggle")
        .build();
}
// @formatter:on
