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

package org.talend.dataprep.api.service.settings.help.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.help.api.HelpSettings;
import org.talend.dataprep.help.Help;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Default documentation settings provider
 */
@Component
public class HelpProvider implements AppSettingsProvider<HelpSettings> {

    @Autowired
    private Help help;

    @Override
    public List<HelpSettings> getSettings() {

        return asList(
                HelpSettings.builder()
                        .id("versionFacet")
                        .value(help.getVersionFacet())
                        .build(),

                HelpSettings.builder()
                        .id("languageFacet")
                        .value(help.getLanguageFacet())
                        .build(),

                HelpSettings.builder()
                        .id("searchUrl")
                        .value(help.getSearchUrl())
                        .build(),

                HelpSettings.builder()
                        .id("fuzzyUrl")
                        .value(help.getFuzzyUrl())
                        .build(),

                HelpSettings.builder()
                        .id("exactUrl")
                        .value(help.getExactUrl())
                        .build()
        );
    }
}
