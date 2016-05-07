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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.talend.daikon.exception.error.ErrorCode;

public enum APIErrorCodes implements ErrorCode {

                                                UNABLE_TO_DELETE_PREPARATION(400),
                                                UNABLE_TO_CREATE_DATASET(400),
                                                UNABLE_TO_CREATE_OR_UPDATE_DATASET(400),
                                                UNABLE_TO_CERTIFY_DATASET(400),
                                                UNABLE_TO_DELETE_DATASET(400, "dataSetId"),
                                                UNABLE_TO_RETRIEVE_DATASET_CONTENT(400, "id"),
                                                UNABLE_TO_COPY_DATASET_CONTENT(400, "id"),
                                                UNABLE_TO_MOVE_DATASET_CONTENT(400, "id"),
                                                UNABLE_TO_RETRIEVE_DATASET_CONTENT_DATASET_NOT_READY(400, "id"),
                                                UNABLE_TO_RETRIEVE_DATASET_METADATA(400),
                                                UNABLE_TO_LIST_DATASETS(400),
                                                UNABLE_TO_LIST_COMPATIBLE_DATASETS(400),
                                                UNABLE_TO_LIST_COMPATIBLE_PREPARATIONS(400),
                                                UNABLE_TO_LIST_ERRORS(500),
                                                UNABLE_TO_ACTIONS_TO_PREPARATION(400, "id"),
                                                UNABLE_TO_CREATE_PREPARATION(500), // can also be 400 ?
                                                UNABLE_TO_RETRIEVE_PREPARATION_LIST(400),
                                                UNABLE_TO_RETRIEVE_PREPARATION_CONTENT(400),
                                                UNABLE_TO_UPDATE_PREPARATION(400, "id"),
                                                UNABLE_TO_RETRIEVE_SUGGESTED_ACTIONS(400, "columnName", "dataSetId"),
                                                UNABLE_TO_SEND_MAIL(500, "mail"),
                                                UNABLE_TO_FIND_COMMAND(500, "class", "args"),
                                                UNABLE_TO_GET_PREPARATION_DETAILS(400),
                                                UNABLE_TO_GET_MAIL_DETAILS(400),
                                                UNABLE_TO_TRANSFORM_DATASET(400, "dataSetId"),
                                                UNABLE_TO_UPDATE_ACTION_IN_PREPARATION(400, "id"),
                                                UNABLE_TO_DELETE_ACTION_IN_PREPARATION(400, "id", "stepId"),
                                                UNABLE_TO_EXPORT_CONTENT(500),
                                                UNABLE_TO_GET_DYNAMIC_ACTION_PARAMS(400),
                                                UNABLE_TO_SET_FAVORITE_DATASET(400, "id"),
                                                DATASET_STILL_IN_USE(409, "dataSetId", "preparations"),
                                                UNABLE_TO_UPDATE_COLUMN(400, "id"),
                                                UNABLE_TO_GET_FOLDERS(400),
                                                UNABLE_TO_LIST_FOLDERS(400),
                                                UNABLE_TO_CREATE_FOLDER(400),
                                                UNABLE_TO_DELETE_FOLDER(400),
                                                UNABLE_TO_RENAME_FOLDER(400),
                                                UNABLE_TO_CREATE_FOLDER_ENTRY(400),
                                                UNABLE_TO_DELETE_FOLDER_ENTRY(400),
                                                UNABLE_TO_LIST_FOLDER_ENTRIES(400),
                                                UNABLE_TO_LIST_FOLDER_INVENTORY(400),
                                                UNABLE_TO_SEARCH_DATAPREP(500),
                                                UNABLE_TO_LIST_DATEPATTERNS(400),
                                                UNABLE_TO_CREATE_DATEPATTERN(400),
                                                DATASET_REDIRECT(301);


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
    APIErrorCodes(int httpStatus) {
        this.httpStatus = httpStatus;
        this.expectedContextEntries = Collections.emptyList();
    }

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     * @param contextEntries expected context entries.
     */
    APIErrorCodes(int httpStatus, String... contextEntries) {
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
