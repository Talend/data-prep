package org.talend.dataprep.configuration;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * Task Executor that replicate current security context
 */
public class AuthenticatedTaskExecutor implements AsyncListenableTaskExecutor {

    private final TaskExecutor delegate;

    private AuthenticatedTaskExecutor(TaskExecutor delegate) {
        this.delegate = delegate;
    }

    public static AsyncListenableTaskExecutor authenticated(AsyncListenableTaskExecutor executor) {
        return new AuthenticatedTaskExecutor(executor);
    }

    public static TaskExecutor authenticated(TaskExecutor executor) {
        return new AuthenticatedTaskExecutor(executor);
    }

    private AsyncListenableTaskExecutor asAsyncListenable() {
        if (!(delegate instanceof AsyncListenableTaskExecutor)) {
            throw new IllegalStateException("Delegate is not a async listenable executor (" + delegate.getClass() + ").");
        }
        return (AsyncListenableTaskExecutor) delegate;
    }

    private static Runnable toRunnable(Runnable runnable, Authentication authentication) {
        return () -> {
            try {
                if (authentication != null) {
                    final SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                    ctx.setAuthentication(authentication);
                    SecurityContextHolder.setContext(ctx);
                }
                runnable.run(); // NOSONAR not dealing with threads here but a runnable
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }

    private static <T> Callable<T> toCallable(Callable<T> callable, Authentication authentication) {
        return () -> {
            try {
                if (authentication != null) {
                    final SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                    ctx.setAuthentication(authentication);
                    SecurityContextHolder.setContext(ctx);
                }
                return callable.call();
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }

    @Override
    public void execute(Runnable task) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        delegate.execute(toRunnable(task, authentication));
    }

    @Override
    public ListenableFuture<?> submitListenable(Runnable runnable) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return asAsyncListenable().submitListenable(toRunnable(runnable, authentication));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> callable) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return asAsyncListenable().submitListenable(toCallable(callable, authentication));
    }

    @Override
    public void execute(Runnable runnable, long l) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        asAsyncListenable().execute(toRunnable(runnable, authentication), l);
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return asAsyncListenable().submit(toRunnable(runnable, authentication));
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return asAsyncListenable().submit(toCallable(callable, authentication));
    }
}