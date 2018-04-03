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

package org.talend.dataprep.async.progress;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.async.AsyncExecutionResult;
import org.talend.dataprep.async.repository.ManagedTaskRepository;
import org.talend.dataprep.transformation.pipeline.Signal;

/**
 * This class helps code that runs asynchronously to:
 * <ul>
 * <li>Execute custom code when asynchronous task is canceled.</li>
 * <li>Push information to asynchronous task monitor (e.g. for progress update)</li>
 * </ul>
 *
 * @see #get()
 */
public class ExecutionContext {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionContext.class);

    /** Default initial progress. */
    private static final Progress initialProgress = new Progress() {
    };

    /** Singleton for this class. */
    private static final ExecutionContext INSTANCE = new ExecutionContext();

    /** List of current progress entry per thread. */
    private final Map<Thread, ProgressEntry> currentProgress = new HashMap<>();

    /** Map of signals per execution id. */
    private final Map<String, BiConsumer<AsyncExecution, Signal>> signalHandlers = new FifoMap<>(1000);

    /**
     * Private constructor used to ensure the singleton.
     *
     * @see ExecutionContext#get()
     */
    private ExecutionContext() {
    }

    /**
     * @return The execution context for all async executions.
     */
    public static ExecutionContext get() {
        return INSTANCE;
    }

    /**
     * Link the current async execution to the current thread to be able to update the execution progress.
     *
     * @param asyncExecution the execution to link.
     * @param thread the current thread that executes the async task.
     * @param repository the async task repository.
     * @see #unlink(Thread)
     */
    public void link(AsyncExecution asyncExecution, Thread thread, ManagedTaskRepository repository) {
        final ProgressEntry entry = new ProgressEntry(asyncExecution, initialProgress, repository);
        currentProgress.put(thread, entry);
    }

    /**
     * Removes the link between an async execution and thread parameter. If thread was not previously linked to an execution no
     * exception is thrown.
     *
     * @param thread The thread to be unlink from its linked execution.
     * @see #link(AsyncExecution, Thread, ManagedTaskRepository)
     */
    public void unlink(Thread thread) {
        currentProgress.remove(thread);
    }

     /**
     * Result initialization entry point
     *
     * @param result Any Java bean (might even be null).
     */
     public void initResult(AsyncExecutionResult result) {
        final ProgressEntry entry = currentProgress.get(Thread.currentThread());

        if(entry.execution.getResult() != null) {
            LOGGER.warn("The result for execution #{} is already initialized", entry.execution.getId());
            return;
        }
        entry.execution.setResult(result);
        entry.repository.save(entry.execution);
     }


    /**
     * Push an arbitrary object as a "progress" notification. The object should a Java bean and may contain all getters / setters
     * that give information about the current running task.
     * needed to indicate information about the
     *
     * @param progress Any Java bean (might even be null).
     */
    public void push(ExecutionProgress progress) {
        final ProgressEntry entry = currentProgress.get(Thread.currentThread());
        if (entry != null) {
            entry.execution.setProgress(progress);
            entry.repository.save(entry.execution);
        } else {
            LOGGER.warn("Unable to publish progress (outside of a managed execution thread).");
        }
    }

    public void setStartTime() {
        final ProgressEntry entry = currentProgress.get(Thread.currentThread());
        if (entry == null) {
            LOGGER.warn("No execution found for current thread");
            return;
        }
        entry.execution.updateExecutionState(AsyncExecution.Status.NEW);
        entry.repository.save(entry.execution);

    }

    /**
     * @return The current {@link AsyncExecution execution} linked to current thread.
     * @see #link(AsyncExecution, Thread, ManagedTaskRepository)
     */
    public AsyncExecution currentExecution() {
        final ProgressEntry entry = currentProgress.get(Thread.currentThread());
        return entry != null ? entry.execution : null;
    }

    /**
     * Add the the given signal handler to the current async execution.
     *
     * @param signalHandler the signal handler.
     * @see ExecutionContext#notifySignal(AsyncExecution, Signal)
     */
    public void on(BiConsumer<AsyncExecution, Signal> signalHandler) {
        final AsyncExecution currentExecution = currentExecution();
        if (currentExecution != null) {
            signalHandlers.put(currentExecution.getId(), signalHandler);
        } else {
            LOGGER.error("No execution currently associated with thread '{}'.", Thread.currentThread());
        }

    }

    /**
     * Notify the signal to the given execution.
     *
     * @param execution the execution to notify.
     * @param signal the signal sent to the execution.
     */
    public void notifySignal(AsyncExecution execution, Signal signal) {
        if (execution == null) {
            return;
        }
        final BiConsumer<AsyncExecution, Signal> signalHandler = signalHandlers.get(execution.getId());
        if (signalHandler != null) {
            LOGGER.info("sending {} to execution #{}", signal, execution.getId());
            signalHandler.accept(execution, signal);
            signalHandlers.remove(execution.getId());
        } else {
            LOGGER.info("No signal handler for {} in execution {}.", signal, execution.getId());
        }
    }

    /**
     * Class used to group a progress, its execution and the task repository.
     */
    private static class ProgressEntry {

        /** The current progress. */
        Progress progress;

        /** The tasks repository. */
        ManagedTaskRepository repository;

        /** The current async execution. */
        AsyncExecution execution;

        /**
         * Constructor.
         *
         * @param asyncExecution the async execution.
         * @param progress the current execution progress.
         * @param repository the task repository.
         */
        private ProgressEntry(AsyncExecution asyncExecution, Progress progress, ManagedTaskRepository repository) {
            this.execution = asyncExecution;
            this.progress = progress;
            this.repository = repository;
        }
    }
}
