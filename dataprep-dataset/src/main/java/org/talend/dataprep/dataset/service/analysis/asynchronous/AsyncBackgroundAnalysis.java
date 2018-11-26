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

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.talend.dataprep.dataset.event.AnalysisEventProcessingUtil;
import org.talend.dataprep.dataset.event.DatasetImportedEvent;
import org.talend.dataprep.dataset.event.DatasetUpdatedEvent;

/**
 * Compute statistics analysis on the full dataset.
 */
@SuppressWarnings("InsufficientBranchCoverage")
@Component
@Conditional(AsyncBackgroundAnalysis.AsyncBackgroundAnalysisConditon.class)
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

    @EventListener
    public void onEvent(DatasetUpdatedEvent event) {
        LOGGER.debug("Processing spring dataset imported event: {}", event);
        String datasetId = event.getSource().getId();
        analysisEventProcessingUtil.processAnalysisEvent(datasetId);
    }

    public static class AsyncBackgroundAnalysisConditon extends AllNestedConditions {

        AsyncBackgroundAnalysisConditon() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(name = "dataprep.event.listener", havingValue = "spring", matchIfMissing = true)
        static class springEventListener {
        }

        @ConditionalOnProperty(name = "dataset.asynchronous.analysis", havingValue = "true", matchIfMissing = true)
        static class asyncAnalysis {
        }
    }
}
