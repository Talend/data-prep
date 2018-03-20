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
import org.springframework.stereotype.Component;
import org.talend.dataprep.dataset.service.analysis.asynchronous.BackgroundAnalysis;
import org.talend.dataprep.security.SecurityProxy;

@Component
public class AnalysisEventProcessingUtil {

    @Autowired
    private BackgroundAnalysis backgroundAnalysis;

    @Autowired
    private SecurityProxy securityProxy;

    /**
     * Processing analysis event
     *
     * @param datasetId the id of the dataset to analyse
     */
    public void processAnalysisEvent(String datasetId) {
        try {
            securityProxy.asTechnicalUser();
            backgroundAnalysis.analyze(datasetId);
        } finally {
            securityProxy.releaseIdentity();
        }
    }
}
