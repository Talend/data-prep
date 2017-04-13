// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.actions;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DPErrorCode {

    @JsonProperty
    private String message;

    @JsonProperty(value = "messageTitle")
    private String messageTitle;

    @JsonProperty
    private String cause;

    @JsonProperty("code")
    private String code;

    @JsonProperty("context")
    private Map<String, Object> context = Collections.emptyMap();

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

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "DPErrorCode{" + "message='" + message + '\'' + ", messageTitle='" + messageTitle + '\'' + ", cause='" + cause
                + '\'' + ", code='" + code + '\'' + ", context=" + context + '}';
    }
}
