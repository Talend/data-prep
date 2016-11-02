package org.talend.dataprep.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Base class for context locale aware message source.
 * <p>Pattern found in IntelliJ Idea 16 codebase.
 */
public abstract class AbstractBundle {

    private final ResourceBundleMessageSource source;

    protected AbstractBundle(String[] baseNames) {
        source = new ResourceBundleMessageSource();
        source.setFallbackToSystemLocale(false);
        source.setBasenames(baseNames);
        source.setUseCodeAsDefaultMessage(true);
    }

    protected AbstractBundle(String[] baseNames, AbstractBundle parent) {
        this(baseNames);
        source.setParentMessageSource(parent.getSource());
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
        return getSource().getMessage(code, args, locale);
    }

    protected MessageSource getSource() {
        return source;
    }
}
