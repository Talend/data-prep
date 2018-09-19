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

import static org.talend.dataprep.async.AsyncExecution.Status.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.async.progress.ExecutionContext;
import org.talend.dataprep.async.repository.ManagedTaskRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.transformation.pipeline.Signal;

/**
 * Managed task executor that run tasks on demand and synchronously.
 */
@Component
@ConditionalOnProperty(name = "test.managed.tasks", havingValue = "onDemand")
public class OnDemandManagedTaskExecutor implements ManagedTaskExecutor {

    private final Map<String, Callable> tasks = new HashMap<>();

    /** Where the tasks are stored. */
    @Autowired
    private ManagedTaskRepository repository;

    @Override
    public AsyncExecution resume(ManagedTaskCallable task, String executionId, AsyncExecutionResult result) {
        final AsyncExecution execution = repository.get(executionId);
        if (execution == null) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_RESUME_EXECUTION,
                    ExceptionContext.withBuilder().put("id", executionId).build());
        } else if (execution.getStatus() != AsyncExecution.Status.NEW) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_RESUME_EXECUTION,
                    ExceptionContext.withBuilder().put("id", executionId).build());
        }

        execution.setResult(result);
        tasks.put(execution.getId(), task);
        repository.save(execution);

        return execution;
    }

    /**
     * @see ManagedTaskExecutor#queue(ManagedTaskCallable, String, String, AsyncExecutionResult)
     */
    @Override
    public AsyncExecution queue(ManagedTaskCallable task, String executionId, String groupId,
            AsyncExecutionResult result) {

        final Optional<String> optional = Optional.ofNullable(groupId);
        final AsyncExecution asyncExecution = optional.isPresent() ? new AsyncExecution(groupId) : new AsyncExecution();

        if (StringUtils.isNotEmpty(executionId)) {
            asyncExecution.setId(executionId);
        }

        asyncExecution.updateExecutionState(AsyncExecution.Status.NEW);
        asyncExecution.setResult(result);
        tasks.put(asyncExecution.getId(), task);
        repository.save(asyncExecution);

        return asyncExecution;
    }

    /**
     * Synchronously run the task that matches this id.
     *
     *
     * @param id the task id to execute.
     * @return the updated AsyncExecution.
     */
    public AsyncExecution run(String id) {
        final AsyncExecution execution = repository.get(id);

        ExecutionContext.get().link(execution, Thread.currentThread(), repository);

        execution.updateExecutionState(RUNNING);
        repository.save(execution);

        final Callable task = tasks.get(execution.getId());
        try {
            Object result = task.call();
            if (result instanceof AsyncExecutionResult) {
                // if the async method result an asyncExecutionResult then we override basic Url Result
                execution.setResult((AsyncExecutionResult) result);
            }
            execution.updateExecutionState(DONE);
        } catch (Exception e) {
            execution.updateExecutionState(FAILED);
            execution.setException(e);
        } finally {
            ExecutionContext.get().unlink(Thread.currentThread());
        }

        tasks.remove(execution.getId());
        repository.save(execution);
        return execution;
    }

    /**
     * @see ManagedTaskExecutor#cancel(String)
     */
    @Override
    public AsyncExecution cancel(String id) throws CancellationException {
        final AsyncExecution execution = repository.get(id);
        if (execution.getStatus() == DONE) {
            throw new CancellationException();
        }

        execution.updateExecutionState(AsyncExecution.Status.CANCELLED);
        repository.save(execution);

        tasks.remove(execution.getId());
        ExecutionContext.get().notifySignal(execution, Signal.CANCEL);

        return execution;
    }

    /**
     * @see ManagedTaskExecutor#stop(String)
     */
    @Override
    public AsyncExecution stop(String id) {
        final AsyncExecution execution = repository.get(id);
        if (execution != null) {
            execution.updateExecutionState(DONE);
            repository.save(execution);
            ExecutionContext.get().notifySignal(execution, Signal.STOP);
        }
        return execution;
    }

}
