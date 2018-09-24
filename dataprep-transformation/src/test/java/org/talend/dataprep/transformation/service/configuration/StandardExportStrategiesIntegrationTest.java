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

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.FILTER;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.RESERVOIR;
import static org.talend.dataprep.api.preparation.Step.ROOT_STEP;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.talend.dataprep.api.export.ExportParameters.SourceType;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.preparation.PreparationDetailsDTO;
import org.talend.dataprep.api.preparation.json.MixedContentMapModule;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.TransformationCacheKey;
import org.talend.dataprep.cache.TransformationMetadataCacheKey;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.command.preparation.PreparationSummaryGet;
import org.talend.dataprep.transformation.service.BaseExportStrategy;
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
    private PreparationSummaryGet preparationSummaryGet;

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

    private void injectObjectMapper(SampleExportStrategy exportStrategy) {
        Field mapperField = ReflectionUtils.findField(BaseExportStrategy.class, "mapper");
        ReflectionUtils.makeAccessible(mapperField);
        ReflectionUtils.setField(mapperField, exportStrategy, mapper);
    }

    private void initPreparationDetails() throws Exception {
        doReturn(preparationDetailsGet).when(applicationContext).getBean(eq(PreparationDetailsGet.class), anyString(),
                anyString());
        final PreparationDetailsDTO preparationDetailsDTO = mapper
                .readerFor(PreparationDetailsDTO.class) //
                .readValue(this.getClass().getResourceAsStream("preparation_details.json"));
        when(preparationDetailsGet.execute()) //
                .thenReturn(preparationDetailsDTO) //
                .thenReturn(preparationDetailsDTO);

        doReturn(preparationSummaryGet).when(applicationContext).getBean(eq(PreparationSummaryGet.class), anyString(),
                anyString());
        final PreparationDTO preparationDTO = mapper
                .readerFor(PreparationDTO.class) //
                .readValue(this.getClass().getResourceAsStream("preparation_details_summary.json"));
        when(preparationSummaryGet.execute()).thenReturn(preparationDTO, preparationDTO);
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

    private void doTestCachedExportStrategyShouldBeChosen(SourceType from) throws Exception {
        // Given
        ExportParameters parameters = new ExportParameters();
        parameters.setFrom(from);
        parameters.setContent(null);
        parameters.setPreparationId("prepId-1234");
        when(contentCache.has(transformationCacheKey)).thenReturn(true);

        // When
        Optional<SampleExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        // Then
        assertElectedStrategyIsInstanceOf(electedStrategy, CachedExportStrategy.class);
    }

    protected Optional<SampleExportStrategy> chooseExportStrategy(ExportParameters parameters) {
        return sampleExportStrategies //
                .filter(exportStrategy -> exportStrategy.test(parameters)) //
                .findFirst();
    }

    protected void assertElectedStrategyIsInstanceOf(Optional<SampleExportStrategy> electedStrategy, Class<?> clazz) {
        assertTrue("An ExportStrategy should be chosen but none was found", electedStrategy.isPresent());
        assertThat("The chosen ExportStrategy is not the expected one", electedStrategy.get(), instanceOf(clazz));
    }

    @Test
    public void testCachedExportStrategyShouldBeChosenFromHEAD() throws Exception {
        doTestCachedExportStrategyShouldBeChosen(HEAD);
    }

    @Test
    public void testCachedExportStrategyShouldBeChosenFromFilter() throws Exception {
        doTestCachedExportStrategyShouldBeChosen(FILTER);
    }

    @Test
    public void testCachedExportStrategyShouldBeChosenFromRESERVOIR() throws Exception {
        doTestCachedExportStrategyShouldBeChosen(RESERVOIR);
    }

    @Test
    public void testCachedExportStrategyShouldBeChosenFromNull() throws Exception {
        doTestCachedExportStrategyShouldBeChosen(null);
    }

    private void doTestOptimizedExportStrategyShouldBeChosenWhenPrepIsNotInCacheAndHasAtLeastTwoStepsFrom(
            SourceType from) throws Exception {
        // Given
        ExportParameters parameters = new ExportParameters();
        parameters.setFrom(from);
        parameters.setContent(null);
        parameters.setPreparationId(idOfPrepWith2StepsOrMore());
        when(contentCache.has(transformationCacheKeyPreviousVersion)).thenReturn(true);
        when(contentCache.has(transformationMetadataCacheKeyPreviousVersion)).thenReturn(true);

        // When
        Optional<SampleExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        // Then
        assertElectedStrategyIsInstanceOf(electedStrategy, OptimizedExportStrategy.class);
    }

    private String idOfPrepWith2StepsOrMore() throws IOException {
        reset(preparationDetailsGet);
        final PreparationDetailsDTO preparationDetailsDTO = mapper
                .readerFor(PreparationDetailsDTO.class) //
                .readValue(this.getClass().getResourceAsStream("two_steps_preparation_details.json"));
        when(preparationDetailsGet.execute()) //
                .thenReturn(preparationDetailsDTO) //
                .thenReturn(preparationDetailsDTO);

        final PreparationDTO preparationDTO = mapper
                .readerFor(PreparationDTO.class) //
                .readValue(this.getClass().getResourceAsStream("two_steps_preparation_details_summary.json"));
        when(preparationSummaryGet.execute())//
                .thenReturn(preparationDTO) //
                .thenReturn(preparationDTO);

        return "prepId-1234";
    }

    @Test
    public void testOptimizedExportStrategyShouldBeChosenWhenPrepIsNotInCacheAndHasAtLeastTwoStepsFromHEAD()
            throws Exception {
        doTestOptimizedExportStrategyShouldBeChosenWhenPrepIsNotInCacheAndHasAtLeastTwoStepsFrom(HEAD);
    }

    @Test
    public void testOptimizedExportStrategyShouldBeChosenWhenPrepIsNotInCacheAndHasAtLeastTwoStepsFromFILTER()
            throws Exception {
        doTestOptimizedExportStrategyShouldBeChosenWhenPrepIsNotInCacheAndHasAtLeastTwoStepsFrom(FILTER);
    }

    @Test
    public void testOptimizedExportStrategyShouldBeChosenWhenPrepIsNotInCacheAndHasAtLeastTwoStepsFromRESERVOIR()
            throws Exception {
        doTestOptimizedExportStrategyShouldBeChosenWhenPrepIsNotInCacheAndHasAtLeastTwoStepsFrom(RESERVOIR);
    }

    @Test
    public void testOptimizedExportStrategyShouldBeChosenWhenPrepIsNotInCacheAndHasAtLeastTwoStepsFromNull()
            throws Exception {
        doTestOptimizedExportStrategyShouldBeChosenWhenPrepIsNotInCacheAndHasAtLeastTwoStepsFrom(null);
    }

    protected void doTestOptimizedExportStrategyShouldNotBeChosenWhenVersionIsRootStepAndNoCacheIsAvailableFrom(
            SourceType from, Class expectedStrategyClass) throws Exception {
        // Given
        ExportParameters parameters = new ExportParameters();
        parameters.setFrom(from);
        parameters.setContent(null);
        parameters.setPreparationId(idOfPrepWith2StepsOrMore());
        parameters.setStepId(ROOT_STEP.getId());
        when(contentCache.has(transformationCacheKeyPreviousVersion)).thenReturn(false);
        when(contentCache.has(transformationMetadataCacheKeyPreviousVersion)).thenReturn(false);

        // When
        Optional<SampleExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        // Then
        assertTrue("An ExportStrategy should be chosen but none was found", electedStrategy.isPresent());
        assertThat("The chosen ExportStrategy is not the expected one", electedStrategy.get(),
                not(instanceOf(OptimizedExportStrategy.class)));
        assertThat("The chosen ExportStrategy is not the expected one", electedStrategy.get(),
                instanceOf(expectedStrategyClass));
    }

    @Test
    public void testOptimizedExportStrategyShouldNotBeChosenWhenVersionIsRootStepAndNoCacheIsAvailableFromHEAD()
            throws Exception {
        doTestOptimizedExportStrategyShouldNotBeChosenWhenVersionIsRootStepAndNoCacheIsAvailableFrom(HEAD,
                PreparationExportStrategy.class);
    }

    @Test
    public void testOptimizedExportStrategyShouldNotBeChosenWhenVersionIsRootStepAndNoCacheIsAvailableFromNull()
            throws Exception {
        doTestOptimizedExportStrategyShouldNotBeChosenWhenVersionIsRootStepAndNoCacheIsAvailableFrom(null,
                PreparationExportStrategy.class);
    }

    protected void doTestOptimizedExportStrategyShouldNotBeChosenWhenPrepHasOnlyOneStep(SourceType from,
            Class expectedStrategyClass) throws Exception {
        // Given
        ExportParameters parameters = new ExportParameters();
        parameters.setFrom(from);
        parameters.setContent(null);
        parameters.setPreparationId("prep-1234");
        when(contentCache.has(transformationCacheKeyPreviousVersion)).thenReturn(true);
        when(contentCache.has(transformationMetadataCacheKeyPreviousVersion)).thenReturn(true);

        // When
        Optional<SampleExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        // Then
        assertTrue("An ExportStrategy should be chosen but none was found", electedStrategy.isPresent());
        assertThat("The chosen ExportStrategy is not the expected one", electedStrategy.get(),
                not(instanceOf(OptimizedExportStrategy.class)));
        assertThat("The chosen ExportStrategy is not the expected one", electedStrategy.get(),
                instanceOf(expectedStrategyClass));
    }

    @Test
    public void testOptimizedExportStrategyShouldNotBeChosenWhenPrepHasOnlyOneStepFromHEAD() throws Exception {
        doTestOptimizedExportStrategyShouldNotBeChosenWhenPrepHasOnlyOneStep(HEAD, PreparationExportStrategy.class);
    }

    @Test
    public void testOptimizedExportStrategyShouldNotBeChosenWhenPrepHasOnlyOneStepFromNull() throws Exception {
        doTestOptimizedExportStrategyShouldNotBeChosenWhenPrepHasOnlyOneStep(null, PreparationExportStrategy.class);
    }

    private void doTestPreparationExportStrategyShouldBeChosen(SourceType from) throws Exception {
        // Given
        ExportParameters parameters = new ExportParameters();
        parameters.setFrom(from);
        parameters.setContent(null);
        parameters.setPreparationId("prepId-1234");
        parameters.setDatasetId(null);

        // When
        Optional<SampleExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        // Then
        assertElectedStrategyIsInstanceOf(electedStrategy, PreparationExportStrategy.class);
    }

    @Test
    public void testPreparationExportStrategyShouldBeChosenFromHEAD() throws Exception {
        doTestPreparationExportStrategyShouldBeChosen(HEAD);
    }

    @Test
    public void testPreparationExportStrategyShouldBeChosenFromNull() throws Exception {
        doTestPreparationExportStrategyShouldBeChosen(null);
    }

    @Test
    public void testDataSetExportStrategyShouldBeChosen() throws Exception {
        // Given
        ExportParameters parameters = new ExportParameters();
        parameters.setContent(null);
        parameters.setPreparationId(null);
        parameters.setDatasetId("dataset-5678");

        // When
        Optional<SampleExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        // Then
        assertElectedStrategyIsInstanceOf(electedStrategy, DataSetExportStrategy.class);
    }

    @Test
    public void testApplyPreparationExportStrategyShouldBeChosen() throws Exception {
        // Given
        ExportParameters parameters = new ExportParameters();
        parameters.setContent(null);
        parameters.setPreparationId("prepId-1234");
        parameters.setDatasetId("dataset-5678");

        // When
        Optional<SampleExportStrategy> electedStrategy = chooseExportStrategy(parameters);

        // Then
        assertElectedStrategyIsInstanceOf(electedStrategy, ApplyPreparationExportStrategy.class);
    }
}
