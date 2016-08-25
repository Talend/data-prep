package org.talend.dataprep.i18n;

import java.util.Locale;

public interface MessagesBundle {

    /**
     * Returns the i18n string that corresponds to <code>code</code>. If no i18n string is to be found, returns
     * <code>code</code>.
     *
     *
     * @param locale The locale to be used when looking for <code>code</code> message.
     * @param code A i18n key.
     * @return The i18n message associated with <code>code</code>. Returns <code>null</code> if <code>code</code> is
     * <code>null</code>. Returns <code>code</code> as is if message does not exist.
     */
    String getString(Locale locale, String code);

    /**
     * Returns the i18n string that corresponds to <code>code</code>. If no i18n string is to be found, returns
     * <code>defaultMessage</code>.
     *
     * @param locale The locale to be used when looking for <code>code</code> message.
     * @param code A i18n key.
     * @param defaultMessage the default message to use if <code>code</code> is not present
     * @return The i18n message associated with <code>code</code>. Returns <code>null</code> if <code>code</code> is
     * <code>null</code>.
     */
    String getString(Locale locale, String code, String defaultMessage);

    /**
     * <p>
     * Similarly to {@link #getString(Locale, String)}, returns the i18n string that corresponds to <code>code</code>. If no
     * i18n string is to be found, returns <code>code</code>.
     * </p>
     * <p>
     * This overload takes arguments in case the i18n message specifies arguments (in the form of "{0}", "{1}"...).
     * </p>
     *
     * @param locale The locale to be used when looking for <code>code</code> message.
     * @param code A i18n key.
     * @return The i18n message associated with <code>code</code>. Returns <code>null</code> if <code>code</code> is
     * <code>null</code>.
     * @see java.text.MessageFormat
     */
    String getString(Locale locale, String code, Object... args);
}
