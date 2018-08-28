package org.talend.dataprep.api.service.command.common;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.async.AsyncExecution.Status.NEW;
import static org.talend.dataprep.async.AsyncExecution.Status.RUNNING;
import static org.talend.dataprep.command.GenericCommand.ASYNC_GROUP;
import static org.talend.dataprep.command.GenericCommand.DATASET_GROUP;
import static org.talend.dataprep.command.GenericCommand.FULLRUN_GROUP;
import static org.talend.dataprep.command.GenericCommand.PREPARATION_GROUP;
import static org.talend.dataprep.command.GenericCommand.TRANSFORM_GROUP;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.service.CommonAPI;
import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.async.AsyncExecutionMessage;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.google.common.base.Stopwatch;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class AsyncGet<T> extends HystrixCommand<T> {

    private static final Logger LOGGER = getLogger(AsyncGet.class);

    private static final long WAIT_TIME = 10;

    private static final TimeUnit WAIT_TIME_UNIT = TimeUnit.MINUTES;

    private final Supplier<GenericCommand<T>> commandSupplier;

    private CommonAPI commonAPI;

    public AsyncGet(Supplier<GenericCommand<T>> commandSupplier, CommonAPI commonAPI) {
        super(ASYNC_GROUP);
        this.commandSupplier = commandSupplier;
        this.commonAPI = commonAPI;
    }

    public T run() {
        GenericCommand<T> command = commandSupplier.get();
        T result = command.execute();
        if (command.getStatus() == HttpStatus.ACCEPTED) {
            Header location = command.getHeader(HttpHeaders.LOCATION);
            Header retryAfter = command.getHeader(HttpHeaders.RETRY_AFTER);
            if (location != null) {
                final String asyncMethodStatusUrl = location.getValue();
                final int retryDelaySeconds;
                if (retryAfter != null && StringUtils.isNumeric(retryAfter.getValue())) {
                    retryDelaySeconds = Integer.parseInt(retryAfter.getValue());
                } else {
                    retryDelaySeconds = 1;
                }

                AsyncExecution asyncExecution = waitForAsyncMethodToFinish(command.getCommandGroup(), asyncMethodStatusUrl,
                        retryDelaySeconds);
                if (asyncExecution.getStatus() == AsyncExecution.Status.DONE) {
                    result = commandSupplier.get().execute();
                } else {
                    // failed to execute async, throw exception
                    throw new TDPException(asyncExecution.getError());
                }
            } else {
                // No location, we can process async. Or we try the same call again and again
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION);
            }
        }
        return result;
    }

    /**
     * Ping (100 times max) async method status url in order to wait the end of the execution
     *
     * @param group
     * @param asyncMethodStatusUrl
     * @return the status of the async execution (is likely DONE or FAILED)
     */
    private AsyncExecution waitForAsyncMethodToFinish(HystrixCommandGroupKey group, String asyncMethodStatusUrl,
            int retryDelaySeconds) {
        AsyncExecutionMessage executionStatus;
        Stopwatch waitTimeStopWatch = Stopwatch.createStarted();
        // RegEx for a [/api/<service>]/queue/{id} URL to extract exec ID
        if (asyncMethodStatusUrl.matches(".*/queue/[a-f0-9_]+")) {
            String execId = StringUtils.substringAfterLast(asyncMethodStatusUrl, "/");
            GenericCommand.ServiceType service = getServiceFromGroup(group);
            boolean isAsyncMethodRunning;
            do {
                executionStatus = commonAPI.getQueue(service.name(), execId);
                AsyncExecution.Status asyncStatus = executionStatus.getStatus();
                isAsyncMethodRunning = RUNNING.equals(asyncStatus) || NEW.equals(asyncStatus);

                try {
                    TimeUnit.SECONDS.sleep(retryDelaySeconds);
                } catch (InterruptedException e) {
                    LOGGER.error("cannot sleep", e);
                }
            } while (isAsyncMethodRunning && waitTimeStopWatch.elapsed(WAIT_TIME_UNIT) < WAIT_TIME);
        } else {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION,
                    ExceptionContext.withBuilder().put("message", "Invalid async wait URL" + asyncMethodStatusUrl).build());
        }
        return executionStatus;
    }

    private static GenericCommand.ServiceType getServiceFromGroup(HystrixCommandGroupKey group) {
        GenericCommand.ServiceType service;
        if (group == TRANSFORM_GROUP) {
            service = GenericCommand.ServiceType.TRANSFORM;
        } else if (group == DATASET_GROUP) {
            service = GenericCommand.ServiceType.DATASET;
        } else if (group == PREPARATION_GROUP) {
            service = GenericCommand.ServiceType.PREPARATION;
        } else if (group == FULLRUN_GROUP) {
            service = GenericCommand.ServiceType.FULLRUN;
        } else {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION,
                    ExceptionContext.withBuilder().put("message", "unknown service" + group).build());
        }
        return service;
    }

}
