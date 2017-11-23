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

package org.talend.dataprep.configuration;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Localization {

    private static final Logger LOGGER = getLogger(Localization.class);

    @Value("${dataprep.default.locale:en}")
    private String defaultLocale;

    @PostConstruct
    public void localizationDefaultConfiguration() {
        Locale locale;
        try {
            locale = LocaleUtils.toLocale(defaultLocale);
        } catch (Exception e) {
            locale = Locale.US;
            LOGGER.warn("Illegal locale supplied in configuration: {}. Defaulting to english.", defaultLocale);
        }
        Locale.setDefault(locale);
        LOGGER.info("Locale used: '{}'", locale);
    }
}
