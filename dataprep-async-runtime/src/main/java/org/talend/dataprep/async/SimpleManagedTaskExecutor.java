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

import static java.util.Optional.ofNullable;
import static org.talend.dataprep.async.AsyncExecution.Status.DONE;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.async.progress.ExecutionContext;
import org.talend.dataprep.async.repository.ManagedTaskRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.transformation.pipeline.Signal;

/**
 * Managed task executor based on a local thread pool.
 *
 */
@Component
@ConditionalOnProperty(name = "execution.executor.local", matchIfMissing = true)
public class SimpleManagedTaskExecutor implements ManagedTaskExecutor {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleManagedTaskExecutor.class);

    /** Where the tasks are stored. */
    @Autowired
    private ManagedTaskRepository repository;

    /** The thread pool that execute the tasks. */
    @Autowired
    @Qualifier("managedTaskEngine")
    private AsyncListenableTaskExecutor delegate;

    @Autowired
    private Security security;

    /** List of tasks to run. */
    private final Map<String, ListenableFuture> futures = new ConcurrentHashMap<>();

    @Override
    public AsyncExecution resume(ManagedTaskCallable task, String executionId, AsyncExecutionResult resultUrl) {
        LOGGER.debug("Resuming execution '{}' from repository '{}'", executionId, repository);
        final AsyncExecution execution = repository.get(executionId);
        if (execution == null) {
            LOGGER.error("Execution #{} can be resumed (not found).", executionId);
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_RESUME_EXECUTION,
                    ExceptionContext.withBuilder().put("id", executionId).build());
        } else if (!execution.isResumable()) {
            // Execution is expected to be created as "RUNNING" or "NEW" before the dispatcher resumes it.
            LOGGER.error("Execution #{} can't be resumed (status is {}) for tenant: {}", execution.getId(),
                    execution.getStatus(), execution.getTenantId());
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_RESUME_EXECUTION,
                    ExceptionContext.withBuilder().put("id", executionId).build());
        }

        // Wrap callable to get the running status.
        final Callable wrapper = wrapTaskWithProgressInformation(task, execution);

        execution.setResult(resultUrl);

        ListenableFuture future = delegate.submitListenable(wrapper);
        future.addCallback(new AsyncListenableFutureCallback(execution));
        futures.put(execution.getId(), future);

        LOGGER.debug("Execution {} resumed for execution.", execution.getId());
        return execution;
    }

    /**
     * @see ManagedTaskExecutor#queue(ManagedTaskCallable, String, String, AsyncExecutionResult)
     */
    @Override
    public synchronized AsyncExecution queue(final ManagedTaskCallable task, String executionId, String groupId,
            AsyncExecutionResult resultUrl) {

        // Create async execution
        final AsyncExecution asyncExecution =
                ofNullable(groupId).map(s -> new AsyncExecution(groupId)).orElseGet(AsyncExecution::new);

        if (StringUtils.isNotEmpty(executionId)) {
            asyncExecution.setId(executionId);
        }

        asyncExecution.setUserId(security.getUserId());
        asyncExecution.setTenantId(security.getTenantId());
        repository.save(asyncExecution);

        // Wrap callable to get the running status.
        final Callable wrapper = wrapTaskWithProgressInformation(task, asyncExecution);

        asyncExecution.setResult(resultUrl);

        ListenableFuture future = delegate.submitListenable(wrapper);
        future.addCallback(new AsyncListenableFutureCallback(asyncExecution));
        futures.put(asyncExecution.getId(), future);

        LOGGER.debug("Execution {} queued for execution.", asyncExecution.getId());
        return asyncExecution;
    }

    /**
     * Wrap the given task with progress information and update execution in the repository.
     *
     * @param task the task to wrap.
     * @param asyncExecution the matching async execution monitor.
     * @return the tasks wrapped with progress information.
     */
    private Callable wrapTaskWithProgressInformation(Callable task, AsyncExecution asyncExecution) {
        return () -> {
            asyncExecution.updateExecutionState(AsyncExecution.Status.RUNNING);
            repository.save(asyncExecution);
            try {
                ExecutionContext.get().link(asyncExecution, Thread.currentThread(), repository);
                return task.call();
            } finally {
                ExecutionContext.get().unlink(Thread.currentThread());
            }
        };
    }

    /**
     * @see ManagedTaskRepository#get(String)
     */
    public AsyncExecution find(final String id) {
        LOGGER.debug("Request for execution #{}", id);
        return repository.get(id);
    }

    /**
     * @see ManagedTaskExecutor#cancel(String)
     */
    @Override
    public synchronized AsyncExecution cancel(final String id) {
        LOGGER.debug("Cancel execution #{}", id);
        final AsyncExecution asyncExecution = repository.get(id);
        if (asyncExecution != null) {
            if (asyncExecution.getStatus() == DONE) {
                throw new CancellationException();
            }

            try {
                ExecutionContext.get().notifySignal(asyncExecution, Signal.CANCEL);
            } catch (Exception e) {
                LOGGER.error("Unable to call cancel in execution context.", e);
            }
            try {
                final Optional<ListenableFuture> futureToCancel = ofNullable(futures.get(id));
                futureToCancel.ifPresent(tListenableFuture -> tListenableFuture.cancel(true));
            } catch (CancellationException e) {
                LOGGER.debug("Cancel task {} exception.", id, e);
            } finally {
                asyncExecution.updateExecutionState(AsyncExecution.Status.CANCELLED);
                repository.save(asyncExecution);
            }
        }
        return asyncExecution;
    }

    /**
     * @see ManagedTaskExecutor#stop(String)
     */
    @Override
    public AsyncExecution stop(String id) {
        final AsyncExecution asyncTask = find(id);
        if (asyncTask != null) {
            // call the signal handler to deal with the stop signal
            ExecutionContext.get().notifySignal(asyncTask, Signal.STOP);

            // update and save the async execution
            asyncTask.updateExecutionState(DONE);
            repository.save(asyncTask);
        }
        return asyncTask;
    }

    /**
     * ListenableFutureCallback for managed tasks to update the AsyncExecution status based on the tasks progress.
     */
    private class AsyncListenableFutureCallback<T> implements ListenableFutureCallback<T> {

        /** The async execution. */
        private final AsyncExecution asyncExecution;

        /**
         * Default constructor.
         *
         * @param asyncExecution the async execution.
         */
        AsyncListenableFutureCallback(AsyncExecution asyncExecution) {
            this.asyncExecution = asyncExecution;
        }

        /**
         * @see ListenableFutureCallback#onFailure(Throwable)
         */
        @Override
        public void onFailure(Throwable throwable) {
            if (throwable instanceof CancellationException) {
                LOGGER.info("Execution {} is cancelled.", asyncExecution.getId(), throwable);
            } else {
                LOGGER.error("Execution {} finished with error.", asyncExecution.getId(), throwable);
                try {
                    asyncExecution.setException(throwable);
                    asyncExecution.updateExecutionState(AsyncExecution.Status.FAILED);
                    asyncExecution.getTime().setEndDate(System.currentTimeMillis());

                } finally {
                    futures.remove(asyncExecution.getId());
                    repository.save(asyncExecution);
                }
            }
        }

        /**
         * @see ListenableFutureCallback#onSuccess(Object)
         */
        @Override
        public void onSuccess(T t) {
            if (t != null) {
                LOGGER.debug("Execution {} finished with success.", asyncExecution.getId());
                try {
                    if (t instanceof AsyncExecutionResult) {
                        // if the async method result an asyncExecutionResult then we override basic Url Result
                        asyncExecution.setResult((AsyncExecutionResult) t);
                    }
                    asyncExecution.updateExecutionState(AsyncExecution.Status.DONE);
                } finally {
                    futures.remove(asyncExecution.getId());
                    repository.save(asyncExecution);
                }
            }
        }
    }
}
