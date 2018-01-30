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

package org.talend.dataprep.upgrade;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.USER;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.talend.dataprep.upgrade.model.UpgradeTask;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;
import org.talend.dataprep.upgrade.repository.UpgradeTaskRepository;

/**
 * Unit test for the org.talend.dataprep.upgrade.UpgradeService class.
 *
 * @see UpgradeService
 */
@RunWith(MockitoJUnitRunner.class)
public class UpgradeServiceTest {

    /** The service to test. */
    @InjectMocks
    private UpgradeService service;

    /** A mock upgrade repository. */
    @Mock
    private UpgradeTaskRepository repository;

    /** List of upgrade tasks bound to the service. */
    private List<UpgradeTask> tasks = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(service, "tasks", tasks);
    }

    @After
    public void tearDown() throws Exception {
        reset(repository);
        tasks.clear();
    }

    @Test
    public void shouldSortTasks() throws Exception {
        // given
        tasks.add(new MockUpgradeTask("12.3-EE", "fourth", 4, USER)); // 4
        tasks.add(new MockUpgradeTask("1.4-EE", "second", 2, USER)); // 2
        tasks.add(new MockUpgradeTask("75.12.0-EE", "sixth", 6, USER)); // 6
        tasks.add(new MockUpgradeTask("100.5.0-EE", "seventh", 7, USER)); // 7
        tasks.add(new MockUpgradeTask("2.5-PE", "third", 3, USER)); // 3
        tasks.add(new MockUpgradeTask("1.3-PE", "first", 1, USER)); // 1
        tasks.add(new MockUpgradeTask("50.4-PE", "fifth", 5, USER)); // 5

        // when
        ReflectionTestUtils.invokeMethod(service, "sortAndCheckTasks");

        // then
        int i = 1;
        for (UpgradeTask task : tasks) {
            assertEquals(i, task.getId().getOrder());
            i++;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldDetectConflictTasks() throws Exception {
        // given
        tasks.add(new MockUpgradeTask("1.0.0", "one", 1, USER));
        tasks.add(new MockUpgradeTask("1.0.0", "second", 2, USER));
        tasks.add(new MockUpgradeTask("1.2.0", "one", 1, USER));
        tasks.add(new MockUpgradeTask("1.2.0", "second", 2, USER));
        tasks.add(new MockUpgradeTask("1.2.0", "second bis", 2, USER)); // conflict
        tasks.add(new MockUpgradeTask("1.2.0", "third", 3, USER));
        tasks.add(new MockUpgradeTask("1.3.0", "second", 2, USER));
        tasks.add(new MockUpgradeTask("1.3.0", "second bis", 2, USER)); // conflict

        // when
        ReflectionTestUtils.invokeMethod(service, "sortAndCheckTasks");
    }


    @Test
    public void shouldApplyTasksToUser() throws Exception {
        // given
        List<UpgradeTask> expected = new ArrayList<>();
        expected.add(new MockUpgradeTask("1.2.0-PE", "1.2.0-first", 1, USER));
        expected.add(new MockUpgradeTask("1.2.0-PE", "1.2.0-second", 2, USER));
        expected.add(new MockUpgradeTask("1.2.0-PE", "1.2.0-third", 3, USER));
        expected.add(new MockUpgradeTask("1.3.0-PE", "1.3.0-1-fourth", 1, USER));
        expected.add(new MockUpgradeTask("1.4.0-PE", "1.4.0-twelfth", 12, USER));
        expected.add(new MockUpgradeTask("2.12.0-EE", "2.12.0-fourth", 4, USER));
        expected.add(new MockUpgradeTask("10.5.0-EE", "2.12.0-fifth", 5, USER));

        List<UpgradeTask> unexpected = new ArrayList<>();
        unexpected.add(new MockUpgradeTask("1.2.0-PE", "1.2.0-second", 2, VERSION));
        unexpected.add(new MockUpgradeTask("1.3.0-PE", "1.3.0-1-fifth", 5, VERSION));
        unexpected.add(new MockUpgradeTask("1.4.0-PE", "1.4.0-eleventh", 11, VERSION));
        unexpected.add(new MockUpgradeTask("2.12.0-EE", "2.12.0-third", 3, VERSION));

        tasks.addAll(expected);
        tasks.addAll(unexpected);

        // when
        when(repository.isAlreadyApplied(anyString(), any())).thenReturn(false);
        service.upgradeUser("Mr Anonymous");

        // then
        for (UpgradeTask task : expected) {
            final MockUpgradeTask mockTask = (MockUpgradeTask) task;
            assertTrue(mockTask.applied);
        }
        for (UpgradeTask task : unexpected) {
            final MockUpgradeTask mockTask = (MockUpgradeTask) task;
            assertFalse(mockTask.applied);
        }
    }


    @Test
    public void shouldNotApplyUserTasks() throws Exception {
        // given
        tasks.add(new MockUpgradeTask("1.3-PE", "first", 1, USER));
        tasks.add(new MockUpgradeTask("1.4-EE", "second", 2, USER));
        tasks.add(new MockUpgradeTask("2.5-PE", "third", 3, USER));
        tasks.add(new MockUpgradeTask("12.3-EE", "fourth", 4, USER));
        tasks.add(new MockUpgradeTask("50.4-PE", "fifth", 5, USER));
        tasks.add(new MockUpgradeTask("2.12.0-EE", "sixth", 6, USER));
        tasks.add(new MockUpgradeTask("10.5.0-EE", "seventh", 7, USER));

        // when
        when(repository.isAlreadyApplied(anyString(), any())).thenReturn(true);
        service.upgradeUser("Mr Anonymous");

        // then
        for (UpgradeTask task : tasks) {
            final MockUpgradeTask mockTask = (MockUpgradeTask) task;
            assertFalse(mockTask.applied);
        }
    }

    @Test
    public void shouldApplyTasksForVersion() throws Exception {
        // given
        List<UpgradeTask> expected = new ArrayList<>();
        expected.add(new MockUpgradeTask("1.2.0-PE", "1.2.0-preums", 1, VERSION));
        expected.add(new MockUpgradeTask("1.2.0-PE", "1.2.0-deuz", 2, VERSION));
        expected.add(new MockUpgradeTask("1.2.0-PE", "1.2.0-troiz", 3, VERSION));

        List<UpgradeTask> unexpected = new ArrayList<>();
        unexpected.add(new MockUpgradeTask("1.2.0-PE", "1.2.0-deuz", 2, USER));
        unexpected.add(new MockUpgradeTask("1.3.0-PE", "1.3.0-1-troiz", 3, USER));
        unexpected.add(new MockUpgradeTask("1.4.0-PE", "1.4.0-preums", 1, USER));

        tasks.addAll(expected);
        tasks.addAll(unexpected);

        // when
        when(repository.isAlreadyApplied(anyString(), any())).thenReturn(false);
        service.upgradeVersion();

        // then
        for (UpgradeTask task : expected) {
            final MockUpgradeTask mockTask = (MockUpgradeTask) task;
            assertTrue(mockTask.applied);
        }
        for (UpgradeTask task : unexpected) {
            final MockUpgradeTask mockTask = (MockUpgradeTask) task;
            assertFalse(mockTask.applied);
        }
    }

    @Test
    public void shouldAllowUpgrade() {
        // given
        when(repository.countUpgradeTask(anyString())).thenReturn(0);
        final UpgradeTask upgradeTask = mock(UpgradeTask.class);
        when(upgradeTask.getTarget()).thenReturn(VERSION);
        service.setTasks(Collections.singletonList(upgradeTask));

        // when
        assertTrue(service.needUpgrade());
    }

    @Test
    public void shouldAllowUpgrade_tooManyTasksApplied() {
        // given
        when(repository.countUpgradeTask(anyString())).thenReturn(2);
        final UpgradeTask upgradeTask = mock(UpgradeTask.class);
        when(upgradeTask.getTarget()).thenReturn(VERSION);
        service.setTasks(Collections.singletonList(upgradeTask));

        // when
        assertTrue(service.needUpgrade());
    }

    @Test
    public void shouldNotAllowUpgrade() {
        // given
        when(repository.countUpgradeTask(anyString())).thenReturn(1);
        final UpgradeTask upgradeTask = mock(UpgradeTask.class);
        when(upgradeTask.getTarget()).thenReturn(VERSION);
        service.setTasks(Collections.singletonList(upgradeTask));

        // when
        assertFalse(service.needUpgrade());
    }

    /**
     * Mock Upgrade Task implementation for testing purpose only.
     */
    class MockUpgradeTask implements UpgradeTask {

        /** The upgrade task id. */
        private UpgradeTaskId id;

        /** True if this task was applied. */
        private boolean applied;

        /** The order this task should be applied. */
        private int order;

        /** This task target. */
        private target target;

        /**
         * Constructor.
         *
         * @param version the upgrade task version.
         * @param name the upgrade task name.
         * @param order the upgrade task order.
         */
        MockUpgradeTask(String version, String name, int order, target target) {
            this.id = new UpgradeTaskId(version, name, order);
            this.applied = false;
            this.order = order;
            this.target = target;
        }

        /**
         * @see UpgradeTask#getId()
         */
        @Override
        public UpgradeTaskId getId() {
            return this.id;
        }

        /**
         * To run the upgrade task.
         */
        @Override
        public void run() {
            this.applied = true;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public target getTarget() {
            return target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MockUpgradeTask that = (MockUpgradeTask) o;
            return applied == that.applied && Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, applied);
        }

        @Override
        public String toString() {
            return "MockUpgradeTask{" + "id=" + id + ", applied=" + applied + '}';
        }
    }
}