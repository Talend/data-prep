package org.talend.dataprep.preparation.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.dataset.adapter.DataCatalogClient;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.security.SecurityProxy;

@RunWith(MockitoJUnitRunner.class)
public class DataSetNameInjectionTest {

    @InjectMocks
    private DataSetNameInjection dataSetNameInjection;

    @Mock
    private Security security;

    @Mock
    private SecurityProxy securityProxy;

    @Mock
    private DataCatalogClient dataCatalogClient;

    @Mock
    protected PreparationRepository preparationRepository;

    @Test
    public void injectDatasetNameBasedOnId() {
        // given
        PersistentPreparation input = new PersistentPreparation();
        String dataSetId = "1234";
        input.setDataSetId(dataSetId);
        when(security.getTenantId()).thenReturn("tenant id");
        Dataset datasetMetadata = new Dataset();
        String datasetLabel = "dataset label";
        datasetMetadata.setLabel(datasetLabel);
        when(dataCatalogClient.getMetadata(dataSetId)).thenReturn(datasetMetadata);

        // when
        PersistentPreparation result = dataSetNameInjection.injectDatasetNameBasedOnId(input);

        // then
        assertEquals(datasetLabel, result.getDataSetName());
        verify(security).getTenantId();
        verifyNoMoreInteractions(security);
        verify(securityProxy).asTechnicalUserForDataSet();
        verify(securityProxy).releaseIdentity();
        verifyNoMoreInteractions(securityProxy);
        verify(dataCatalogClient).getMetadata(dataSetId);
        verifyNoMoreInteractions(dataCatalogClient);
        verify(preparationRepository).add(input);
        verifyNoMoreInteractions(preparationRepository);
    }

    @Test
    public void injectDatasetNameBasedOnId_alreadyHasADatasetName() {
        // given
        PersistentPreparation input = new PersistentPreparation();
        String dataSetId = "1234";
        input.setDataSetId(dataSetId);
        String datasetLabel = "dataset label";
        input.setDataSetName(datasetLabel);

        // when
        PersistentPreparation result = dataSetNameInjection.injectDatasetNameBasedOnId(input);

        // then
        assertEquals(datasetLabel, result.getDataSetName());
        verifyZeroInteractions(security);
        verifyZeroInteractions(securityProxy);
        verifyZeroInteractions(dataCatalogClient);
        verifyZeroInteractions(preparationRepository);
    }

    @Test
    public void injectDatasetNameBasedOnId_datasetClientError() {
        // given
        PersistentPreparation input = new PersistentPreparation();
        String dataSetId = "1234";
        input.setDataSetId(dataSetId);
        when(security.getTenantId()).thenReturn("tenant id");
        Dataset datasetMetadata = new Dataset();
        String datasetLabel = "dataset label";
        datasetMetadata.setLabel(datasetLabel);
        when(dataCatalogClient.getMetadata(dataSetId)).thenThrow(new TDPException());

        // when
        PersistentPreparation result = dataSetNameInjection.injectDatasetNameBasedOnId(input);

        // then
        assertNull(result.getDataSetName()); // dataset name has not changed in DTO
        verify(security).getTenantId();
        verifyNoMoreInteractions(security);
        verify(securityProxy).asTechnicalUserForDataSet();
        verify(securityProxy).releaseIdentity();
        verifyNoMoreInteractions(securityProxy);
        verify(dataCatalogClient).getMetadata(dataSetId);
        verifyNoMoreInteractions(dataCatalogClient);
        verifyZeroInteractions(preparationRepository);
    }
}
