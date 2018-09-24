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

package org.talend.dataprep.api.service.settings.context.provider;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.context.api.ContextSettings;
import org.talend.dataprep.dataset.DatasetConfiguration;
import org.talend.dataprep.security.Security;

/**
 * Default documentation settings provider
 */
@Component
public class ContextProvider implements AppSettingsProvider<ContextSettings> {

    @Autowired
    protected ApplicationContext context;

    @Autowired
    private Security security;

    @Override
    public List<ContextSettings> getSettings() {
        final Locale locale = security.getLocale();
        return asList( //
                ContextSettings
                        .builder() //
                        .id("locale") //
                        .value(locale.toLanguageTag()) //
                        .build(), //
                ContextSettings
                        .builder() //
                        .id("country") //
                        .value(locale.getCountry()) //
                        .build(), //
                ContextSettings
                        .builder() //
                        .id("language") //
                        .value(locale.getLanguage()) //
                        .build(), //
                ContextSettings
                        .builder() //
                        .id("provider") //
                        .value(context.getBean(DatasetConfiguration.class).getProvider()) //
                        .build() //
        );
    }
}
