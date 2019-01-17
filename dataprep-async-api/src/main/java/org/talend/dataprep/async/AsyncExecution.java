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

package org.talend.dataprep.async;

import java.util.Comparator;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.async.progress.ExecutionProgress;
import org.talend.dataprep.exception.ErrorCodeDto;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.error.TransformationErrorCodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Models a asynchronous execution (e.g. Transformation or sampling...).
 */
// TODO Switch to immutable
@JsonRootName("execution")
public class AsyncExecution implements Comparable<AsyncExecution> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncExecution.class);

    /** The execution group (parent) id. */
    private String group;

    /** The execution id. */
    private String id;

    /** The execution detailed timing (start / stop...). */
    private Time time = new Time();

    /** The current execution status. */
    private Status status = Status.NEW;

    /** The execution result (e.g. transformation / sampling...). */
    @JsonInclude(value = JsonInclude.Include.NON_NULL, content = JsonInclude.Include.NON_NULL)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private AsyncExecutionResult result;

    /** The execution error code when it failed. */
    private ErrorCode error;

    /** The current execution progress. */
    private ExecutionProgress progress;

    private String userId;

    private String tenantId;

    private String dispatchId;

    /**
     * Default empty constructor.
     */
    public AsyncExecution() {
        this.group = StringUtils.EMPTY;
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Create an AsyncExecution for the given group id.
     *
     * @param group the group (parent) id.
     */
    public AsyncExecution(String group) {
        this.group = group;
        this.id = group + '-' + UUID.randomUUID().toString();
    }

    public AsyncExecution(String group, String id) {
        this.group = group;
        this.id = id;
    }

    public static Comparator<AsyncExecution> reverseStartDateComparator() {
        return Comparator.<AsyncExecution, Long> comparing(task -> task.getTime().getStartDate()).reversed();
    }

    public static Comparator<AsyncExecution> reverseEndDateComparator() {
        return Comparator.<AsyncExecution, Long> comparing(task -> task.getTime().getEndDate()).reversed();
    }

    /**
     * @return the detailed execution timing.
     */
    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    /**
     * @return the execution group (parent).
     */
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * @return the execution id.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the execution error code.0
     */
    public ErrorCode getError() {
        return error;
    }

    public void setError(ErrorCode error) {
        this.error = error;
    }

    public ExecutionProgress getProgress() {
        switch (status) {
        case DONE:
            return null;
        case NEW:
        case RUNNING:
        case CANCELLED:
        case FAILED:
        default:
            return progress;
        }
    }

    public void setProgress(ExecutionProgress progress) {
        this.progress = progress;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Setter for status. Method primary usage is for deserialization, for updating status see
     * {@link #updateExecutionState(Status)}.
     *
     * @param status The new {@link AsyncExecution.Status status} for this execution.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Update execution status to a new {@link AsyncExecution.Status status}. This update status takes care of start /
     * creation
     * times.
     *
     * @param status the new {@link AsyncExecution.Status status} for this execution.
     */
    public void updateExecutionState(Status status) {
        LOGGER.debug("Execution {} set to {}", id, status);
        this.status = status;
        switch (status) {
        case NEW:
            time.creationDate = System.currentTimeMillis();
            break;
        case RUNNING:
            time.startDate = System.currentTimeMillis();
            break;
        case CANCELLED:
        case FAILED:
        case DONE:
            time.endDate = System.currentTimeMillis();
            break;
        }
    }

    public void setException(Throwable error) {
        if (error instanceof TalendRuntimeException) {
            setError(((TalendRuntimeException) error).getCode());
        } else {
            setError(CommonErrorCodes.UNEXPECTED_EXCEPTION);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setResult(AsyncExecutionResult result) {
        this.result = result;
    }

    public AsyncExecutionResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "AsyncExecution{" + //
                "group='" + group + '\'' + //
                ", id='" + id + '\'' + //
                ", time=" + time + //
                ", status=" + status + //
                ", result=" + result + //
                ", error=" + error + //
                ", progress=" + progress + //
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AsyncExecution)) {
            return false;
        }

        AsyncExecution that = (AsyncExecution) o;

        if (!group.equals(that.group)) {
            return false;
        }
        return id.equals(that.id);

    }

    @JsonIgnore
    public boolean isResumable() {
        return this.getStatus() == Status.RUNNING || this.getStatus() == Status.NEW;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public int hashCode() {
        int result = group.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    public void setDispatchId(String dispatchId) {
        this.dispatchId = dispatchId;
    }

    public String getDispatchId() {
        return dispatchId;
    }

    @Override
    public int compareTo(AsyncExecution o) {
        // Ensure running executions are top of sort
        if (this.getStatus() == Status.RUNNING) {
            return -1;
        }
        if (o.getStatus() == Status.RUNNING) {
            return 1;
        }
        // Normal sort
        return Long.compare(o.getTime().getEndDate(), this.getTime().getEndDate());
    }

    /**
     * Different execution status.
     */
    public enum Status {
        /** New execution. */
        NEW,
        /** Running execution. */
        RUNNING,
        /** Cancelled execution. */
        CANCELLED,
        /** Failed execution. */
        FAILED,
        /** Finished execution. */
        DONE
    }

    /**
     * Models a detailed asynchronous execution timing.
     */
    public static class Time {

        /** Creation date. */
        @JsonProperty("creation")
        private long creationDate = System.currentTimeMillis();

        /** Starting date. */
        @JsonProperty("start")
        @JsonInclude(value = JsonInclude.Include.NON_DEFAULT, content = JsonInclude.Include.NON_DEFAULT)
        private long startDate;

        /** End date. */
        @JsonProperty("end")
        @JsonInclude(value = JsonInclude.Include.NON_DEFAULT, content = JsonInclude.Include.NON_DEFAULT)
        private long endDate;

        public long getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(long creationDate) {
            this.creationDate = creationDate;
        }

        public long getStartDate() {
            return startDate;
        }

        public void setStartDate(long startDate) {
            this.startDate = startDate;
        }

        public long getEndDate() {
            return endDate;
        }

        public void setEndDate(long endDate) {
            this.endDate = endDate;
        }

        @Override
        public String toString() {
            return "Time{" + "creationDate=" + creationDate + ", startDate=" + startDate + ", endDate=" + endDate + '}';
        }
    }
}
