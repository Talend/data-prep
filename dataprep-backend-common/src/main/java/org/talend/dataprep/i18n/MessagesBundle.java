//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================
package org.talend.dataprep.i18n;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

/**
 * This class provides i18n support and provides a simple way to access {@link ResourceBundleMessageSource} in current
 * Spring context.
 */
@Component
public class MessagesBundle implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(MessagesBundle.class);

    /**
     * Source resource bundle that holds all the actions name and parameters name. This is not autowired because this
     * class is meant to be called with the <b>static</b> method getString().
     */
    private static ResourceBundleMessageSource source;

    /**
     * Private constructor.
     */
    private MessagesBundle() {
    }

    /**
     * Returns the i18n string that corresponds to <code>code</code>. If no i18n string is to be found, returns
     * <code>code</code>.
     * 
     * @param code A i18n key.
     * @return The i18n message associated with <code>code</code>. Returns <code>null</code> if <code>code</code> is
     * <code>null</code>.
     * @see LocaleContextHolder#getLocale()
     */
    public static String getString(String code) {
        return getString(code, new String[0]);
    }

    /**
     * Returns the i18n string that corresponds to <code>code</code>. If no i18n string is to be found, returns
     * <code>defaultMessage</code>.
     *
     * @param code A i18n key.
     * @param defaultMessage the default message to use if <code>code</code> is not present
     * @return The i18n message associated with <code>code</code>. Returns <code>null</code> if <code>code</code> is
     * <code>null</code>.
     * @see LocaleContextHolder#getLocale()
     */
    public static String getString(String code, String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        return source.getMessage(code, new String[0], defaultMessage, locale);
    }

    /**
     * <p>
     * Similarly to {@link #getString(String)}, returns the i18n string that corresponds to <code>code</code>. If no
     * i18n string is to be found, returns <code>code</code>.
     * </p>
     * <p>
     * This overload takes arguments in case the i18n message specifies arguments (in the form of "{0}", "{1}"...).
     * </p>
     *
     * @param code A i18n key.
     * @return The i18n message associated with <code>code</code>. Returns <code>null</code> if <code>code</code> is
     * <code>null</code>.
     * @see LocaleContextHolder#getLocale()
     * @see java.text.MessageFormat
     */
    public static String getString(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return source.getMessage(code, args, locale);
    }

    /**
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        source = applicationContext.getBean(ResourceBundleMessageSource.class);
        LOG.info("Activated i18n messages ({}).", source);
    }
}
