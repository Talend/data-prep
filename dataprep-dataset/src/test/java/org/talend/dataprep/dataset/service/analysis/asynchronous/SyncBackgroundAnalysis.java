// ============================================================================
//
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

package org.talend.dataprep.dataset.service.analysis.asynchronous;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talend.dataprep.dataset.event.DatasetImportedEvent;
import org.talend.dataprep.dataset.event.DatasetUpdatedEvent;
import org.talend.dataprep.metrics.LogTimed;

/**
 * Synchronous analysis of a dataset used for unit / integration tests.
 */
@Component
@ConditionalOnProperty(name = "dataset.asynchronous.analysis", havingValue = "false")
public class SyncBackgroundAnalysis {

    @Autowired
    private BackgroundAnalysis backgroundAnalysis;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */

    @EventListener
    @LogTimed
    public void onEvent(DatasetImportedEvent event) {
        String dataSetId = event.getSource();
        backgroundAnalysis.analyze(dataSetId);
    }

    @EventListener
    @LogTimed
    public void onEvent(DatasetUpdatedEvent event) {
        String dataSetId = event.getSource().getId();
        backgroundAnalysis.analyze(dataSetId);
    }
}
