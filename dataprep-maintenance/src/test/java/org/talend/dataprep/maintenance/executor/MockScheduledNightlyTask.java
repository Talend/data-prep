package org.talend.dataprep.maintenance.executor;

import org.talend.dataprep.maintenance.MaintenanceTaskProcess;

import java.util.function.Supplier;

@MaintenanceTask(ScheduleFrequency.NIGHT)
public class MockScheduledNightlyTask extends MaintenanceTaskProcess {

    private Boolean condition;

    public MockScheduledNightlyTask(boolean condition) {
        this.condition = condition;
    }

    @Override
    protected void performTask() {
        // do nothing
    }

    @Override
    protected Supplier<Boolean> condition() {
        return () -> condition;
    }
}
