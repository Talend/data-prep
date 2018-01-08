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
import java.util.Collections;
import java.util.List;

import org.talend.daikon.exception.error.ErrorCode;

public enum CommonErrorCodes implements ErrorCode {
    UNEXPECTED_EXCEPTION(500),
    UNEXPECTED_CONTENT(BAD_REQUEST.value(), "message"),
    UNEXPECTED_SERVICE_EXCEPTION(500, "message"),
    UNABLE_TO_PARSE_JSON(400),
    UNABLE_TO_GET_SERVICE_VERSION(400),
    UNABLE_TO_WRITE_JSON(500),
    UNABLE_TO_SERIALIZE_TO_JSON(500),
    UNABLE_TO_COMPUTE_ID(500),
    UNABLE_TO_PRINT_PREPARATION(500),
    UNABLE_TO_READ_CONTENT(500),
    UNABLE_TO_PARSE_ACTIONS(500),
    UNABLE_TO_PARSE_REQUEST(400), // e.g IllegalArgumentException
    UNABLE_TO_CONNECT_TO_HDFS(500, "location"),
    UNSUPPORTED_ACTION_SCOPE(400),
    MISSING_ACTION_SCOPE_PARAMETER(400),
    BAD_ACTION_PARAMETER(400, "paramName"),
    BAD_AGGREGATION_PARAMETERS(400),
    UNABLE_TO_AGGREGATE(500),
    UNABLE_TO_SAVE_USER_DATA(500),
    UNABLE_TO_READ_USER_DATA(500, "id"),
    UNABLE_TO_SAVE_PREPARATION(500, "id"),
    UNABLE_TO_GET_PREPARATION(500, "id"),
    UNABLE_TO_READ_PREPARATION(500),

    ILLEGAL_ORDER_FOR_LIST(400, "order"),
    ILLEGAL_SORT_FOR_LIST(400, "sort"),
    UNABLE_TO_PARSE_FILTER(400),
    CONFLICT_TO_LOCK_RESOURCE(409, "id"),
    CONFLICT_TO_UNLOCK_RESOURCE(409, "id"),
    /**
     * Unable to connect TAC: address specified in configuration is most likely incorrect and leads to a connection timeout.
     */
    UNABLE_TO_CONNECT_TO_TAC(BAD_GATEWAY.value()),
    TAC_SERVER_UNREACHABLE(BAD_GATEWAY.value()),
    TIC_SERVER_UNREACHABLE(BAD_GATEWAY.value()),
    /**
     * Unable to connect TIC: address specified in configuration is most likely incorrect and leads to a connection timeout.
     */
    UNABLE_TO_CONNECT_TO_TIC(BAD_GATEWAY.value()),
    /**
     * Error thrown when data prep fails to list available tasks (for running remote jobs).
     */
    UNABLE_TO_LIST_REMOTE_TASKS(INTERNAL_SERVER_ERROR.value(), "error"),
    /**
     * Invalid credentials: TAC doesn't recognize the user/password as valid ones.
     */
    INVALID_TAC_CREDENTIALS(BAD_GATEWAY.value()),
    /**
     * TAC rights are not sufficient to complete operation∕.
     */
    INSUFFICIENT_RIGHTS_TAC_CREDENTIAL(BAD_GATEWAY.value()),
    /**
     * TAC rights are not sufficient to get the tasks list∕.
     */
    INSUFFICIENT_RIGHTS_TAC_LIST_TASKS(BAD_GATEWAY.value()),
    INSUFFICIENT_RIGHTS_TIC_LIST_FLOWS(BAD_GATEWAY.value()),
    /**
     * Error thrown when data prep fails to run remote job.
     */
    UNABLE_TO_RUN_REMOTE_JOB(BAD_GATEWAY.value()),
    /**
     * Error thrown when receiving content from a job takes too long.
     */
    NO_DATA_RECEIVED_FROM_TAC(BAD_GATEWAY.value()),
    /**
     * Error thrown when receiving content from a flow takes too long.
     */
    NO_DATA_RECEIVED_FROM_TIC(BAD_GATEWAY.value()),
    UNABLE_TO_LIST_REMOTE_EXECUTABLES(INTERNAL_SERVER_ERROR.value()),
    UNABLE_TO_EXECUTE_REMOTE_EXECUTABLE(INTERNAL_SERVER_ERROR.value()),
    /**
     * Job is already running: TAC only allows one run of the task.
     */
    JOB_ALREADY_RUNNING(BAD_GATEWAY.value()),
    /**
     * If we are unable to connect to streams
     */
    UNABLE_TO_CONNECT_TO_STREAMS(BAD_GATEWAY.value()),
    /**
     * If unable to access SCIM server whatever the reason. It will encapsulate the {@link org.talend.iam.common.exception.SCIMException}.
     */
    SCIM_CLIENT_ERROR(INTERNAL_SERVER_ERROR.value(), "message");

    /** The http status to use. */
    private int httpStatus;

    /** Expected entries to be in the context. */
    private List<String> expectedContextEntries;

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     */
    CommonErrorCodes(int httpStatus) {
        this.httpStatus = httpStatus;
        this.expectedContextEntries = Collections.emptyList();
    }

    /**
     * default constructor.
     *
     * @param httpStatus the http status to use.
     */
    CommonErrorCodes(int httpStatus, String... contextEntries) {
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
        return "ALL"; //$NON-NLS-1$
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
