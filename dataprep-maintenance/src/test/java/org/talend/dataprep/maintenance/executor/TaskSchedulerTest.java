package org.talend.dataprep.maintenance.executor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.talend.dataprep.maintenance.BaseMaintenanceTest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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

        listTask.add(spy(MockScheduledOnceTask.class)); // conditional = true
        listTask.add(spy(MockScheduledOnceTask.class)); // conditional = false
        listTask.add(spy(MockScheduledNightlyTask.class)); // conditional = true
        listTask.add(spy(MockScheduledNightlyTask.class)); // conditional = false
        listTask.add(spy(MockScheduledRepeatTask.class)); // conditional = true
        listTask.add(spy(MockScheduledRepeatTask.class)); // conditional = false
        listTask.add(spy(MockScheduledNightlyTask.class)); // conditional = true

        for (int i = 0; i < listTask.size(); i++) {
            // return true if i is odd, false if i is even
            boolean condition = (i % 2 == 0);
            when(listTask.get(i).condition()).thenReturn(() -> {
                System.out.println("condition = " + condition);
                return condition;
            });
        }
    }

    @Test
    public void testScheduleOnce() {

        scheduler.launchOnceTask();

        verify(listTask.get(0), times(1)).performTask();
        verify(listTask.get(1), times(0)).performTask();

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
