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

package org.talend.dataprep.transformation.service.export;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;

import java.io.OutputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;
import org.talend.dataprep.transformation.service.TransformationServiceBaseTest;

public class OptimizedExportStrategyTest extends TransformationServiceBaseTest {

    @Autowired
    OptimizedExportStrategy optimizedExportStrategy;

    @Autowired
    PreparationRepository preparationRepository;

    @Autowired
    ContentCache contentCache;

    @Autowired
    CacheKeyGenerator cacheKeyGenerator;

    @Test
    public void testAcceptNullParameters() throws Exception {
        assertFalse(optimizedExportStrategy.accept(null));
    }

    @Test
    public void testAcceptKO_withContent() throws Exception {
        // Given
        ExportParameters exportParameters = new ExportParameters();
        exportParameters.setContent(new DataSet());

        // Then
        assertFalse(optimizedExportStrategy.accept(exportParameters));
    }

    @Test
    public void testAcceptKO_noPreparation() throws Exception {
        // Given
        ExportParameters exportParameters = new ExportParameters();
        exportParameters.setDatasetId("1234");

        // Then
        assertFalse(optimizedExportStrategy.accept(exportParameters));
    }

    @Ignore
    @Test(expected = TDPException.class)
    public void testAcceptKO_preparationNotExist() throws Exception {
        // Given
        ExportParameters exportParameters = new ExportParameters();
        exportParameters.setPreparationId("1234");

        // Then
        assertFalse(optimizedExportStrategy.accept(exportParameters));
    }

    @Test
    public void testAcceptKO_noStepId() throws Exception {
        // Given
        preparationRepository.add(new Preparation("prep-1234", "1234", Step.ROOT_STEP.id(), "0.1"));
        ExportParameters exportParameters = new ExportParameters();
        exportParameters.setPreparationId("prep-1234");

        // Then
        assertFalse(optimizedExportStrategy.accept(exportParameters));
    }

    @Test
    public void testAcceptKO_noMetadataCache() throws Exception {
        // Given
        final String preparation = createEmptyPreparationFromDataset("1234", "test");
        applyAction(preparation, "[{}]");
        applyAction(preparation, "[{}]");

        ExportParameters exportParameters = new ExportParameters();
        exportParameters.setPreparationId(preparation);

        // Then
        assertFalse(optimizedExportStrategy.accept(exportParameters));
    }

    @Test
    public void testAcceptKO_withMetadataCacheNoContentCache() throws Exception {
        // Given
        final String preparation = createEmptyPreparationFromDataset("1234", "test");
        applyAction(preparation, "[{}]");
        applyAction(preparation, "[{}]");

        final Preparation preparationDetails = getPreparation(preparation);
        for (Step step : preparationDetails.getSteps()) {
            try (OutputStream content = contentCache.put(cacheKeyGenerator.generateMetadataKey(preparation, step.id(), HEAD), ContentCache.TimeToLive.DEFAULT)) {
                content.write("{}".getBytes());
                content.flush();
            }
        }

        ExportParameters exportParameters = new ExportParameters();
        exportParameters.setPreparationId(preparation);
        exportParameters.setFrom(HEAD);

        // Then
        assertFalse(optimizedExportStrategy.accept(exportParameters));
    }

    @Test
    public void testAcceptOK() throws Exception {
        // Given
        final String datasetId = "1234";
        final String format = "";
        final String preparation = createEmptyPreparationFromDataset(datasetId, "test");
        applyAction(preparation, "[{}]");
        applyAction(preparation, "[{}]");

        final Preparation preparationDetails = getPreparation(preparation);
        for (Step step : preparationDetails.getSteps()) {
            try (OutputStream content = contentCache.put(cacheKeyGenerator.generateMetadataKey(preparation, step.id(), HEAD), ContentCache.TimeToLive.DEFAULT)) {
                content.write("{}".getBytes());
                content.flush();
            }

            final TransformationCacheKey key = cacheKeyGenerator.generateContentKey( //
                    datasetId, //
                    preparation, //
                    step.id(), //
                    format, //
                    HEAD, //
                    "" //
            );
            try (OutputStream content = contentCache.put(key, ContentCache.TimeToLive.DEFAULT)) {
                content.write("{}".getBytes());
                content.flush();
            }
        }

        ExportParameters exportParameters = new ExportParameters();
        exportParameters.setPreparationId(preparation);
        exportParameters.setDatasetId(datasetId);
        exportParameters.setExportType(format);
        exportParameters.setFrom(HEAD);

        // Then
        assertTrue(optimizedExportStrategy.accept(exportParameters));
    }

    @Test
    public void testExecute() throws Exception {
        // Given
        final String datasetId = "1234";
        final String format = "JSON";
        final String preparation = createEmptyPreparationFromDataset(datasetId, "test");
        applyAction(preparation, "[{}]");
        applyAction(preparation, "[{}]");

        final Preparation preparationDetails = getPreparation(preparation);
        for (Step step : preparationDetails.getSteps()) {
            try (OutputStream content = contentCache.put(cacheKeyGenerator.generateMetadataKey(preparation, step.id(), HEAD), ContentCache.TimeToLive.DEFAULT)) {
                content.write("{}".getBytes());
                content.flush();
            }

            final TransformationCacheKey key = cacheKeyGenerator.generateContentKey( //
                    datasetId, //
                    preparation, //
                    step.id(), //
                    format, //
                    HEAD, //
                    "" // no filter
            );
            try (OutputStream content = contentCache.put(key, ContentCache.TimeToLive.DEFAULT)) {
                content.write("{\"records\": [{\"0000\": \"a\"}]}".getBytes());
                content.flush();
            }
        }

        ExportParameters exportParameters = new ExportParameters();
        exportParameters.setPreparationId(preparation);
        exportParameters.setDatasetId(datasetId);
        exportParameters.setExportType(format);
        exportParameters.setFrom(HEAD);

        // Then
        optimizedExportStrategy.execute(exportParameters);
    }
}
