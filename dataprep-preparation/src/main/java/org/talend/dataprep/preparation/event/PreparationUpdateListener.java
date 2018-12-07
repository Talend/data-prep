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

package org.talend.dataprep.preparation.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.StepRowMetadata;
import org.talend.dataprep.dataset.event.DatasetUpdatedEvent;
import org.talend.dataprep.metrics.LogTimed;

/**
 * <p>
 * This listener takes care of {@link StepRowMetadata} clean up when a dataset use as input for a preparation is
 * modified.
 * </p>
 * <p>
 * When a dataset is modified, its metadata may change, in this case previously computed {@link StepRowMetadata} must be
 * removed, so they will be re-computed on next preparation use.
 * </p>
 */
@Component
@ConditionalOnProperty(name = "dataprep.event.listener", havingValue = "spring")
public class PreparationUpdateListener {

    @Autowired
    private PreparationEventUtil preparationEventUtil;

    @EventListener
    @LogTimed(needLogCorrelationId = true)
    public void onUpdate(DatasetUpdatedEvent event) {
        preparationEventUtil.performUpdateEvent(event.getSource().getId());
    }

}
