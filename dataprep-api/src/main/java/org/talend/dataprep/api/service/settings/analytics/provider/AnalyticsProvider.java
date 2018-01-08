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

package org.talend.dataprep.api.service.settings.analytics.provider;

import static java.util.Collections.singletonList;

import java.util.List;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.analytics.api.AnalyticsSettings;

/**
 * Default analytics settings provider
 */
@Component
public class AnalyticsProvider implements AppSettingsProvider<AnalyticsSettings> {

    public static final String ANALYTICS_ENABLED = "analyticsEnabled";

    @Override
    public List<AnalyticsSettings> getSettings() {

        return singletonList(AnalyticsSettings.builder().id(ANALYTICS_ENABLED).value("false").build());
    }
}
