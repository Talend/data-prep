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

package org.talend.dataprep.api.service.settings.views.provider;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.views.api.ViewSettings;
import org.talend.dataprep.security.Security;

/**
 * Default views settings provider
 */
@Component
public class CommonViewsProvider implements AppSettingsProvider<ViewSettings> {

    @Autowired
    private Security security;

    @Override
    public List<ViewSettings> getSettings() {

        List<ViewSettings> settings = new ArrayList<>(5);

        if (security.isTDPUser()) {
            settings.add(HomeViews.APP_HEADER_BAR);
            settings.add(HomeViews.SIDE_PANEL);
            settings.add(HomeViews.BREADCRUMB);
            settings.add(PlaygroundViews.PLAYGROUND_APP_HEADER_BAR);
        } else {
            settings.add(HomeViewsForNonTDPUsers.APP_HEADER_BAR_FOR_NON_TDP_USERS);
            settings.add(HomeViewsForNonTDPUsers.SIDE_PANEL);
        }

        settings.addAll(asList(ListViews.FOLDERS_LIST, ListViews.PREPARATIONS_LIST, ListViews.DATASETS_LIST));

        return settings;

    }
}
