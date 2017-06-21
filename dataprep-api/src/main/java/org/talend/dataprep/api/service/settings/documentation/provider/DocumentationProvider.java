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

package org.talend.dataprep.api.service.settings.documentation.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.documentation.api.DocumentationSettings;
import org.talend.dataprep.help.Help;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Default documentation settings provider
 */
@Component
public class DocumentationProvider implements AppSettingsProvider<DocumentationSettings> {

    @Autowired
    private Help help;

    @Override
    public List<DocumentationSettings> getSettings() {

        return asList(
                DocumentationSettings.builder()
                        .id("searchUrl")
                        .value(help.getSearchUrl())
                        .build(),

                DocumentationSettings.builder()
                        .id("fuzzyUrl")
                        .value(help.getFuzzyUrl())
                        .build(),

                DocumentationSettings.builder()
                        .id("exactUrl")
                        .value(help.getExactUrl())
                        .build()
        );
    }
}
