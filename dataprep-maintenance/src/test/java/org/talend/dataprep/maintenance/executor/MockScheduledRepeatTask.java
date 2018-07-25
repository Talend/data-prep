package org.talend.dataprep.maintenance.executor;

import java.util.function.Supplier;

@MaintenanceTask(ScheduleFrequency.REPEAT)
public class MockScheduledRepeatTask extends MaintenanceTaskProcess {

    @Override
    protected void performTask() {
        // do nothing
    }

    @Override
    protected Supplier<Boolean> condition() {
        System.out.println("MockScheduledOnceTask.condition");
        return () -> true;
    }
}
