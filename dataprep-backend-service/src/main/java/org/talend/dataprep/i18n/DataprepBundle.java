package org.talend.dataprep.i18n;

import static org.talend.dataprep.i18n.ErrorMessagesDelegate.getErrorKey;
import static org.talend.dataprep.i18n.ErrorMessagesDelegate.getErrorTitleKey;

import java.util.Locale;

import org.springframework.stereotype.Component;
import org.talend.daikon.exception.error.ErrorCode;

/**
 * Singleton to access all dataprep messages. It can be used as parent bundle using {@link #getDataprepBundle()}.
 */
@Component
public class DataprepBundle extends SpringBundle {

    private static final DataprepBundle INSTANCE = new DataprepBundle();

    private DataprepBundle() {
        super("org.talend.dataprep.messages", "org.talend.dataprep.error_messages");
    }

    /**
     * Get an internationalized message from the dataprep message bundle. Locale is from
     * {@link org.springframework.context.i18n.LocaleContextHolder#getLocale() LocaleContextHolder.getLocale()}.
     *
     * @param key    the message key.
     * @param params the message parameters.
     * @return the internationalized message or the supplied key if no message is found.
     * @see org.springframework.context.MessageSource#getMessage(String, Object[], Locale)
     */
    public static String message(String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    /**
     * Returns the desired message to send to the frontend according to the specified error code. It fetches the message at:
     * {@code <error_code>.MESSAGE}.
     *
     * @param errorCode the specified error code
     * @param values    used to specify the message title
     * @return the desired message to send to the frontend according to the specified error code
     */
    public static String errorMessage(ErrorCode errorCode, Object... values) {
        return INSTANCE.getMessage(getErrorKey(errorCode), values);
    }

    /**
     * Returns the desired message title to send to the frontend according to the specified error code. It fetches the message at:
     * {@code <error_code>.TITLE}.
     *
     * @param errorCode the specified error code
     * @param values    used to specify the message title
     * @return the desired message title to send to the frontend according to the specified error code
     */
    public static String errorTitle(ErrorCode errorCode, Object... values) {
        return INSTANCE.getMessage(getErrorTitleKey(errorCode), values);
    }

    /**
     * Retrieve the singleton instance to use as bundle parent.
     */
    public static DataprepBundle getDataprepBundle() {
        return INSTANCE;
    }
}
