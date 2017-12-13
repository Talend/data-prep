package org.talend.dataprep.maintenance.preparation;

import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PreparationRepository;

/**
 * A interface to mark all used {@link Step steps} in a {@link PreparationRepository}.
 */
public interface StepMarker {

    /**
     * Mark all used {@link Step steps} with given <code>marker</code>.
     *
     * @param repository The preparation repository to look for steps.
     * @param marker The marker to add on used steps.
     * @return {@link Result#COMPLETED} if implementation was not interrupted by a user action,
     * {@link Result#INTERRUPTED} if implementation was not able to carefully mark all used objects.
     */
    Result mark(PreparationRepository repository, String marker);

    /**
     * The result of the mark operation as returned by {@link #mark(PreparationRepository, String)}.
     */
    enum Result {
        /**
         * Mark operation is complete, cleaner may proceed with actual delete.
         */
        COMPLETED,
        /**
         * Mark operation was interrupted, cleaner must NOT proceed with actual delete.
         */
        INTERRUPTED
    }
}
