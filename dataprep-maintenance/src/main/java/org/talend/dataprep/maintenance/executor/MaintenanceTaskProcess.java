package org.talend.dataprep.maintenance.executor;

import static org.talend.dataprep.maintenance.executor.ScheduleFrequency.REPEAT;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface MaintenanceTaskProcess {

    Logger LOGGER = LoggerFactory.getLogger(MaintenanceTaskProcess.class);

    /**
     * Execute maintenance task only if condition is TRUE
     */
    default void execute() {
        if( this.condition().get()){
            this.performTask();
        }
    }

    /**
     * Maintenance task schedule frequency.
     * <p>Default to {@link ScheduleFrequency#REPEAT}</p>
     * @see ScheduleFrequency
     * @return the schedule frequency of the task
     */
    default ScheduleFrequency getFrequency() {
        LOGGER.info("Maintenance task '{}' has no schedule indication, default to {}", this.getClass(), REPEAT);
        return REPEAT;
    }

    void performTask();

    Supplier<Boolean> condition();

}
