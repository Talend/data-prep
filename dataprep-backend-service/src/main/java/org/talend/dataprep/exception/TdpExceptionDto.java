/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.dataprep.exception;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.talend.daikon.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.talend.daikon.exception.ExceptionContext;

/**
 * Representation of an exception for the APIs.
 */
public class TdpExceptionDto {

    private String code;

    private String message;

    private String messageTitle;

    private Map<String, Object> context;

    private TdpExceptionDto cause;

    public TDPException toTdpException(HttpStatus httpStatus) {
        String completeErrorCode = getCode();
        ErrorCodeDto errorCodeDto = deserializeErrorCode(httpStatus, completeErrorCode);
        return new TDPException(errorCodeDto, null, getMessage(), getMessageTitle(), ExceptionContext.build().from(getContext()).put("cause", getCause()));
    }

    private static ErrorCodeDto deserializeErrorCode(HttpStatus httpStatus, String completeErrorCode) {
        String productCode = substringBefore(completeErrorCode, "_");
        String groupCode = substringBefore(substringAfter(completeErrorCode, "_"), "_"); //$NON-NLS-1$ //$NON-NLS-2$
        String errorCode;
        if (completeErrorCode == null) {
            errorCode = UNEXPECTED_EXCEPTION.getCode();
        } else {
            errorCode = substringAfter(completeErrorCode, productCode + '_' + groupCode + '_');
        }

        return new ErrorCodeDto()
                .setCode(errorCode)
                .setGroup(groupCode)
                .setProduct(productCode)
                .setHttpStatus(httpStatus == null ? null : httpStatus.value());
    }

    public TdpExceptionDto() {
    }

    public TdpExceptionDto(String code, TdpExceptionDto cause, String message, String messageTitle, Map<String, Object> context) {
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

    public TdpExceptionDto getCause() {
        return cause;
    }

    public void setCause(TdpExceptionDto cause) {
        this.cause = cause;
    }
}
