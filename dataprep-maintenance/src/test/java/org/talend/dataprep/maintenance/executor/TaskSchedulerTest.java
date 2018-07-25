package org.talend.dataprep.maintenance.executor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.talend.dataprep.maintenance.BaseMaintenanceTest;
import org.talend.dataprep.maintenance.MaintenanceTaskProcess;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TaskSchedulerTest extends BaseMaintenanceTest {

    @InjectMocks
    private TaskScheduler scheduler;

    @Spy
    private List<MaintenanceTaskProcess> listTask = new ArrayList<>();

    @Before
    public void init() {

        listTask.add(mock(MockScheduledOnceTask.class));
        listTask.add(mock(MockScheduledOnceTask.class));
        listTask.add(mock(MockScheduledNightlyTask.class));
        listTask.add(mock(MockScheduledNightlyTask.class));
        listTask.add(mock(MockScheduledRepeatTask.class));
        listTask.add(mock(MockScheduledRepeatTask.class));
        listTask.add(mock(MockScheduledNightlyTask.class));

//        listTask.add(TaskSchedulerTest.createMaintenanceTask(ScheduleFrequency.ONCE, true));
//        listTask.add(TaskSchedulerTest.createMaintenanceTask(ScheduleFrequency.ONCE, false));
//        listTask.add(TaskSchedulerTest.createMaintenanceTask(ScheduleFrequency.NIGHT, true));
//        listTask.add(TaskSchedulerTest.createMaintenanceTask(ScheduleFrequency.NIGHT, false));
//        listTask.add(TaskSchedulerTest.createMaintenanceTask(ScheduleFrequency.REPEAT, true));
//        listTask.add(TaskSchedulerTest.createMaintenanceTask(ScheduleFrequency.REPEAT, false));
//        listTask.add(TaskSchedulerTest.createMaintenanceTask(ScheduleFrequency.NIGHT, true));
    }

    @Test
    public void testScheduleOnce() {

        scheduler.launchOnceTask();

        verify(listTask.get(0), times(1)).execute();
        verify(listTask.get(1), times(0)).execute();

    }

    private static MaintenanceTaskProcess createMaintenanceTask(ScheduleFrequency frequency, boolean conditionResult) {
        switch (frequency) {
            case ONCE:
                return new MockScheduledOnceTask(conditionResult);
            case NIGHT:
                return new MockScheduledNightlyTask(conditionResult);
            case REPEAT:
                return new MockScheduledRepeatTask(conditionResult);
        }

        throw new IllegalArgumentException("Unkown frequency");
    }
}
