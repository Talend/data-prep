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

import java.util.List;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.context.api.ContextSettings;
import org.talend.dataprep.ui.UiConfiguration;

import static java.util.Arrays.asList;

/**
 * Default documentation settings provider
 */
@Component
public class ContextProvider implements AppSettingsProvider<ContextSettings> {

    private final UiConfiguration uiConfiguration;

    public ContextProvider(UiConfiguration uiConfiguration) {
        this.uiConfiguration = uiConfiguration;
    }

    @Override
    public List<ContextSettings> getSettings() {
        Locale locale = LocaleContextHolder.getLocale();
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
                        .id("theme") //
                        .value(uiConfiguration.hasTheme()) //
                        .build() //
        );
    }
}
