package org.talend.dataprep.maintenance.executor;

/**
 * Schedule of a {@link MaintenanceTask task}.
 */
public enum Schedule {
    /**
     * {@link MaintenanceTask} to be executed only once (one time and only).
     */
    ONCE,
    /**
     * {@link MaintenanceTask} to be executed repeatedly.
     */
    REPEAT,
    /**
     * {@link MaintenanceTask} to be executed every day between 3AM and 6AM (local JVM time).
     */
    NIGHT
}
