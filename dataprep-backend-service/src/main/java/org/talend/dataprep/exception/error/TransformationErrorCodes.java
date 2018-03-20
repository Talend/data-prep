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
import java.util.Collections;
import java.util.List;

import org.talend.daikon.exception.error.ErrorCode;

/**
 * Transformation error codes.
 */
public enum TransformationErrorCodes implements ErrorCode {
    // 400
    BAD_LOOKUP_PARAMETER(400),
    UNABLE_TO_USE_EXPORT(400, "format"),
    EXPORT_IN_PROGRESS(400),
    UNSUPPORTED_SAMPLE_TYPE(400, "type"),
    UNSUPPORTED_SAMPLE_STATUS_UPDATE(400, "status"),
    UNABLE_TO_CANCEL_EXECUTION(400, "id"),
    // 403
    INSUFFICIENT_AUTHORITY(403),
    // 404
    UNKNOWN_DYNAMIC_ACTION(404, "value"),
    NO_RUNNING_SAMPLING(404, "preparation"),
    // 415
    OUTPUT_TYPE_NOT_SUPPORTED(415),
    // 500
    UNABLE_TO_COMPUTE_DATASET_ACTIONS(500),
    UNABLE_TO_TRANSFORM_DATASET(500),
    UNEXPECTED_EXCEPTION(500),
    UNABLE_TO_READ_LOOKUP_DATASET(500),
    UNABLE_CREATE_SAMPLE(500),
    UNABLE_TO_PERFORM_PREVIEW(500),
    UNABLE_TO_PERFORM_EXPORT(500),
    EXPORT_NOT_FOUND(404, "id"),
    UNABLE_TO_RESUME_EXECUTION(400, "id"),
    METADATA_NOT_FOUND(500);

    /** The http status to use. */
    private int httpStatus;

    /** Expected entries to be in the context. */
    private List<String> expectedContextEntries;

    /**
     * default constructor.
     *
     * @param httpStatusCode the http status to use.
     */
    TransformationErrorCodes(final int httpStatusCode) {
        this.httpStatus = httpStatusCode;
        this.expectedContextEntries = Collections.emptyList();
    }

    /**
     * default constructor.
     *
     * @param httpStatusCode the http status to use.
     * @param contextEntries expected context entries.
     */
    TransformationErrorCodes(final int httpStatusCode, final String... contextEntries) {
        this.httpStatus = httpStatusCode;
        this.expectedContextEntries = Arrays.asList(contextEntries);
    }

    /**
     * @return the product.
     */
    @Override
    public String getGroup() {
        return "TS"; //$NON-NLS-1$
    }

    /**
     * @return the group.
     */
    @Override
    public String getProduct() {
        return "TDP"; //$NON-NLS-1$
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
