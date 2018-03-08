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

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talend.dataprep.dataset.event.AnalysisEventProcessingUtil;
import org.talend.dataprep.dataset.event.DatasetImportedEvent;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Compute statistics analysis on the full dataset.
 */
@SuppressWarnings("InsufficientBranchCoverage")
@Component
@ConditionalOnProperty(name = "dataset.asynchronous.analysis", havingValue = "true", matchIfMissing = true)
//TODO a activer uniquement si pas kafka
public class AsyncBackgroundAnalysis {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = getLogger(AsyncBackgroundAnalysis.class);

    @Autowired
    private AnalysisEventProcessingUtil analysisEventProcessingUtil;



    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @EventListener
    public void onEvent(DatasetImportedEvent event) {
        LOGGER.debug("Processing spring dataset imported event: {}", event);
        String datasetId = event.getSource();
        analysisEventProcessingUtil.processAnalysisEvent(datasetId);
    }

}
