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

import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;
import static org.talend.dataprep.async.AsyncAspectTestMockController.cancelled;
import static org.talend.dataprep.async.AsyncAspectTestMockController.stopped;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.async.repository.ManagedTaskRepository;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;

/**
 * Unit test for the org.talend.dataprep.async.AsyncAspect class.
 *
 * @see AsyncAspect
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(
        properties = { "test.managed.tasks=onDemand", "execution.executor.local=false", "preparation.store=in-memory" })
public class AsyncAspectTest {

    @Autowired
    private ApplicationContext context;

    /** The mock controller used to get the async operations from. */
    @Autowired
    private AsyncAspectTestMockController controller;

    @Autowired
    private ManagedTaskRepository repository;

    @Autowired
    private OnDemandManagedTaskExecutor executor;

    @Bean(name = "managedTaskEngine")
    public TaskExecutor getManagedTaskExecutorEngine() {
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(1);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(false);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    @After
    public void tearDown() throws Exception {
        repository.clear();
        AsyncAspectTestMockController.cancelled.set(false);
        AsyncAspectTestMockController.stopped.set(false);
    }

    @Test
    public void shouldCancel() throws Exception {
        // given
        assertFalse(cancelled.get());
        assertFalse(stopped.get());

        // when
        controller.callCancel();
        final Iterator<AsyncExecution> iterator = repository.list(CancelAndStopGroupIdGenerator.GROUP_ID).iterator();
        assertTrue(iterator.hasNext());
        final String executionId = iterator.next().getId();
        Thread run = new Thread(() -> executor.run(executionId));
        run.start();
        Thread.sleep(100);
        executor.cancel(executionId);

        // then
        assertTrue(cancelled.get());
        assertFalse(stopped.get());
    }

    @Test
    public void shouldNotCancelDoneExecutions() throws Exception {
        // given
        assertFalse(cancelled.get());
        assertFalse(stopped.get());

        // when
        controller.callCancel();
        final Iterator<AsyncExecution> iterator = repository.list(CancelAndStopGroupIdGenerator.GROUP_ID).iterator();
        assertTrue(iterator.hasNext());
        final AsyncExecution execution = iterator.next();
        Thread runThread = new Thread(() -> executor.run(execution.getId()));
        runThread.run();
        Thread.sleep(1200);

        // then
        assertEquals(AsyncExecution.Status.DONE, repository.get(execution.getId()).getStatus());
        try {
            executor.cancel(execution.getId());
            fail("Expected cancellation exception since execution is done.");
        } catch (CancellationException e) {
            // Expected
        }

        assertFalse(cancelled.get());
        assertFalse(stopped.get());
    }

    @Test
    public void shouldStop() throws Exception {
        // given
        assertFalse(cancelled.get());
        assertFalse(stopped.get());

        // when
        controller.callStop();

        final Iterator<AsyncExecution> iterator = repository.list(CancelAndStopGroupIdGenerator.GROUP_ID).iterator();
        assertTrue(iterator.hasNext());
        final AsyncExecution execution = iterator.next();
        // (wraps the execution in its own thread, it lasts 1000 ms)
        Thread runThread = new Thread(() -> executor.run(execution.getId()));
        runThread.run();
        // (after 200 ms, stop the execution)
        Thread.sleep(200);
        executor.stop(execution.getId());

        // then
        assertFalse(cancelled.get());
        assertTrue(stopped.get());
    }

    @Test
    public void shouldNotGetAnyGroupId() throws Exception {
        // when
        controller.noGroupName();

        // then
        assertGroupId("");
    }

    @Test
    public void shouldGetGroupIdFromAnnotation() throws Exception {
        // given
        String expectedGroupName = "super group name";

        // when
        controller.groupNameFromAnnotation("toto", expectedGroupName);

        // then
        assertGroupId(expectedGroupName);
    }

    @Test
    public void shouldGetGroupIdFromAsyncGroupKeyAnnotation() throws Exception {
        // given
        ExportParameters params = new ExportParameters();
        params.setDatasetId("ds#123");
        params.setPreparationId("prep#753");

        // when
        controller.groupNameFromAsyncGroupKeyAnnotation(params);

        // then
        assertGroupId(params.getPreparationId());
    }

    @Test
    public void shouldGetGroupIdFromSpringBean() throws Exception {
        // when
        controller.groupNameFromSpringBean(42);

        // then
        assertGroupId("Chuck Norris");
    }

    @Test
    public void shouldDealWithInvalidSpringBean() throws Exception {
        // when
        controller.invalidSpringBeanDefinition();

        // then
        assertGroupId("");
    }

    @Test
    public void shouldDealWithInvalidClass() throws Exception {
        // when
        controller.invalidGeneratorClass();

        // then
        assertGroupId("");
    }

    @Test
    public void shouldDealWithAsyncExecutionIdGenerator() throws Exception {
        // when
        final String executionId = "execution-1234";
        final AsyncExecution asyncExecution = new AsyncExecution();
        asyncExecution.setId(executionId);
        repository.save(asyncExecution);
        controller.executionIdFromPath(executionId);

        // then
        final List<AsyncExecution> executions = repository.list().collect(toList());
        assertEquals(1, executions.size());
        final AsyncExecution execution = executions.get(0);
        assertEquals(executionId, execution.getId());
        assertGroupId("");
    }

    /**
     * Assert that the only execution in the repository belongs to the given group.
     *
     * @param expectedGroupId the expected group id.
     */
    private void assertGroupId(String expectedGroupId) {
        final List<AsyncExecution> executions = repository.list().collect(toList());
        assertEquals(1, executions.size());
        final AsyncExecution execution = executions.get(0);
        assertEquals(expectedGroupId, execution.getGroup());
    }

    @Test
    public void testConditionalAsyncMethod() {

        // this controller should execute the method asynchronously only if the param number is pair

        List<AsyncExecution> executionsBefore = repository.list().collect(toList());
        controller.asyncOnlyPairNumber(1);
        List<AsyncExecution> executionsAfter = repository.list().collect(toList());

        // we sent 1. Method should not be executed asynchronously
        Assert.assertEquals(executionsBefore.size(), executionsAfter.size());

        executionsBefore = repository.list().collect(toList());
        controller.asyncOnlyPairNumber(2);
        executionsAfter = repository.list().collect(toList());

        // we sent 2. Method should be executed asynchronously
        Assert.assertEquals(executionsBefore.size() + 1, executionsAfter.size());

    }

    @Test
    public void testResultUrlGenerator() {

        controller.generateResultUrl(1);

        final List<AsyncExecution> executions = repository.list().collect(toList());
        assertEquals(1, executions.size());
        final AsyncExecution execution = executions.get(0);

        assertEquals("/url/result/1", execution.getResult().getDownloadUrl());
    }
}
