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

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.AppSettingsProvider;
import org.talend.dataprep.api.service.settings.context.api.ContextSettings;

/**
 * Default documentation settings provider
 */
@Component
public class ContextProvider implements AppSettingsProvider<ContextSettings> {

    @Override
    public List<ContextSettings> getSettings() {

        return asList( //
                ContextSettings
                        .builder() //
                        .id("locale") //
                        .value(LocaleContextHolder.getLocale().toLanguageTag()) //
                        .build(), //
                ContextSettings
                        .builder() //
                        .id("country") //
                        .value(LocaleContextHolder.getLocale().getCountry()) //
                        .build(), //
                ContextSettings
                        .builder() //
                        .id("language") //
                        .value(LocaleContextHolder.getLocale().getLanguage()) //
                        .build() //
        );
    }
}
