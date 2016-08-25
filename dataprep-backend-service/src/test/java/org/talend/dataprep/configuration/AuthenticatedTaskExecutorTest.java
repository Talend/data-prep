package org.talend.dataprep.configuration;

import org.apache.cxf.common.i18n.Exception;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class AuthenticatedTaskExecutorTest {
    @Test
    public void execute() throws Exception, InterruptedException {
        // given
        final Authentication authentication = new UsernamePasswordAuthenticationToken(new Object(), new Object());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ThreadPoolTaskExecutor delegate = new ThreadPoolTaskExecutor();
        delegate.setWaitForTasksToCompleteOnShutdown(true);
        delegate.initialize();
        final TaskExecutor executor = AuthenticatedTaskExecutor.authenticated(delegate);

        final CountDownLatch lock = new CountDownLatch(1);

        // when
        executor.execute(() -> {
            final Authentication threadAuth = SecurityContextHolder.getContext().getAuthentication();

            if(threadAuth != authentication) {
                fail("Authentication is not the same as main thread");
            }
            else {
                lock.countDown();
            }
        });

        // then : wait for the thread to finish, it should not throw error
        final boolean isFinished = lock.await(1, SECONDS);
        assertThat(isFinished, Matchers.is(true));
    }
}
