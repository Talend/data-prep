/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.dataprep.exception;

import java.util.Map;

/**
 * Representation of an exception for the APIs.
 */
public class TdpExceptionDto {

    private String code;

    /**
     * The message in the context independent locale for logging.
     */
    private String defaultMessage;

    private String message;

    private String messageTitle;

    private Map<String, Object> context;

    private TdpExceptionDto cause;

    public TdpExceptionDto() {
    }

    public TdpExceptionDto(String code, TdpExceptionDto cause, String defaultMessage, String message, String messageTitle, Map<String, Object> context) {
        this.code = code;
        this.cause = cause;
        this.defaultMessage = defaultMessage;
        this.message = message;
        this.messageTitle = messageTitle;
        this.context = context;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public TdpExceptionDto getCause() {
        return cause;
    }

    public void setCause(TdpExceptionDto cause) {
        this.cause = cause;
    }
}
