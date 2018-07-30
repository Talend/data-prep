package org.talend.dataprep.maintenance.executor;

/**
 * ScheduleFrequency of a MaintenanceTask
 */
public enum ScheduleFrequency {
    /**
     * MaintenanceTask to be executed only once (one time and only).
     */
    ONCE,
    /**
     * MaintenanceTask to be executed repeatedly.
     */
    REPEAT,
    /**
     * MaintenanceTask to be executed every day between 3AM and 6AM (local JVM time).
     */
    NIGHT
}
