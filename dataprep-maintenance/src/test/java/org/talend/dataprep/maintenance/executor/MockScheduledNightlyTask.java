package org.talend.dataprep.maintenance.executor;

import java.util.function.Supplier;

public class MockScheduledNightlyTask implements MaintenanceTaskProcess {

    @Override
    public void performTask() {
        // do nothing
    }

    @Override
    public Supplier<Boolean> condition() {
        return () -> true;
    }

    @Override
    public ScheduleFrequency getFrequency() {
        return ScheduleFrequency.NIGHT;
    }
}
