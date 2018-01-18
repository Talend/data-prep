//  ============================================================================
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.util;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationMetadataCacheKey;
import org.talend.dataprep.transformation.service.TransformationServiceBaseTest;

import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;

public class MetadataGeneratorTest extends TransformationServiceBaseTest {

    @Autowired
    private MetadataGenerator metadataGenerator;

    @Autowired
    protected ContentCache contentCache;

    @Autowired
    protected CacheKeyGenerator cacheKeyGenerator;

    @Test
    public void testGenerateMetadataForPreparation() throws IOException {

        // Given
        String dataSetId = createDataset("../service/input_dataset.csv", "uppercase", "text/csv");
        String preparationId = createEmptyPreparationFromDataset(dataSetId, "uppercase prep");
        Preparation preparation = getPreparation(preparationId);
        String stepId = preparation.getSteps().get(0).getId();

        final TransformationMetadataCacheKey metadataCacheKey = cacheKeyGenerator.generateMetadataKey(preparationId, stepId, HEAD);

        // we check that there is not metadata on cache
        Assert.assertFalse(contentCache.has(metadataCacheKey));

        // we generate metadata
        metadataGenerator.generateMetadataForPreparation(preparationId, stepId, metadataCacheKey);

        // metadata should be on cache
        Assert.assertTrue(contentCache.has(metadataCacheKey));
    }
}
