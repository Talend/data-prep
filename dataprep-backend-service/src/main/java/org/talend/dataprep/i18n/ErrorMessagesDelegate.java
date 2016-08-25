package org.talend.dataprep.i18n;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.exception.error.ErrorCode;

class ErrorMessagesDelegate {

    /**
     * The suffix of messages specified in the properties file.
     */
    private static final String MESSAGE_SUFFIX = ".MESSAGE";

    /**
     * The suffix of title messages specified in the properties file.
     */
    private static final String TITLE_SUFFIX = ".TITLE";

    private ErrorMessagesDelegate() {}

    public static String getErrorKey(ErrorCode errorCode) {
        return getMessagePrefix(errorCode) + MESSAGE_SUFFIX;
    }

    public static String getErrorTitleKey(ErrorCode errorCode) {
        return getMessagePrefix(errorCode) + TITLE_SUFFIX;
    }

    /**
     * Returns the prefix message according to the specified error code.
     *
     * @param errorCode the specified error code
     * @return the prefix message according to the specified error code
     */
    private static String getMessagePrefix(ErrorCode errorCode) {
        switch (errorCode.getHttpStatus()) {
        case 0:
            return "SERVICE_UNAVAILABLE";
        case 500:
            return "GENERIC_ERROR";
        default:
            String code = errorCode.getCode();
            return StringUtils.isNotBlank(code) ? code : "GENERIC_ERROR";
        }
    }
}
