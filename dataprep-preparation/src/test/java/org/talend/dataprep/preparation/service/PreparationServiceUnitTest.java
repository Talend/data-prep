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

package org.talend.dataprep.preparation.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.audit.BaseDataprepAuditService;
import org.talend.dataprep.dataset.adapter.DatasetClient;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.info.Version;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.Security;

@RunWith(MockitoJUnitRunner.class)
public class PreparationServiceUnitTest {

    @Mock
    private BaseDataprepAuditService auditService;

    @Mock
    private VersionService versionService;

    @Mock
    private Security security;

    @Mock
    private DatasetClient datasetClient;

    @Mock
    private PreparationRepository preparationRepository;

    @Mock
    private FolderRepository folderRepository;

    @InjectMocks
    private PreparationService preparationService;

    @Before
    public void setUp() {
        mockAppVersion();
    }

    private void mockAppVersion() {
        when(versionService.version()).thenReturn(mock(Version.class));
    }

    @Test
    public void testCreateShouldLogAuditEventOnSuccess() {
        // given
        when(datasetClient.getDataSetMetadata(any())).thenReturn(mock(DataSetMetadata.class));

        // when
        preparationService.create(mock(Preparation.class), "folder-9012");

        // then
        verify(auditService, times(1)).auditPreparationCreation(any(), any(), any(), any(), any());
    }

    @Test(expected = RuntimeException.class)
    public void testCreateShouldNotLogAuditEventOnFailure() {
        // given
        when(datasetClient.getDataSetMetadata(any())).thenReturn(mock(DataSetMetadata.class));
        doThrow(new RuntimeException("on-purpose thrown exception"))
                .when(preparationRepository)
                .add(any(PersistentPreparation.class));

        // when
        preparationService.create(mock(Preparation.class), "folder-9012");

        // then
        verify(auditService, never()).auditPreparationCreation(any(), any(), any(), any(), any());
    }

}
