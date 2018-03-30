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

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME;

import java.util.Arrays;
import java.util.IllformedLocaleException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Implementation of {@link LocaleResolver} that uses a {@link AcceptHeaderLocaleResolver} underneath. Locale used in
 * decreasing order of priority:
 * <ol>
 * <li>Locale in Accept-Language</li>
 * <li>Locale defined in
 * 
 * <code>
 * dataprep.locale
 * </code>
 * 
 * </li>
 * <li>US Locale: if no property is defined in
 * 
 * <code>
 * dataprep.locale
 * </code>
 * 
 * <b>OR</b> if property has an invalid value (malformed or not available).</li>
 * </ol>
 *
 * <p>
 * This component must have the {@link org.springframework.web.servlet.DispatcherServlet#LOCALE_RESOLVER_BEAN_NAME
 * LOCALE_RESOLVER_BEAN_NAME} as
 * it is searched by this name in
 * {@link org.springframework.web.servlet.DispatcherServlet#initLocaleResolver(org.springframework.context.ApplicationContext)
 * initLocaleResolver()}.
 * </p>
 */
@Component(LOCALE_RESOLVER_BEAN_NAME)
public class DataprepLocaleContextResolver implements LocaleResolver {

    private static final Logger LOGGER = getLogger(DataprepLocaleContextResolver.class);

    private static final Locale DEFAULT_LOCALE = Locale.US;

    private final AcceptHeaderLocaleResolver delegate = new AcceptHeaderLocaleResolver();

    public DataprepLocaleContextResolver(@Value("${dataprep.locale:en-US}") String configuredLocale) {
        delegate.setSupportedLocales(Arrays.asList(Locale.getAvailableLocales()));
        delegate.setDefaultLocale(resolveApplicationLocale(configuredLocale));
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
                    LOGGER.debug("Locale {} is not available. Defaulting to {}", configuredLocale,
                            delegate.getDefaultLocale());
                }
            } catch (IllformedLocaleException e) {
                locale = DEFAULT_LOCALE;
                LOGGER.warn(
                        "Error parsing configured application locale: {}. Defaulting to {}. Locale must be in the form \"en-US\" or \"fr-FR\"",
                        configuredLocale, DEFAULT_LOCALE, e);
            }
        } else {
            locale = DEFAULT_LOCALE;
            LOGGER.debug("Setting application locale to default value {}", delegate.getDefaultLocale());
        }
        LOGGER.info("Application locale set to: '{}'", locale);
        return locale;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return delegate.resolveLocale(request);
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        delegate.setLocale(request, response, locale);
    }
}
