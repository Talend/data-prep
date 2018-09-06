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

package org.talend.dataprep.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.logging.audit.Context;

@RunWith(MockitoJUnitRunner.class)
public class DataprepAuditServiceTest {

    @Mock
    private DataprepEventAuditLogger auditLogger;

    @InjectMocks
    private DataprepAuditService dataprepAuditService;

    @Test
    public void testAuditPreparationCreationWithAllNonNullArguments() {
        dataprepAuditService.auditPreparationCreation("My new preparation", "prep-1234", "The base dataset",
                "dataset-5678", "folder-9012");

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(auditLogger, times(1)).preparationCreated(contextCaptor.capture());
        assertThat(contextCaptor.getValue().values().toArray()).containsOnly("My new preparation", "prep-1234",
                "The base dataset", "dataset-5678", "folder-9012");
    }

    @Test
    public void testAuditPreparationCreationWithNullDatasetName() {
        dataprepAuditService.auditPreparationCreation("My new preparation", "prep-1234", null, "dataset-5678",
                "folder-9012");

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(auditLogger, times(1)).preparationCreated(contextCaptor.capture());
        assertThat(contextCaptor.getValue().values().toArray()).containsOnly("My new preparation", "prep-1234",
                "dataset-5678", "folder-9012");
    }

}
