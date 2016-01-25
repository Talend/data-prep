package org.talend.dataprep.exception.error;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.api.dataset.DataSetLifecycle;

import static org.springframework.http.HttpStatus.*;


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
    DATASET_NAME_ALREADY_USED(BAD_REQUEST.value(), "id", "name", "folder"),
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
     * Error return when the uploaded content is mal formatted .
     */
    MALFORMATTED_CONTENT(BAD_REQUEST.value());

    /** The http status to use. */
    private int httpStatus;

    /** Expected entries to be in the context. */
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
