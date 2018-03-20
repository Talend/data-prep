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

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

/**
 * Interface for asynchronous managed task executor. This interface is responsible for the runtime part of asynchronous
 * execution (i.e. it does <b>not</b> store metric or information relative to the execution itself).
 *
 * @see org.talend.dataprep.async.repository.ManagedTaskRepository For query/update operations on execution results.
 */
public interface ManagedTaskExecutor {

    /**
     * Resume a previously queued task with a new callable.
     *
     * @param task the task to execute.
     * @param executionId the execution id of the task to resume.
     * @return the AsyncExecution that enables caller to monitor the execution.
     */
    AsyncExecution resume(ManagedTaskCallable task, String executionId, AsyncExecutionResult result);

    /**
     * Queue a task to execute.
     *
     * @param task the task to execute.
     * @param executionId the execution id to used in order to register the task
     * @param groupId the task group id.
     * @return the AsyncExecution that enables caller to monitor the execution.
     */
    AsyncExecution queue(ManagedTaskCallable task, String executionId, String groupId, AsyncExecutionResult result);

    /**
     * Cancel (stop) the task that matches the given task id.
     *
     * @param id the task id to cancel.
     * @return the updated async execution to monitor the task.
     * @throws CancellationException In case the execution cannot be cancelled (if execution is
     * {@link AsyncExecution.Status#DONE}).
     */
    AsyncExecution cancel(String id) throws CancellationException;

    /**
     * Stop the current async execution. Unlike cancel, all the work that has been done so far is kept.
     *
     * @param id the execution id.
     * @return the updated execution.
     */
    AsyncExecution stop(String id);

    interface ManagedTaskCallable<S> extends Callable<S> {

        Method getMethod();

        Object[] getArguments();

    }

}
