// ============================================================================
//
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
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.talend.daikon.exception.error.ErrorCode;

public enum APIErrorCodes implements ErrorCode {
    UNABLE_TO_DELETE_PREPARATION(BAD_REQUEST),
    UNABLE_TO_DELETE_PREPARATION_CACHE(INTERNAL_SERVER_ERROR),
    UNABLE_TO_CREATE_DATASET(BAD_REQUEST),
    UNABLE_TO_CREATE_OR_UPDATE_DATASET(BAD_REQUEST),
    UNABLE_TO_CERTIFY_DATASET(BAD_REQUEST),
    UNABLE_TO_DELETE_DATASET(BAD_REQUEST, "dataSetId"),
    UNABLE_TO_RETRIEVE_DATASET_CONTENT(BAD_REQUEST, "id"),
    UNABLE_TO_COPY_DATASET_CONTENT(BAD_REQUEST, "id"),
    UNABLE_TO_MOVE_DATASET_CONTENT(BAD_REQUEST, "id"),
    UNABLE_TO_RETRIEVE_DATASET_CONTENT_DATASET_NOT_READY(BAD_REQUEST, "id"),
    UNABLE_TO_RETRIEVE_DATASET_METADATA(BAD_REQUEST),
    UNABLE_TO_LIST_DATASETS(BAD_REQUEST),
    UNABLE_TO_LIST_COMPATIBLE_DATASETS(BAD_REQUEST),
    UNABLE_TO_LIST_COMPATIBLE_PREPARATIONS(BAD_REQUEST),
    UNABLE_TO_LIST_ERRORS(INTERNAL_SERVER_ERROR),
    UNABLE_TO_CREATE_PREPARATION(INTERNAL_SERVER_ERROR), // can also be HttpStatus.BAD_REQUEST ?
    UNABLE_TO_RETRIEVE_PREPARATION_LIST(BAD_REQUEST),
    UNABLE_TO_RETRIEVE_PREPARATION_CONTENT(BAD_REQUEST),
    UNABLE_TO_UPDATE_PREPARATION(BAD_REQUEST, "id"),
    UNABLE_TO_RETRIEVE_SUGGESTED_ACTIONS(BAD_REQUEST, "columnName", "dataSetId"),
    UNABLE_TO_SEND_MAIL(INTERNAL_SERVER_ERROR, "mail"),
    UNABLE_TO_FIND_COMMAND(INTERNAL_SERVER_ERROR, "class", "args"),
    UNABLE_TO_GET_PREPARATION_DETAILS(BAD_REQUEST),
    UNABLE_TO_GET_MAIL_DETAILS(BAD_REQUEST),
    UNABLE_TO_TRANSFORM_DATASET(BAD_REQUEST, "dataSetId"),
    UNABLE_TO_UPDATE_ACTION_IN_PREPARATION(BAD_REQUEST, "id"),
    UNABLE_TO_DELETE_ACTION_IN_PREPARATION(BAD_REQUEST, "id", "stepId"),
    UNABLE_TO_EXPORT_CONTENT(INTERNAL_SERVER_ERROR),
    UNABLE_TO_GET_DYNAMIC_ACTION_PARAMS(BAD_REQUEST),
    UNABLE_TO_SET_FAVORITE_DATASET(BAD_REQUEST, "id"),
    DATASET_STILL_IN_USE(CONFLICT, "dataSetId", "preparations"),
    UNABLE_TO_UPDATE_COLUMN(BAD_REQUEST, "id"),
    UNABLE_TO_GET_FOLDERS(BAD_REQUEST),
    UNABLE_TO_LIST_FOLDERS(BAD_REQUEST),
    UNABLE_TO_CREATE_FOLDER(BAD_REQUEST),
    UNABLE_TO_DELETE_FOLDER(BAD_REQUEST),
    UNABLE_TO_RENAME_FOLDER(BAD_REQUEST),
    UNABLE_TO_CREATE_FOLDER_ENTRY(BAD_REQUEST),
    UNABLE_TO_DELETE_FOLDER_ENTRY(BAD_REQUEST),
    UNABLE_TO_LIST_FOLDER_ENTRIES(BAD_REQUEST),
    UNABLE_TO_LIST_FOLDER_INVENTORY(BAD_REQUEST),
    UNABLE_TO_SEARCH_DATAPREP(INTERNAL_SERVER_ERROR),
    DATASET_REDIRECT(MOVED_PERMANENTLY),
    INVALID_HEAD_STEP_USING_DELETED_DATASET(FORBIDDEN),
    UNABLE_TO_IMPORT_PREPARATIONS(BAD_REQUEST),
    UNABLE_TO_OVERWRITE_PREPARATIONS(BAD_REQUEST);

    /**
     * The http status to use.
     */
    private HttpStatus httpStatus;

    /**
     * Expected entries to be in the context.
     */
    private List<String> expectedContextEntries;

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     */
    APIErrorCodes(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        this.expectedContextEntries = Collections.emptyList();
    }

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     * @param contextEntries expected context entries.
     */
    APIErrorCodes(HttpStatus httpStatus, String... contextEntries) {
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

    /**
     * @return the group.
     */
    @Override
    public String getGroup() {
        return "API"; //$NON-NLS-1$
    }

    /**
     * @return the http status.
     */
    @Override
    public int getHttpStatus() {
        return httpStatus.value();
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
