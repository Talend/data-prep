package org.talend.dataprep.maintenance.executor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.talend.dataprep.maintenance.BaseMaintenanceTest;

import java.util.ArrayList;
import java.util.List;

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
            // return true if it is odd, false if i is even
            boolean condition = (i % 2 == 0);
            when(listTask.get(i).condition()).thenReturn(() -> condition);
        }
    }

    @Test
    public void testScheduleOnce() {

        scheduler.launchOnceTask();

        // only task with ScheduleFrequency = ONCE are call
        verify(listTask.get(0), times(1)).performTask();
        verify(listTask.get(1), times(0)).performTask();

        listTask
                .stream() //
                .filter(task -> !(task instanceof MockScheduledOnceTask))
                .forEach(task -> {
                    verify(task, times(0)).performTask();
                    verify(task, times(0)).condition();
                    verify(task, times(0)).execute();
                });
    }

    @Test
    public void testScheduleNight() {

        scheduler.launchNightlyTask();

        // only task with ScheduleFrequency = ONCE are call
        verify(listTask.get(2), times(1)).performTask();
        verify(listTask.get(3), times(0)).performTask();
        verify(listTask.get(6), times(1)).performTask();

        listTask
                .stream() //
                .filter(task -> !(task instanceof MockScheduledNightlyTask))
                .forEach(task -> {
                    verify(task, times(0)).performTask();
                    verify(task, times(0)).condition();
                    verify(task, times(0)).execute();
                });
    }

    @Test
    public void testScheduleRepeat() {

        scheduler.launchRepeatlyTask();

        // only task with ScheduleFrequency = ONCE are call
        verify(listTask.get(4), times(1)).performTask();
        verify(listTask.get(5), times(0)).performTask();


        listTask
                .stream() //
                .filter(task -> !(task instanceof MockScheduledRepeatTask))
                .forEach(task -> {
                    verify(task, times(0)).performTask();
                    verify(task, times(0)).condition();
                    verify(task, times(0)).execute();
                });
    }
}
