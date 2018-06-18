package org.talend.dataprep.dataset.service;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetLifecycle;
import org.talend.dataprep.api.dataset.DataSetLocation;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.dataset.service.analysis.synchronous.FormatAnalysis;
import org.talend.dataprep.dataset.service.analysis.synchronous.SynchronousDataSetAnalyzer;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.lock.DistributedLock;
import org.talend.dataprep.schema.FormatFamilyFactory;

@RunWith(MockitoJUnitRunner.class)
public class DataSetServiceImportTest {

    @InjectMocks
    private DataSetService dataSetService;

    @Mock
    private DataSetMetadataRepository repository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private DataSetMetadataBuilder builder;

    @Mock
    private FormatFamilyFactory formatFamilyFactory;

    @Mock
    private FormatAnalysis formatAnalysis;

    @Before
    public void setUp() throws Exception {
        when(repository.createDatasetMetadataLock(any())).thenReturn(mock(DistributedLock.class));

        when(builder.metadata()).thenReturn(builder);
        when(builder.copy(any())).thenReturn(builder);
        when(builder.build()).thenReturn(new DataSetMetadata());

        when(formatFamilyFactory.hasFormatFamily(anyString())).thenReturn(false);

        final DataSetMetadata metadata = mock(DataSetMetadata.class);
        when(metadata.getContent()).thenReturn(mock(DataSetContent.class));
        when(metadata.getLocation()).thenReturn(mock(DataSetLocation.class));
        when(metadata.getContent()).thenReturn(mock(DataSetContent.class));
        when(metadata.getLifecycle()).thenReturn(mock(DataSetLifecycle.class));
        when(repository.get(eq("ds-1234"))).thenReturn(metadata);
    }

    @Test
    public void shouldNotDeleteDataSetOnFailedAnalysis() {
        // given
        final SynchronousDataSetAnalyzer analysis = mock(SynchronousDataSetAnalyzer.class);
        doAnswer(invocation -> {
            throw new RuntimeException("On purpose failed analysis");
        }).when(analysis).analyze(eq("ds-1234"));
        dataSetService.setSynchronousAnalyzers(Collections.singletonList(analysis));

        // when
        try {
            dataSetService.updateDataSet("ds-1234", new DataSetMetadata());
            fail("Expected a failure during update");
        } catch (Exception e) {
            // Expected
        }

        // then
        verify(repository, times(1)).save(any());
        verify(repository, never()).remove(eq("ds-1234"));
    }

    @Test
    public void shouldNotDeleteDataSetOnSuccessfulAnalysis() {
        // given
        final SynchronousDataSetAnalyzer analysis = mock(SynchronousDataSetAnalyzer.class);
        dataSetService.setSynchronousAnalyzers(Collections.singletonList(analysis));

        // when
        final DataSetMetadata dataSetMetadata = new DataSetMetadata();
        dataSetService.updateDataSet("ds-1234", dataSetMetadata);

        // then
        verify(repository, times(1)).save(any());
        verify(repository, never()).remove("ds-1234");
    }

}