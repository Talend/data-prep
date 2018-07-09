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

package org.talend.dataprep.dataset.service.analysis.asynchronous;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.dataset.event.AnalysisEventProcessingUtil;
import org.talend.dataprep.dataset.event.DatasetImportedEvent;

@RunWith(MockitoJUnitRunner.class)
public class AnalysisEventProcessingTest {

    @InjectMocks
    private AsyncBackgroundAnalysis listener;

    @Mock
    private AnalysisEventProcessingUtil analysisEventProcessingUtil;

    @Test
    public void testEventLaunchAnalysis() {

        DatasetImportedEvent event = new DatasetImportedEvent("datasetId");
        listener.onEvent(event);

        verify(analysisEventProcessingUtil, times(1)).processAnalysisEvent(any());
    }

}
