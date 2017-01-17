/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.dataprep.exception;

import static java.util.stream.StreamSupport.stream;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.talend.daikon.exception.error.ErrorCode;

/**
 * Representation of an exception for the APIs.
 */
public class TdpExceptionDto {

    private String code;

    private String message;

    private String messageTitle;

    private Map<String, Object> context;

    private String cause;

    /**
     * Creates the DTO based on a {@link TDPException}. It handles the conversion code that would be serialization code.
     *
     * @param internal the internal form of {@link TDPException}.
     * @return the {@link TdpExceptionDto} ready to be serialized to external products
     */
    public static TdpExceptionDto from(TDPException internal) {
        ErrorCode errorCode = internal.getCode();
        String serializedCode = errorCode.getProduct() + '_' + errorCode.getGroup() + '_' + errorCode.getCode();
        String message = internal.getMessage();
        String messageTitle = internal.getMessageTitle();
        String cause = internal.getCause() == null ? null : internal.getCause().getMessage();
        Map<String, Object> context = stream(internal.getContext().entries().spliterator(), false)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return new TdpExceptionDto(serializedCode, cause, message, messageTitle, context);
    }

    public TdpExceptionDto(String code, String cause, String message, String messageTitle, Map<String, Object> context) {
        this.code = code;
        this.cause = cause;
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

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}
