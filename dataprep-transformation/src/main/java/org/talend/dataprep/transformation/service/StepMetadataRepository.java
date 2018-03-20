// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.command.preparation.GetStepRowMetadata;
import org.talend.dataprep.command.preparation.UpdateStepRowMetadata;

/**
 * This service provides operation to update a preparation in preparation service. This is useful when transformation
 * service wants to update step's metadata once a transformation is over.
 */
@Service
public class StepMetadataRepository {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StepMetadataRepository.class);

    @Autowired
    private ApplicationContext context;

    /**
     * Get a preparation step's metadata.
     *
     * @param stepId the preparation step to retrieve metadata from.
     */
    public RowMetadata get(String stepId) {
        LOGGER.debug("getting step {} metadata", stepId);
        return context.getBean(GetStepRowMetadata.class, stepId).execute();
    }

    /**
     * Update a preparation step's metadata.
     *
     * @param stepId the preparation step to update.
     * @param rowMetadata the row metadata to associate with step.
     */
    public void update(String stepId, RowMetadata rowMetadata) {
        LOGGER.debug("updating step {} metadata", stepId);
        context.getBean(UpdateStepRowMetadata.class, stepId, rowMetadata).execute();
    }
}
