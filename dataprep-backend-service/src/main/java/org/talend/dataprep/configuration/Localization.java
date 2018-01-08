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

package org.talend.dataprep.configuration;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * This class is used to set default JVM locale at the initialization.
 * It's not a duplication of DataprepLocaleContextResolver because it only deals with JVM default Locale and not with
 * EE Spring context Locale
 */
@Configuration
@Import(Localization.LocaleRegistrar.class)
public class Localization {

    /**
     * A {@link ImportBeanDefinitionRegistrar} allows code to be executed <b>before</b> any bean creation, which is the
     * main point of this class (in case some classes are locale-dependent in their constructors.
     */
    public static class LocaleRegistrar implements ImportBeanDefinitionRegistrar {

        private static final Logger LOGGER = getLogger(Localization.class);

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {
            if (registry instanceof BeanFactory) {
                final Environment environment = ((BeanFactory) registry).getBean(Environment.class);

                final String defaultLocale = environment.getProperty("dataprep.locale", "en-US");
                Locale locale = new Locale.Builder().setLanguageTag(defaultLocale).build();
                if (LocaleUtils.isAvailableLocale(locale)) {
                    LOGGER.debug("Setting default JVM locale to configured {}", locale);
                } else {
                    LOGGER.debug("Configured JVM Locale {} is not available. Defaulting to {}", locale, Locale.US);
                    locale = Locale.US;
                }
                Locale.setDefault(locale);
                LOGGER.info("JVM Default locale set to: '{}'", locale);
            } else {
                LOGGER.warn("Unable to set default locale (unexpected bean registry of type '{}')",
                        registry.getClass().getName());
            }
        }
    }
}
