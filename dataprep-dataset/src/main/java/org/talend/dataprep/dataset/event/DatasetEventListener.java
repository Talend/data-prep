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

package org.talend.dataprep.dataset.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talend.dataprep.metrics.LogTimed;

@Component
@ConditionalOnProperty(name = "dataprep.event.listener", havingValue = "spring")
public class DatasetEventListener {

    @Autowired
    private DatasetEventUtil datasetEventUtil;

    @EventListener
    @LogTimed(needLogCorrelationId = true)
    public void onUpdate(DatasetUpdatedEvent event) {
        datasetEventUtil.performUpdateEvent(event.getSource().getId());
    }

    @EventListener
    @LogTimed(needLogCorrelationId = true)
    public void onInsert(DatasetImportedEvent event) {
        datasetEventUtil.performImportEvent(event.getSource());
    }
}
