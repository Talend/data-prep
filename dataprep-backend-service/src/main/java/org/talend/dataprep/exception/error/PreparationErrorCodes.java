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

import static org.springframework.http.HttpStatus.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.talend.daikon.exception.error.ErrorCode;

/**
 * Preparation error codes.
 */
public enum PreparationErrorCodes implements ErrorCode {
    PREPARATION_DOES_NOT_EXIST(NOT_FOUND.value(), "id"),
    PREPARATION_STEP_DOES_NOT_EXIST(NOT_FOUND.value(), "id", "stepId"),
    PREPARATION_STEP_CANNOT_BE_DELETED_IN_SINGLE_MODE(FORBIDDEN.value(), "id", "stepId"),
    PREPARATION_STEP_CANNOT_BE_REORDERED(CONFLICT.value()),
    PREPARATION_ROOT_STEP_CANNOT_BE_DELETED(FORBIDDEN.value(), "id", "stepId"),
    UNABLE_TO_SERVE_PREPARATION_CONTENT(BAD_REQUEST.value(), "id", "version"),
    UNABLE_TO_READ_PREPARATION(INTERNAL_SERVER_ERROR.value(), "id", "version"),
    PREPARATION_NAME_ALREADY_USED(CONFLICT.value(), "id", "name", "folder"),
    PREPARATION_NOT_EMPTY(CONFLICT.value(), "id"),
    FORBIDDEN_PREPARATION_CREATION(FORBIDDEN.value()),
    PREPARATION_VERSION_DOES_NOT_EXIST(NOT_FOUND.value(), "id", "stepId"),
    EXPORTED_PREPARATION_VERSION_NOT_SUPPORTED(BAD_REQUEST.value()),
    UNABLE_TO_READ_PREPARATIONS_EXPORT(BAD_REQUEST.value(), "importVersion", "dataPrepVersion"),
    PREPARATION_ALREADY_EXIST(
            CONFLICT.value(),
            "newPreparationId",
            "oldPreparationId",
            "newPreparationName",
            "oldPreparationName"),
    INVALID_PREPARATION(BAD_REQUEST.value(), "message");


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
    PreparationErrorCodes(int httpStatus, String... contextEntries) {
        this.httpStatus = httpStatus;
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
