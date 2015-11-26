package org.talend.dataprep.exception.error;

import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.api.dataset.DataSetLifecycle;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Folder error codes.
 */
public enum FolderErrorCodes implements ErrorCode {
    UNEXPECTED_IO_EXCEPTION(500),
    UNABLE_TO_DELETE_FOLDER(400, "folderId"),
    FOLDER_DOES_NOT_EXIST(400, "folderId");

    /** The http status to use. */
    private int httpStatus;

    /** Expected entries to be in the context. */
    private List<String> expectedContextEntries;

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     */
    FolderErrorCodes(int httpStatus) {
        this.httpStatus = httpStatus;
        this.expectedContextEntries = Collections.emptyList();
    }

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     * @param contextEntries expected context entries.
     */
    FolderErrorCodes(int httpStatus, String... contextEntries) {
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
