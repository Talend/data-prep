// ============================================================================
//
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

package org.talend.dataprep.exception.error;

import static org.springframework.http.HttpStatus.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.api.dataset.DataSetLifecycle;

/**
 * Dataset error codes.
 */
public enum DataSetErrorCodes implements ErrorCode {
    UNEXPECTED_IO_EXCEPTION(INTERNAL_SERVER_ERROR.value()),
    UNABLE_TO_READ_DATASET_CONTENT(INTERNAL_SERVER_ERROR.value()),
    UNABLE_TO_READ_REMOTE_DATASET_CONTENT(NOT_FOUND.value()),
    UNEXPECTED_JMS_EXCEPTION(INTERNAL_SERVER_ERROR.value()),
    UNABLE_TO_CLEAR_DATASETS(INTERNAL_SERVER_ERROR.value()),
    UNABLE_TO_DELETE_DATASET(BAD_REQUEST.value(), "dataSetId"),
    UNABLE_TO_STORE_DATASET_CONTENT(INTERNAL_SERVER_ERROR.value(), "id"),
    UNABLE_TO_ANALYZE_COLUMN_TYPES(INTERNAL_SERVER_ERROR.value()),
    UNABLE_TO_ANALYZE_DATASET_QUALITY(INTERNAL_SERVER_ERROR.value()),
    /**
     * Error returned in case the data set is in "importing" state,
     * meaning all mandatory analysis prevents service to correctly
     * serve data set's content.
     *
     * @see DataSetLifecycle#importing()
     */
    UNABLE_TO_SERVE_DATASET_CONTENT(BAD_REQUEST.value(), "id"),
    /**
     * this one will happen when user do something on data whereas the
     * data has been updated async in the backend and this action is not
     * possible anymore (i.e preview whereas this dataset do not need
     * any preview)
     */
    REDIRECT_CONTENT(MOVED_PERMANENTLY.value()),
    /**
     * Error returned in case user tries to access to a data set that
     * does not exist (or no longer exists).
     */
    DATASET_DOES_NOT_EXIST(BAD_REQUEST.value(), "id"),
    /**
     * Error returned when the json that contains the dataset location
     * cannot be parsed.
     */
    UNABLE_TO_READ_DATASET_LOCATION(BAD_REQUEST.value()),
    /**
     * Error returned in case user tries to access to a column that does
     * not exist (or no longer exists) for a data set.
     */
    COLUMN_DOES_NOT_EXIST(BAD_REQUEST.value(), "id"),
    /**
     * Error returned when the order is not supported.
     */
    ILLEGAL_ORDER_FOR_LIST(BAD_REQUEST.value(), "order"),
    /**
     * Error returned when the sort is not supported.
     */
    ILLEGAL_SORT_FOR_LIST(BAD_REQUEST.value(), "sort"),
    /**
     * Error returned when the dataset metadata could not be saved.
     */
    UNABLE_TO_STORE_DATASET_METADATA(INTERNAL_SERVER_ERROR.value(), "id"),
    /**
     * Error returned when the dataset metadata could not be read.
     */
    UNABLE_TO_READ_DATASET_METADATA(INTERNAL_SERVER_ERROR.value(), "id"),
    /**
     * This dataset name is already used
     */
    DATASET_NAME_ALREADY_USED(CONFLICT.value(), "id", "name", "folder"),
    /**
     * Error when a folder is not empty
     */
    FOLDER_NOT_EMPTY(CONFLICT.value()),
    /**
     * Error return when the uploaded content is not supported by any
     * {@link org.talend.dataprep.schema.FormatGuesser guesser}.
     */
    UNSUPPORTED_CONTENT(BAD_REQUEST.value()),

