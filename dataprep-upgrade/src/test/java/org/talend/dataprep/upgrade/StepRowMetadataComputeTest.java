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

package org.talend.dataprep.upgrade;

import static org.mockito.BDDMockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;
import org.talend.dataprep.transformation.cache.TransformationMetadataCacheKey;
import org.talend.dataprep.transformation.service.TransformationService;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;
import org.talend.dataprep.upgrade.to_2_1_0_PE.Base_2_1_0_PE_Test;
import org.talend.dataprep.upgrade.to_2_1_0_PE.SetStepRowMetadata;

/**
 * Unit tests.
 *
 * @see SetStepRowMetadata
 */
@RunWith(MockitoJUnitRunner.class)
public class StepRowMetadataComputeTest {

    @InjectMocks
    private StepRowMetadataCompute task;

    @Mock
    private ContentCache contentCache;

    @Mock
    private CacheKeyGenerator cacheKeyGenerator;

    @Mock
    private PreparationRepository repository;

    @Mock
    private TransformationService service;

    @Mock
    private FolderRepository folderRepository;

    private List<PersistentPreparation> preparations;

    @Before
    public void setup() {
        PersistentPreparation firstPreparation = makePersistentPreparation("prepId1");
        PersistentPreparation secondPreparation = makePersistentPreparation("prepId2");
        preparations = Arrays.asList(firstPreparation, secondPreparation);

        // A first stream for count() (terminal operation), and a second one for the forEach()
        when(repository.list(PersistentPreparation.class))
                .thenReturn(preparations.stream()) //
                .thenReturn(preparations.stream());

        final CacheKeyGenerator.MetadataCacheKeyBuilder metadataBuilder = mock(CacheKeyGenerator.MetadataCacheKeyBuilder.class);
        when(cacheKeyGenerator.metadataBuilder()).thenReturn(metadataBuilder);
        when(metadataBuilder.preparationId(anyString())).thenReturn(metadataBuilder);
        when(metadataBuilder.sourceType(any())).thenReturn(metadataBuilder);
        when(metadataBuilder.stepId(anyString())).thenReturn(metadataBuilder);
        when(metadataBuilder.build()).thenReturn(mock(TransformationMetadataCacheKey.class));

        final CacheKeyGenerator.ContentCacheKeyBuilder contentBuilder = mock(CacheKeyGenerator.ContentCacheKeyBuilder.class);
        when(cacheKeyGenerator.contentBuilder()).thenReturn(contentBuilder);
        when(contentBuilder.preparationId(anyString())).thenReturn(contentBuilder);
        when(contentBuilder.sourceType(any())).thenReturn(contentBuilder);
        when(contentBuilder.stepId(anyString())).thenReturn(contentBuilder);
        when(contentBuilder.build()).thenReturn(mock(TransformationCacheKey.class));

    }

    private PersistentPreparation makePersistentPreparation(String id) {
        PersistentPreparation preparation = new PersistentPreparation();
        preparation.setId(id);
        return preparation;
    }

    @Test
    public void whenFolderNotFoundThenShouldNotUpdateStepRowMetadata() throws Exception {

        // given
        when(folderRepository.locateEntry(anyString(), any())).thenReturn(null);

        // when
        task.run();

        // then
        verify(service, never()).execute(any());
    }

    @Test
    public void shouldUpdateStepRowMetadataForEachPersistentPreparation() throws Exception {
        // given
        when(folderRepository.locateEntry(any(), any())).thenReturn(mock(Folder.class));
        when(service.execute(any(ExportParameters.class))).thenReturn(mock(StreamingResponseBody.class));

        // when
        task.run();

        // then
        verify(service, times(preparations.size())).execute(any());
    }

}