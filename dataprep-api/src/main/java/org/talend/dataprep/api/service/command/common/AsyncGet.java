package org.talend.dataprep.api.service.command.common;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.http.Header;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.async.AsyncExecutionMessage;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class AsyncGet<T> extends HystrixCommand<T> {

    private static final Logger LOGGER = getLogger(AsyncGet.class);

    private final Supplier<GenericCommand<T>> commandSupplier;

    private final ApplicationContext context;

    public AsyncGet(Supplier<GenericCommand<T>> commandSupplier, ApplicationContext context) {
        super(GenericCommand.ASYNC_GROUP);
        this.commandSupplier = commandSupplier;
        this.context = context;
    }

    public T run() {
        GenericCommand<T> command = commandSupplier.get();
        T result = command.execute();
        if (command.getStatus() == HttpStatus.ACCEPTED) {
            Header location = command.getHeader("Location");
            if (location != null) {
                final String asyncMethodStatusUrl = location.getValue();
                AsyncExecution asyncExecution = waitForAsyncMethodToFinish(command.getCommandGroup(), asyncMethodStatusUrl);
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
    private AsyncExecution waitForAsyncMethodToFinish(HystrixCommandGroupKey group, String asyncMethodStatusUrl) {
        boolean isAsyncMethodRunning;
        int nbLoop = 0;
        AsyncExecutionMessage executionStatus;
        do {
            executionStatus = GetAsyncStatus.create(context, group, asyncMethodStatusUrl).execute();

            AsyncExecution.Status asyncStatus = executionStatus.getStatus();
            isAsyncMethodRunning = asyncStatus.equals(AsyncExecution.Status.RUNNING)
                    || asyncStatus.equals(AsyncExecution.Status.NEW);

            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                LOGGER.error("cannot sleep", e);
            }
            nbLoop++;
        } while (isAsyncMethodRunning && nbLoop < 100);
        return executionStatus;
    }

}
