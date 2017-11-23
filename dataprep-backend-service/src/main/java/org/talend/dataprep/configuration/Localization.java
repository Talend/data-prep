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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * This class is used to set default JVM locale at the initialization.
 * It's not a duplication of DataprepLocaleContextResolver because it only deals with JVM default Locale and not with
 * EE Spring context Locale
 */
@Configuration
public class Localization {

    private static final Logger LOGGER = getLogger(Localization.class);

    @Value("${dataprep.default.locale:en-US}")
    private String defaultLocale;

    @PostConstruct
    public void localizationDefaultConfiguration() {
        Locale locale;
        if (StringUtils.isEmpty(defaultLocale)) {
            LOGGER.debug("Detected empty locale, default to english.");
            locale = Locale.US;
        } else {
            locale = new Locale.Builder().setLanguageTag(defaultLocale).build();
            if (LocaleUtils.isAvailableLocale(locale)) {
                LOGGER.info("Setting default JVM locale to {}", locale);
            } else {
                LOGGER.info("Locale {} is not available. Defaulting to {}", locale, Locale.US);
                locale = Locale.US;
            }
        }
        Locale.setDefault(locale);
        LOGGER.info("Locale used: '{}'", locale);
    }
}
