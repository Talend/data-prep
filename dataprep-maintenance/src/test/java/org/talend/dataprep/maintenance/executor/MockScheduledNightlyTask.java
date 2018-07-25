package org.talend.dataprep.maintenance.executor;

import java.util.function.Supplier;

@MaintenanceTask(ScheduleFrequency.NIGHT)
public class MockScheduledNightlyTask extends MaintenanceTaskProcess {

    @Override
    protected void performTask() {
        // do nothing
    }

    @Override
    protected Supplier<Boolean> condition() {
        return () -> true;
    }
}
