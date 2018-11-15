// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.exception.error;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.talend.daikon.exception.error.ErrorCode;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Preparation error codes.
 */
public enum PreparationErrorCodes implements ErrorCode {
    PREPARATION_DOES_NOT_EXIST(NOT_FOUND, "id"),
    PREPARATION_STEP_DOES_NOT_EXIST(NOT_FOUND, "id", "stepId"),
    PREPARATION_STEP_CANNOT_BE_DELETED_IN_SINGLE_MODE(FORBIDDEN, "id", "stepId"),
    PREPARATION_STEP_CANNOT_BE_REORDERED(CONFLICT),
    PREPARATION_ROOT_STEP_CANNOT_BE_DELETED(FORBIDDEN, "id", "stepId"),
    UNABLE_TO_SERVE_PREPARATION_CONTENT(BAD_REQUEST, "id", "version"),
    UNABLE_TO_READ_PREPARATION(INTERNAL_SERVER_ERROR, "id", "version"),
    PREPARATION_NAME_ALREADY_USED(CONFLICT, "id", "name", "folder"),
    PREPARATION_NOT_EMPTY(CONFLICT, "id"),
    FORBIDDEN_PREPARATION_CREATION(FORBIDDEN),
    PREPARATION_VERSION_DOES_NOT_EXIST(NOT_FOUND, "id", "stepId"),
    EXPORTED_PREPARATION_VERSION_NOT_SUPPORTED(BAD_REQUEST),
    UNABLE_TO_READ_PREPARATIONS_EXPORT(BAD_REQUEST, "importVersion", "dataPrepVersion"),
    PREPARATION_ALREADY_EXIST(CONFLICT, "preparationName"),
    INVALID_PREPARATION(BAD_REQUEST, "message");

    /** The http status to use. */
    private int httpStatus;

    /** Expected entries to be in the context. */
    private List<String> expectedContextEntries;

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     * @param contextEntries expected context entries.
     */
    PreparationErrorCodes(HttpStatus httpStatus, String... contextEntries) {
        this.httpStatus = httpStatus.value();
        this.expectedContextEntries = Arrays.asList(contextEntries);
    }

    /**
     * @return the product.
     */
    @Override
    public String getProduct() {
        return "TDP"; //$NON-NLS-1$
    }

    @Override
    public String getGroup() {
        return "PS"; //$NON-NLS-1$
    }

    /**
     * @return the http status.
     */
    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * @return the expected context entries.
     */
    @Override
    public Collection<String> getExpectedContextEntries() {
        return expectedContextEntries;
    }

    @Override
    public String getCode() {
        return this.toString();
    }
}
