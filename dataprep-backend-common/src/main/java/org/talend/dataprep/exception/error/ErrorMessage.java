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

package org.talend.dataprep.exception.error;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.i18n.MessagesBundle;

/**
 * Utility class used to return user-friendly (understandable) messages to the frontend.
 */
public class ErrorMessage {

    /**
     * The suffix of messages specified in the properties file.
     */
    public static String MESSAGE_SUFFIX = ".MESSAGE";

    /**
     * The suffix of title messages specified in the properties file.
     */
    public static String TITLE_SUFFIX = ".TITLE";

    /**
     * Returns the desired message to send to the frontend according to the specified error code.
     * 
     * @param errorCode the specified error code
     * @return the desired message to send to the frontend according to the specified error code
     */
    public static String getMessage(ErrorCode errorCode) {
        String title = getMessagePrefix(errorCode) + MESSAGE_SUFFIX;
        return MessagesBundle.getString(title);
    }

    /**
     * Returns the desired message title to send to the frontend according to the specified error code.
     * 
     * @param errorCode the specified error code
     * @return the desired message title to send to the frontend according to the specified error code
     */
    public static String getMessageTitle(ErrorCode errorCode) {
        String title = getMessagePrefix(errorCode) + TITLE_SUFFIX;
        return MessagesBundle.getString(title);
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