    /**
     * Error returned when the encoding of the uploaded content is not supported.
     */
    UNSUPPORTED_ENCODING(BAD_REQUEST.value()),
    /**
     * Error return when the uploaded content is mal formatted .
     */
    MALFORMATED_CONTENT(BAD_REQUEST.value()),
    /**
     * Error returned when there's an error fetching the list of
     * supported encodings.
     */
    UNABLE_TO_LIST_SUPPORTED_ENCODINGS(INTERNAL_SERVER_ERROR.value()),
    /**
     * Error returned when there's an error fetching the list of possible imports.
     */
    UNABLE_TO_LIST_SUPPORTED_IMPORTS(INTERNAL_SERVER_ERROR.value()),
    /**
     * Error thrown when a folder is... not... found !
     */
    FOLDER_NOT_FOUND(NOT_FOUND.value(), "path"),
    /**
     * Error thrown when a problem occurred while listing folder children.
     */
    UNABLE_TO_LIST_FOLDER_CHILDREN(INTERNAL_SERVER_ERROR.value(), "path"),
    /**
     * Error thrown when a folder could not be added.
     */
    UNABLE_TO_ADD_FOLDER(INTERNAL_SERVER_ERROR.value(), "path"),
    /**
     * Error thrown when a folder could not be added.
     */
    UNABLE_TO_RENAME_FOLDER(INTERNAL_SERVER_ERROR.value(), "path"),
    /**
     * Error thrown when a folder entry could not be added.
     */
    UNABLE_TO_ADD_FOLDER_ENTRY(INTERNAL_SERVER_ERROR.value(), "path"),
    /**
     * Error thrown when a folder entry could not be removed.
     */
    UNABLE_TO_REMOVE_FOLDER_ENTRY(INTERNAL_SERVER_ERROR.value(), "path"),
    /**
     * Error thrown when a folder could not be removed.
     */
    UNABLE_TO_DELETE_FOLDER(INTERNAL_SERVER_ERROR.value(), "path"),
    /**
     * Error thrown when a folder entry could not be moved.
     */
    UNABLE_TO_MOVE_FOLDER_ENTRY(INTERNAL_SERVER_ERROR.value()),
    /**
     * Error thrown when folder entries could not be listed.
     */
    UNABLE_TO_LIST_FOLDER_ENTRIES(INTERNAL_SERVER_ERROR.value(), "path", "type"),
    /**
     * Error thrown when a folder entry could not be read.
     */
    UNABLE_TO_READ_FOLDER_ENTRY(INTERNAL_SERVER_ERROR.value(), "path", "type"),
    /**
     * Error thrown when not able to receive content from a job.
     */
    UNABLE_TO_RECEIVE_CONTENT(INTERNAL_SERVER_ERROR.value()),
    /**
     * Error thrown when receiving content from a job takes too long.
     */
    TIMEOUT_RECEIVING_CONTENT(INTERNAL_SERVER_ERROR.value()),
    /**
     * Error thrown when data prep fails to run remote job.
     */
    UNABLE_TO_RUN_REMOTE_JOB(INTERNAL_SERVER_ERROR.value()),
    /**
     * Error thrown when data prep fails to list available tasks (for running remote jobs).
     */
    UNABLE_TO_LIST_REMOTE_TASKS(INTERNAL_SERVER_ERROR.value()),
    /**
     * Error thrown when data prep fails to create a new data set.
     */
    UNABLE_CREATE_DATASET(INTERNAL_SERVER_ERROR.value()),
    /** Error thrown when an error occurs while adding or updating a dataset. */
    UNABLE_TO_CREATE_OR_UPDATE_DATASET(INTERNAL_SERVER_ERROR.value()),
    /**
     * Error thrown when a datepattern could not be added.
     */
    UNABLE_TO_ADD_DATEPATTERN(INTERNAL_SERVER_ERROR.value(), "pattern"),
    /**
     * Error thrown when datepattern could not be listed.
     */
    UNABLE_TO_LIST_DATEPATTERNS(INTERNAL_SERVER_ERROR.value()),
    /**
     * Error thrown when a datepattern could not be added.
     */
    UNABLE_TO_REMOVE_DATEPATTERN(INTERNAL_SERVER_ERROR.value(), "pattern");

    /**
     * The http status to use.
     */
    private int httpStatus;

    /**
     * Expected entries to be in the context.
     */
    private List<String> expectedContextEntries;

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     */
    DataSetErrorCodes(int httpStatus) {
        this.httpStatus = httpStatus;
        this.expectedContextEntries = Collections.emptyList();
    }

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     * @param contextEntries expected context entries.
     */
    DataSetErrorCodes(int httpStatus, String... contextEntries) {
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
        return "DSS"; //$NON-NLS-1$
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
