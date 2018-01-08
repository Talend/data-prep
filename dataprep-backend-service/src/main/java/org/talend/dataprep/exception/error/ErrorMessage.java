//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.exception.error;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.i18n.DataprepBundle;

/**
 * Utility class used to return user-friendly (understandable) messages to the frontend.
 */
public class ErrorMessage {

    /**
     * The suffix of messages specified in the properties file.
     */
    public static final String MESSAGE_SUFFIX = ".MESSAGE";

    /**
     * The suffix of title messages specified in the properties file.
     */
    public static final String TITLE_SUFFIX = ".TITLE";

    /**
     * Default empty constructor.
     */
    private ErrorMessage() {
        // private constructor for this utility class
    }

    /**
     * Returns the desired message to send to the frontend according to the specified error code.
     *
     * @param errorCode the specified error code
     * @param values used to specify the message title
     * @return the desired message to send to the frontend according to the specified error code
     */
    public static String getMessage(ErrorCode errorCode, Object... values) {
        String title = getMessagePrefix(errorCode) + MESSAGE_SUFFIX;
        return DataprepBundle.message(title, values);
    }

    /**
     * Returns the desired error message in default locale according to the specified error code. This is useful for log messages.
     *
     * @param errorCode the specified error code
     * @param values used to specify the message title
     * @return the desired message to send to the frontend according to the specified error code
     */
    public static String getDefaultMessage(ErrorCode errorCode, Object... values) {
        String title = getMessagePrefix(errorCode) + MESSAGE_SUFFIX;
        return DataprepBundle.defaultMessage(title, values);
    }

    /**
     * Returns the desired message title to send to the frontend according to the specified error code.
     *
     * @param errorCode the specified error code
     * @param values used to specify the message title
     * @return the desired message title to send to the frontend according to the specified error code
     */
    public static String getMessageTitle(ErrorCode errorCode, Object... values) {
        String title = getMessagePrefix(errorCode) + TITLE_SUFFIX;
        return DataprepBundle.message(title, values);
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
            return StringUtils.isNotEmpty(code) ? code : "GENERIC_ERROR";
        }
    }

}
