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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.documentation.api.DocumentationSettings;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Default documentation settings provider
 */
@Component
public class DocumentationProvider implements AppSettingsProvider<DocumentationSettings> {

    @Value("${documentation.url:https://www.talendforge.org/find/api/THC.php}")
    private String url;

    @Value("${documentation.version:2.1}")
    private String version;

    @Value("${documentation.language:en}")
    private String language;

    @Override
    public List<DocumentationSettings> getSettings() {

        // @formatter:off
        return asList(
                DocumentationSettings.builder()
                        .id("url")
                        .value(url)
                        .build(),

                DocumentationSettings.builder()
                        .id("version")
                        .value(version)
                        .build(),

                DocumentationSettings.builder()
                        .id("language")
                        .value(language)
                        .build()
        );
        // @formatter:on
    }
}
