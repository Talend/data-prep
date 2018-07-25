package org.talend.dataprep.maintenance.executor;

import org.talend.dataprep.maintenance.MaintenanceTaskProcess;

import java.util.function.Supplier;

@MaintenanceTask(ScheduleFrequency.REPEAT)
public class MockScheduledRepeatTask extends MaintenanceTaskProcess {

    private Boolean condition;

    public MockScheduledRepeatTask(boolean condition){
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
