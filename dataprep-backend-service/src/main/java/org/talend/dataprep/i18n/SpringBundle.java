package org.talend.dataprep.i18n;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Base class for context locale aware message source.
 * <p>Pattern found in IntelliJ Idea 16 codebase.
 */
public abstract class SpringBundle implements MessagesBundle {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBundle.class);

    private final ResourceBundleMessageSource source;

    protected SpringBundle(String... baseNames) {
        source = new ResourceBundleMessageSource();
        source.setFallbackToSystemLocale(false);
        source.setBasenames(baseNames);
        source.setUseCodeAsDefaultMessage(true);
    }

    /**
     * Returns the i18n string that corresponds to <code>code</code>. If no i18n string is to be found, returns <code>code</code>.
     *
     * @param code A i18n key.
     * @return The i18n message associated with <code>code</code>. Returns <code>null</code> if <code>code</code> is
     * <code>null</code>.
     * @see LocaleContextHolder#getLocale()
     * @see java.text.MessageFormat
     */
    public String getMessage(String code, Object... args) {
        return getMessage(LocaleContextHolder.getLocale(), code, args);
    }

    public String getMessage(Locale locale, String code, Object... args) {
        return source.getMessage(code, args, locale);
    }

    @Override
    public String getString(Locale locale, String code) {
        return source.getMessage(code, new Object[0], locale);
    }

    @Override
    public String getString(Locale locale, String code, String defaultMessage) {
        try {
            return source.getMessage(code, new Object[0], locale);
        } catch (NoSuchMessageException e) {
            LOGGER.debug("No message found for '{}'.", code, e);
            return defaultMessage;
        }
    }

    @Override
    public String getString(Locale locale, String code, Object... args) {
        return source.getMessage(code, args, locale);
    }

}
