/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.dataprep.exception;

import static java.util.Collections.emptyList;

import java.util.Collection;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.talend.daikon.exception.error.ErrorCode;

public class ErrorCodeDto implements ErrorCode {

    private String product;

    private String group;

    private String code;

    private Integer httpStatus;

    private Collection<String> expectedContextEntries = emptyList();

    @Override
    public String getProduct() {
        return product;
    }

    public ErrorCodeDto setProduct(String product) {
        this.product = product;
        return this;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public ErrorCodeDto setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public String getCode() {
        return code;
    }

    public ErrorCodeDto setCode(String code) {
        this.code = code;
        return this;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus == null ? -1 : httpStatus;
    }

    public ErrorCodeDto setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    @Override
    public Collection<String> getExpectedContextEntries() {
        return expectedContextEntries;
    }

    public ErrorCodeDto setExpectedContextEntries(Collection<String> expectedContextEntries) {
        this.expectedContextEntries = expectedContextEntries;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("product", product)
                .append("group", group)
                .append("code", code)
                .append("httpStatus", httpStatus)
                .append("expectedContextEntries", expectedContextEntries)
                .toString();
    }
}
