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

package org.talend.dataprep.upgrade.common;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;

@RunWith(MockitoJUnitRunner.class)
public class PreparationDatasetRowUpdaterTest {

    @InjectMocks
    private PreparationDatasetRowUpdater updater;

    @Mock
    private PreparationRepository preparationRepository;

    @Mock
    private DataSetMetadataRepository dataSetMetadataRepository;

    @Test
    public void updatePreparations() throws Exception {
        // given
        String datasetId = "dataset id";
        Preparation prep = new Preparation("prepId", "123456");
        prep.setDataSetId(datasetId);
        final List<Preparation> preparations = singletonList(prep);
        when(preparationRepository.list(Preparation.class)).thenReturn(preparations.stream());

        DataSetMetadata datasetMetadata = new DataSetMetadata();
        datasetMetadata.setRowMetadata(new RowMetadata());
        when(dataSetMetadataRepository.get(datasetId)).thenReturn(datasetMetadata);

        // when
        updater.updatePreparations();

        // then
        verify(preparationRepository, times(1)).list(Preparation.class);
        verify(preparationRepository, times(1)).add(prep);
        verify(dataSetMetadataRepository, only()).get(datasetId);
    }

}