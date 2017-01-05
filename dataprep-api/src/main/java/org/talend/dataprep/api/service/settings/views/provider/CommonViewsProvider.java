// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.List;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.views.api.ViewSettings;

/**
 * Default views settings provider
 */
@Component
public class CommonViewsProvider implements AppSettingsProvider<ViewSettings> {

    @Override
    public List<ViewSettings> getSettings() {
        // @formatter:off
        return asList(
                HomeViews.APP_HEADER_BAR,
                HomeViews.BREADCRUMB,
                HomeViews.SIDE_PANEL,

                ListViews.FOLDERS_LIST,
                ListViews.PREPARATIONS_LIST,
                ListViews.DATASETS_LIST
        );
        // @formatter:on
    }
}
