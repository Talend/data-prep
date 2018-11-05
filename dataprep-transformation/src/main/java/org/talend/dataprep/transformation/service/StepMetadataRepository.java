package org.talend.dataprep.transformation.service;

import org.talend.dataprep.api.dataset.RowMetadata;

public interface StepMetadataRepository {

    /**
     * Get a preparation step's metadata.
     *
     * @param stepId the preparation step to retrieve metadata from.
     */
    RowMetadata get(String stepId);

    /**
     * Update a preparation step's metadata.
     *
     * @param stepId the preparation step to update.
     * @param rowMetadata the row metadata to associate with step.
     */
    void update(String stepId, RowMetadata rowMetadata);

    /**
     * Invalidate (remove) step metadata associated with <code>stepId</code>.
     *
     * @param stepId the preparation step to update.
     */
    void invalidate(String stepId);
}
