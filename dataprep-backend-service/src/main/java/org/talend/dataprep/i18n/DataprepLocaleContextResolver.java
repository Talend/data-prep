// ============================================================================
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
package org.talend.dataprep.i18n;

import static java.util.Locale.US;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME;

import java.util.Arrays;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

// This component must have the LOCALE_RESOLVER_BEAN_NAME as it is searched by this name in
// DispatcherServlet.initLocaleResolver:524
@Component(LOCALE_RESOLVER_BEAN_NAME)
public class DataprepLocaleContextResolver extends AcceptHeaderLocaleResolver {

    private static final Logger LOGGER = getLogger(DataprepLocaleContextResolver.class);

    private static final Locale DEFAULT_LOCALE = Locale.US;

    private final Locale applicationLocale;

    public DataprepLocaleContextResolver(@Value("${dataprep.locale:en-US}") String configuredLocale) {
        setDefaultLocale(US);
        this.applicationLocale = resolveApplicationLocale(configuredLocale);
    }

    @Override
    public Locale getDefaultLocale() {
        return applicationLocale;
    }

    @Override
    public List<Locale> getSupportedLocales() {
        return Arrays.asList(Locale.getAvailableLocales());
    }

    private Locale resolveApplicationLocale(String configuredLocale) {
        Locale locale;
        if (StringUtils.isNotBlank(configuredLocale)) {
            try {
                locale = new Locale.Builder().setLanguageTag(configuredLocale).build();
                if (LocaleUtils.isAvailableLocale(locale)) {
                    LOGGER.debug("Setting application locale to configured {}", locale);
                } else {
                    locale = DEFAULT_LOCALE;
                    LOGGER.debug("Locale {} is not available. Defaulting to {}", configuredLocale, getDefaultLocale());
                }
            } catch (IllformedLocaleException e) {
                locale = DEFAULT_LOCALE;
                LOGGER.warn(
                        "Error parsing configured application locale: {}. Defaulting to {}. Locale must be in the form \"en-US\" or \"fr-FR\"",
                        configuredLocale, DEFAULT_LOCALE, e);
            }
        } else {
            locale = DEFAULT_LOCALE;
            LOGGER.debug("Setting application locale to default value {}", getDefaultLocale());
        }
        LOGGER.info("Application locale set to: '{}'", locale);
        return locale;
    }
}
