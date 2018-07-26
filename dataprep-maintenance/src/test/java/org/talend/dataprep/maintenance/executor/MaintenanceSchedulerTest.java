package org.talend.dataprep.maintenance.executor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.talend.dataprep.maintenance.BaseMaintenanceTest;
import org.talend.dataprep.security.Security;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MaintenanceSchedulerTest extends BaseMaintenanceTest {

    @InjectMocks
    @Spy
    private MaintenanceScheduler scheduler;

    @Spy
    private List<MaintenanceTaskProcess> listTask = new ArrayList<>();

    @Mock
    private Security security;

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

        when(security.getTenantId()).thenReturn("TENANT_ID");
    }

    @Test
    public void testLaunchAlreadyRunningTask() {

        when(scheduler.isAlreadyRunning(any(String.class))).thenReturn(false);
        scheduler.launchOnceTask();

        // task are not running. We should call execute
        verify(listTask.get(0), times(1)).execute();

        // we reset the number of called method on the mock
        reset(listTask.get(0));

        when(scheduler.isAlreadyRunning(any(String.class))).thenReturn(true);
        scheduler.launchOnceTask();

        // task are running. We should call execute
        verify(listTask.get(0), times(0)).execute();

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

        // only task with ScheduleFrequency = NIGHT are call
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

        // only task with ScheduleFrequency = REPEAT are call
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
