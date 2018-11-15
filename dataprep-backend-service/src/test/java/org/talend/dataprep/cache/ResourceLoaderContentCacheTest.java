package org.talend.dataprep.cache;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.daikon.content.DeletableResource;
import org.talend.daikon.content.ResourceResolver;
import org.talend.dataprep.cache.loader.ResourceLoaderContentCache;

@RunWith(MockitoJUnitRunner.class)
public class ResourceLoaderContentCacheTest {

    @Mock
    private ResourceResolver resolver;

    @InjectMocks
    private ResourceLoaderContentCache resourceLoaderContentCache;

    @Test
    public void testEvictMatch1() throws IOException {

        DeletableResource r10 = createMockResource("transformation_prepId_datasetId_HEAD_other.12345");
        DeletableResource r11 = createMockResource("/cache/transformation_prepId_datasetId_HEAD_other.12345");
        DeletableResource r12 = createMockResource("/subDir/cache/transformation_prepId_datasetId_HEAD_other.12345");
        DeletableResource r20 = createMockResource("transformation_prepId2_datasetId_HEAD_other.12345");
        DeletableResource r21 = createMockResource("/cache/transformation_prepId2_datasetId_HEAD_other.12345");
        DeletableResource r22 = createMockResource("/subDir/cache/transformation_prepId2_datasetId_HEAD_other.12345");
        DeletableResource r30 = createMockResource("transformation-metadata_prepId_datasetId_HEAD_other.12345");
        DeletableResource r31 = createMockResource("/cache/transformation-metadata_prepId_datasetId_HEAD_other.12345");
        DeletableResource r32 =
                createMockResource("/subDir/cache/transformation-metadata_prepId_datasetId_HEAD_other.12345");
        DeletableResource r40 = createMockResource("transformation-metadata_prepId2_datasetId2_HEAD_other.12345");
        DeletableResource r41 =
                createMockResource("/cache/transformation-metadata_prepId2_datasetId2_HEAD_other.12345");
        DeletableResource r42 =
                createMockResource("/subDir/cache/transformation-metadata_prepId2_datasetId2_HEAD_other.12345");

        DeletableResource[] resources = { r10, r11, r12, r20, r21, r22, r30, r31, r32, r40, r41, r42 };
        when(resolver.getResources(anyString())).thenReturn(resources);

        ContentCacheKey metadataKey = new TransformationMetadataCacheKey("prepId", null, null, null);
        resourceLoaderContentCache.evictMatch(metadataKey);

        // we delete all the key with transformation-metadata_prepId
        verify(r30, times(1)).delete();
        verify(r31, times(1)).delete();
        verify(r32, times(1)).delete();

        // other are not delete
        verify(r10, times(0)).delete();
        verify(r11, times(0)).delete();
        verify(r12, times(0)).delete();

        verify(r20, times(0)).delete();
        verify(r21, times(0)).delete();
        verify(r22, times(0)).delete();

        verify(r40, times(0)).delete();
        verify(r41, times(0)).delete();
        verify(r42, times(0)).delete();

    }

    @Test
    public void testEvictMatch2() throws IOException {

        DeletableResource r10 = createMockResource("transformation_prepId_datasetId_HEAD_other.12345");
        DeletableResource r11 = createMockResource("/cache/transformation_prepId_datasetId_HEAD_other.12345");
        DeletableResource r12 = createMockResource("/subDir/cache/transformation_prepId_datasetId_HEAD_other.12345");
        DeletableResource r20 = createMockResource("transformation_prepId2_datasetId_HEAD_other.12345");
        DeletableResource r21 = createMockResource("/cache/transformation_prepId2_datasetId_HEAD_other.12345");
        DeletableResource r22 = createMockResource("/subDir/cache/transformation_prepId2_datasetId_HEAD_other.12345");
        DeletableResource r30 = createMockResource("transformation-metadata_prepId_datasetId_HEAD_other.12345");
        DeletableResource r31 = createMockResource("/cache/transformation-metadata_prepId_datasetId_HEAD_other.12345");
        DeletableResource r32 =
                createMockResource("/subDir/cache/transformation-metadata_prepId_datasetId_HEAD_other.12345");
        DeletableResource r40 = createMockResource("transformation-metadata_prepId2_datasetId2_HEAD_other.12345");
        DeletableResource r41 =
                createMockResource("/cache/transformation-metadata_prepId2_datasetId2_HEAD_other.12345");
        DeletableResource r42 =
                createMockResource("/subDir/cache/transformation-metadata_prepId2_datasetId2_HEAD_other.12345");

        DeletableResource[] resources = { r10, r11, r12, r20, r21, r22, r30, r31, r32, r40, r41, r42 };

        when(resolver.getResources(anyString())).thenReturn(resources);

        ContentCacheKey transfoKey = new TransformationCacheKey(null, "datasetId", null, null, null, null, null, null);
        resourceLoaderContentCache.evictMatch(transfoKey);

        // we delete all the key with "transformation_XXX_datasetId_XX
        verify(r10, times(1)).delete();
        verify(r11, times(1)).delete();
        verify(r12, times(1)).delete();

        verify(r20, times(1)).delete();
        verify(r21, times(1)).delete();
        verify(r22, times(1)).delete();

        // other are not delete

        verify(r30, times(0)).delete();
        verify(r31, times(0)).delete();
        verify(r32, times(0)).delete();

        verify(r40, times(0)).delete();
        verify(r41, times(0)).delete();
        verify(r42, times(0)).delete();

    }

    private DeletableResource createMockResource(String fileName) {
        DeletableResource resource = mock(DeletableResource.class);
        when(resource.getFilename()).thenReturn(fileName);
        return resource;

    }
}
