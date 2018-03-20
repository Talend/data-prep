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
import org.springframework.stereotype.Component;
import org.talend.dataprep.dataset.event.DataSetImportedEvent;
import org.talend.dataprep.event.AsyncApplicationListener;
import org.talend.dataprep.security.SecurityProxy;

/**
 * Compute statistics analysis on the full dataset.
 */
@SuppressWarnings("InsufficientBranchCoverage")
@Component
@ConditionalOnProperty(name = "dataset.asynchronous.analysis", havingValue = "true", matchIfMissing = true)
public class AsyncBackgroundAnalysis implements AsyncApplicationListener<DataSetImportedEvent> {

    @Autowired
    private BackgroundAnalysis backgroundAnalysis;

    @Autowired
    private SecurityProxy securityProxy;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(DataSetImportedEvent event) {
        String dataSetId = event.getSource();

        try {
            securityProxy.asTechnicalUser();
            backgroundAnalysis.analyze(dataSetId);
        } finally {
            securityProxy.releaseIdentity();
        }
    }
}
