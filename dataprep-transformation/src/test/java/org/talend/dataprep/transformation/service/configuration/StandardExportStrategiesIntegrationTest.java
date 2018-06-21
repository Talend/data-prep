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

package org.talend.dataprep.transformation.service.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.json.MixedContentMapModule;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.TransformationCacheKey;
import org.talend.dataprep.cache.TransformationMetadataCacheKey;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.transformation.service.BaseExportStrategy;
import org.talend.dataprep.transformation.service.ExportStrategy;
import org.talend.dataprep.transformation.service.export.ApplyPreparationExportStrategy;
import org.talend.dataprep.transformation.service.export.CachedExportStrategy;
import org.talend.dataprep.transformation.service.export.DataSetExportStrategy;
import org.talend.dataprep.transformation.service.export.OptimizedExportStrategy;
import org.talend.dataprep.transformation.service.export.PreparationExportStrategy;
import org.talend.dataprep.transformation.service.export.SampleExportStrategy;
import org.talend.dataprep.util.OrderedBeans;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class StandardExportStrategiesIntegrationTest {

    private StandardExportStrategies standardExportStrategiesConfiguration = new StandardExportStrategies();

    protected OrderedBeans<SampleExportStrategy> sampleExportStrategies;

    private ApplyPreparationExportStrategy applyPreparationExportStrategy;

    private DataSetExportStrategy dataSetExportStrategy;

    @InjectMocks
    private OptimizedExportStrategy optimizedExportStrategy;

    private PreparationExportStrategy preparationExportStrategy;

    @InjectMocks
    private CachedExportStrategy cachedExportStrategy;

    @Mock
    protected ApplicationContext applicationContext;

    @Mock
    private PreparationDetailsGet preparationDetailsGet;

    @Mock
    private CacheKeyGenerator cacheKeyGenerator;

    @Mock
    private ContentCache contentCache;

    private ObjectMapper mapper;
    {
        mapper = new ObjectMapper();
        mapper.registerModule(new MixedContentMapModule());
    }

    private TransformationCacheKey transformationCacheKey;

    private TransformationCacheKey transformationCacheKeyPreviousVersion;

    private TransformationMetadataCacheKey transformationMetadataCacheKeyPreviousVersion;

    @Before
    public void setUp() throws Exception {
        initSampleExportStrategies();
        initPreparationDetails();
        initContentCache();
    }

    private void initSampleExportStrategies() throws Exception {
        injectObjectMapper(cachedExportStrategy);
        injectObjectMapper(optimizedExportStrategy);
        applyPreparationExportStrategy = new ApplyPreparationExportStrategy();
        dataSetExportStrategy = new DataSetExportStrategy();
        preparationExportStrategy = new PreparationExportStrategy();
        sampleExportStrategies = standardExportStrategiesConfiguration.exportStrategies(applyPreparationExportStrategy,
                dataSetExportStrategy, optimizedExportStrategy, preparationExportStrategy, cachedExportStrategy);
    }

    private void injectObjectMapper(ExportStrategy exportStrategy) throws Exception {
        Field mapperField = ReflectionUtils.findField(BaseExportStrategy.class, "mapper");
        ReflectionUtils.makeAccessible(mapperField);
        ReflectionUtils.setField(mapperField, exportStrategy, mapper);
    }

    private void initPreparationDetails() throws Exception {
        doReturn(preparationDetailsGet).when(applicationContext).getBean(eq(PreparationDetailsGet.class), anyString(),
                anyString());
        when(preparationDetailsGet.execute())
                .thenReturn(this.getClass().getResourceAsStream("preparation_details.json"))
                .thenReturn(this.getClass().getResourceAsStream("preparation_details.json"));
    }

    private void initContentCache() {
        transformationCacheKey = mock(TransformationCacheKey.class);
        doReturn(transformationCacheKey).when(cacheKeyGenerator).generateContentKey(anyString(), anyString(),
                anyString(), anyString(), any(), anyMapOf(String.class, String.class), anyString());

        transformationCacheKeyPreviousVersion = mock(TransformationCacheKey.class);
        doReturn(transformationCacheKeyPreviousVersion).when(cacheKeyGenerator).generateContentKey( //
                anyString(), anyString(), anyString(), anyString(), any(), anyString());

        transformationMetadataCacheKeyPreviousVersion = mock(TransformationMetadataCacheKey.class);
        doReturn(transformationMetadataCacheKeyPreviousVersion).when(cacheKeyGenerator).generateMetadataKey(anyString(),
                anyString(), any());
        when(contentCache.get(transformationMetadataCacheKeyPreviousVersion))
                .thenReturn(new ByteArrayInputStream("{}".getBytes()));
    }

    @Test
    public void testCachedExportStrategyShouldBeChosen() throws Exception {
        ExportParameters parameters = new ExportParameters();
        parameters.setFrom(HEAD); // or null or RESERVOIR
        parameters.setContent(null);
        parameters.setPreparationId("prepId-1234");
        when(contentCache.has(transformationCacheKey)).thenReturn(true);

        Optional<? extends ExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        assertThat(electedStrategy.isPresent());
        assertThat(electedStrategy.get()).isInstanceOf(CachedExportStrategy.class);
    }

    protected Optional<? extends ExportStrategy> chooseExportStrategy(ExportParameters parameters) {
        return sampleExportStrategies //
                .filter(exportStrategy -> exportStrategy.accept(parameters)) //
                .findFirst();
    }

    @Test
    public void testOptimizedExportStrategyShouldBeChosenWhenPrepHasAtLeastTwoSteps() throws Exception {
        ExportParameters parameters = new ExportParameters();
        parameters.setFrom(HEAD); // or null or RESERVOIR
        parameters.setContent(null);
        parameters.setPreparationId(idOfPrepWith2StepsOrMore());
        reset(preparationDetailsGet);
        when(preparationDetailsGet.execute())
                .thenReturn(this.getClass().getResourceAsStream("two_steps_preparation_details.json"))
                .thenReturn(this.getClass().getResourceAsStream("two_steps_preparation_details.json"));
        when(contentCache.has(transformationCacheKeyPreviousVersion)).thenReturn(true);
        when(contentCache.has(transformationMetadataCacheKeyPreviousVersion)).thenReturn(true);

        Optional<? extends ExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        assertThat(electedStrategy.isPresent());
        assertThat(electedStrategy.get()).isInstanceOf(OptimizedExportStrategy.class);
    }

    @Test
    public void testOptimizedExportStrategyShouldNotBeChosenWhenPrepHasOnlyOneStep() throws Exception {
        ExportParameters parameters = new ExportParameters();
        parameters.setFrom(HEAD); // or null or RESERVOIR
        parameters.setContent(null);
        parameters.setPreparationId(idOfPrepWith2StepsOrMore());
        when(contentCache.has(transformationCacheKeyPreviousVersion)).thenReturn(true);
        when(contentCache.has(transformationMetadataCacheKeyPreviousVersion)).thenReturn(true);

        Optional<? extends ExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        assertThat(electedStrategy.isPresent());
        assertThat(electedStrategy.get()).isNotInstanceOf(OptimizedExportStrategy.class);
        assertThat(electedStrategy.get()).isInstanceOf(PreparationExportStrategy.class);
    }

    private String idOfPrepWith2StepsOrMore() {
        return "prepId-1234";
    }

    @Test
    public void testPreparationExportStrategyShouldBeChosen() throws Exception {
        ExportParameters parameters = new ExportParameters();
        parameters.setFrom(HEAD); // or null
        parameters.setContent(null);
        parameters.setPreparationId("prepId-1234");
        parameters.setDatasetId(null);

        Optional<? extends ExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        assertThat(electedStrategy.isPresent());
        assertThat(electedStrategy.get()).isInstanceOf(PreparationExportStrategy.class);
    }

    @Test
    public void testDataSetExportStrategyShouldBeChosen() throws Exception {
        ExportParameters parameters = new ExportParameters();
        parameters.setContent(null);
        parameters.setPreparationId(null);
        parameters.setDatasetId("dataset-5678");

        Optional<? extends ExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        assertThat(electedStrategy.isPresent());
        assertThat(electedStrategy.get()).isInstanceOf(DataSetExportStrategy.class);
    }

    @Test
    public void testApplyPreparationExportStrategyShouldBeChosen() throws Exception {
        ExportParameters parameters = new ExportParameters();
        parameters.setContent(null);
        parameters.setPreparationId("prepId-1234");
        parameters.setDatasetId("dataset-5678");

        Optional<? extends ExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        assertThat(electedStrategy.isPresent());
        assertThat(electedStrategy.get()).isInstanceOf(ApplyPreparationExportStrategy.class);
    }
}
