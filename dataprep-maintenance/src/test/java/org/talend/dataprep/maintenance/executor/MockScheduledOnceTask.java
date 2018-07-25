package org.talend.dataprep.maintenance.executor;

import java.util.function.Supplier;

@MaintenanceTask(ScheduleFrequency.ONCE)
public class MockScheduledOnceTask extends MaintenanceTaskProcess {

    @Override
    protected void performTask() {
        // do nothing
        System.out.println("MockScheduledOnceTask.performTask");

    }

    @Override
    protected Supplier<Boolean> condition() {
        System.out.println("MockScheduledOnceTask.condition");
        return () -> true;
    }
}
