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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DatasetEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetEventListener.class);

    @Autowired
    private DatasetEventUtil datasetEventUtil;

    @EventListener
    public void onUpdate(DatasetUpdatedEvent event) {
        datasetEventUtil.performUpdateEvent(event.getSource().getId());
    }

    @EventListener
    public void onInsert(DatasetImportedEvent event){
        datasetEventUtil.performImportEvent(event.getSource());
    }
}
