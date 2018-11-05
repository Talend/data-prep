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
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.command.preparation.GetStepRowMetadata;
import org.talend.dataprep.command.preparation.InvalidStepRowMetadata;
import org.talend.dataprep.command.preparation.UpdateStepRowMetadata;

/**
 * This service provides operation to update a preparation in preparation service. This is useful when transformation
 * service wants to update step's metadata once a transformation is over.
 */
@Component
public class RemoteStepMetadataRepository implements StepMetadataRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteStepMetadataRepository.class);

    @Autowired
    private ApplicationContext context;

    @Override
    public RowMetadata get(String stepId) {
        LOGGER.debug("getting step {} metadata", stepId);
        if (Step.ROOT_STEP.id().equals(stepId)) {
            LOGGER.debug("No metadata associated to root step.");
            return null;
        }
        final RowMetadata linkedRowMetadata = context.getBean(GetStepRowMetadata.class, stepId).execute();
        if (linkedRowMetadata == null) {
            LOGGER.debug("No step row metadata associated to step #{}", stepId);
        }
        return linkedRowMetadata;
    }

    @Override
    public void update(String stepId, RowMetadata rowMetadata) {
        LOGGER.debug("updating step {} metadata", stepId);
        context.getBean(UpdateStepRowMetadata.class, stepId, rowMetadata).execute();
    }

    @Override
    public void invalidate(String stepId) {
        context.getBean(InvalidStepRowMetadata.class, stepId).execute();
    }

}
