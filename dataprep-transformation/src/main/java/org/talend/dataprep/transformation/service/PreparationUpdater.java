// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.command.preparation.UpdateStepRowMetadata;

/**
 * This service provides operation to update a preparation in preparation service. This is useful when transformation
 * service wants to update step's metadata once a transformation is over.
 */
@Service
public class PreparationUpdater {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationUpdater.class);

    @Autowired
    private ApplicationContext context;

    /**
     * Update a preparation step's metadata.
     *
     * @param preparationId the preparation id to update the step from.
     * @param steps the steps to update.
     */
    public void update(String preparationId, List<Step> steps) {

        LOGGER.debug("updating steps for preparation #{} : \n\t{}", preparationId, steps);

        context.getBean(UpdateStepRowMetadata.class, preparationId, steps).execute();
    }

}
