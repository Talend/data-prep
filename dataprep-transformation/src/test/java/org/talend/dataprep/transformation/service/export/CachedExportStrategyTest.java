// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.service.export;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.ServiceBaseTest;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;

public class CachedExportStrategyTest extends ServiceBaseTest {

    @Autowired
    ContentCache cache;

    @Autowired
    CacheKeyGenerator cacheKeyGenerator;

    @Autowired
    CachedExportStrategy cachedExportStrategy;

    @Autowired
    PreparationRepository preparationRepository;

    @Before
    public void setUp() {
        super.setUp();
        final Preparation preparation = new Preparation("1234", "1.0");
        preparation.setDataSetId("1234");
        preparation.setHeadId("0");
        preparationRepository.add(preparation);

        final TransformationCacheKey cacheKey = cacheKeyGenerator.generateContentKey("1234", "1234", "0", "text",
                ExportParameters.SourceType.HEAD);
        try (OutputStream text = cache.put(cacheKey, ContentCache.TimeToLive.DEFAULT)) {
            text.write("{}".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void shouldAcceptIfCacheEntryExists() throws Exception {
        // Given
        final ExportParameters parameters = new ExportParameters();
        parameters.setDatasetId("1234");
        parameters.setPreparationId("1234");
        parameters.setStepId("0");
        parameters.setExportType("text");
        parameters.setFrom(ExportParameters.SourceType.HEAD);

        // Then
        assertTrue(cachedExportStrategy.accept(parameters));
    }

    @Test
    public void shouldNotAcceptIfCacheEntryDoesNotExists() throws Exception {
        // Given
        final ExportParameters parameters = new ExportParameters();
        parameters.setDatasetId("1234");
        parameters.setPreparationId("2345"); // Preparation differs from key.
        parameters.setStepId("0");
        parameters.setExportType("text");
        parameters.setFrom(ExportParameters.SourceType.HEAD);

        // Then
        assertFalse(cachedExportStrategy.accept(parameters));
    }

}
