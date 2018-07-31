package org.talend.dataprep.transformation.service.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.stream.Stream;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.cache.CacheKeyGenerator;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.TransformationCacheKey;
import org.talend.dataprep.command.preparation.PreparationGetActions;
import org.talend.dataprep.command.preparation.PreparationSummaryGet;
import org.talend.dataprep.dataset.adapter.DatasetClient;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.dataprep.transformation.api.transformer.ExecutableTransformer;
import org.talend.dataprep.transformation.api.transformer.Transformer;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.format.FormatRegistrationService;
import org.talend.dataprep.transformation.format.JsonFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.talend.dataprep.transformation.service.BaseExportStrategy;
import org.talend.dataprep.transformation.service.ExportStrategy;

@RunWith(MockitoJUnitRunner.class)
public class PreparationExportStrategyTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private PreparationExportStrategy strategy;

    @Mock
    private TransformerFactory factory;

    @Mock
    private FormatRegistrationService formatRegistrationService;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private SecurityProxy securityProxy;

    @Mock
    private CacheKeyGenerator cacheKeyGenerator;

    @Mock
    private ContentCache contentCache;

    @Mock
    private Transformer transformer;

    @Mock
    private PreparationSummaryGet preparationSummaryGet;

    @Mock
    private DatasetClient datasetClient;

    @Before
    public void setUp() throws Exception {
        // Given
        mapper.registerModule(new Jdk8Module());
        injectObjectMapper(strategy);

        when(formatRegistrationService.getByName(eq("JSON"))).thenReturn(new JsonFormat());

        DataSet dataSet = new DataSet();
        final DataSetMetadata dataSetMetadata = new DataSetMetadata("ds-1234", "", "", 0L, 0L, new RowMetadata(), "");
        final DataSetContent content = new DataSetContent();
        dataSetMetadata.setContent(content);
        dataSet.setMetadata(dataSetMetadata);
        dataSet.setRecords(Stream.empty());

        when(datasetClient.getDataSet(anyString())).thenReturn(dataSet);
        //when(datasetClient.getDataSetMetadata(anyString())).thenReturn(dataSetMetadata);

        final PreparationGetActions preparationGetActions = mock(PreparationGetActions.class);
        when(preparationGetActions.execute()).thenReturn(Collections.emptyList());
        when(applicationContext.getBean(eq(PreparationGetActions.class), eq("prep-1234"), anyString()))
                .thenReturn(preparationGetActions);

        final TransformationCacheKey cacheKey = mock(TransformationCacheKey.class);
        when(cacheKey.getKey()).thenReturn("cache-1234");
        when(cacheKeyGenerator.generateContentKey(anyString(), anyString(), anyString(), anyString(), any(), any(),
                anyString())).thenReturn(cacheKey);

        final ExecutableTransformer executableTransformer = mock(ExecutableTransformer.class);
        reset(transformer);
        when(transformer.buildExecutable(any(), any())).thenReturn(executableTransformer);
        when(factory.get(any())).thenReturn(transformer);

        when(contentCache.put(any(), any())).thenReturn(new NullOutputStream());
    }

    private void injectObjectMapper(ExportStrategy exportStrategy) throws Exception {
        Field mapperField = ReflectionUtils.findField(BaseExportStrategy.class, "mapper");
        ReflectionUtils.makeAccessible(mapperField);
        ReflectionUtils.setField(mapperField, exportStrategy, mapper);
    }

    private void configurePreparation(PreparationDTO preparation, String preparationId, String stepId) {
        when(preparationSummaryGet.execute()).thenReturn(preparation);
        when(applicationContext.getBean(eq(PreparationSummaryGet.class), eq(preparationId), eq(stepId)))
                .thenReturn(preparationSummaryGet);
    }

    @Test
    public void shouldNotAcceptNullParameters() {
        // Then
        assertFalse(strategy.accept(null));
    }

    @Test
    public void shouldAcceptParameters() {
        // Then
        final ExportParameters parameters = new ExportParameters();
        parameters.setContent(null);
        parameters.setFrom(null);
        parameters.setFrom(ExportParameters.SourceType.HEAD);
        parameters.setPreparationId("prep-1234");
        parameters.setDatasetId("");
        assertTrue(strategy.accept(parameters));
    }

    @Test
    public void shouldUsedVersionedPreparation() throws IOException {
        // Given
        final ExportParameters parameters = new ExportParameters();
        parameters.setExportType("JSON");
        parameters.setPreparationId("prep-1234");
        parameters.setStepId("step-1234");

        final PreparationDTO preparation = new PreparationDTO();
        preparation.setId("prep-1234");
        preparation.setHeadId("step-1234");
        configurePreparation(preparation, "prep-1234", "step-1234");

        // When
        final StreamingResponseBody body = strategy.execute(parameters);
        body.writeTo(new NullOutputStream());

        // Then
        final ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(transformer).buildExecutable(any(), captor.capture());
        assertEquals("prep-1234", captor.getValue().getPreparationId());
        assertEquals("step-1234", captor.getValue().getPreparation().getHeadId());
    }

    @Test
    public void shouldUsedHeadPreparation() throws IOException {
        // Given
        final ExportParameters parameters = new ExportParameters();
        parameters.setExportType("JSON");
        parameters.setPreparationId("prep-1234");
        parameters.setStepId("head");

        final PreparationDTO preparation = new PreparationDTO();
        preparation.getSteps().add(Step.ROOT_STEP.id());
        preparation.setId("prep-1234");
        preparation.setHeadId("head");
        configurePreparation(preparation, "prep-1234", "head");

        // When
        final StreamingResponseBody body = strategy.execute(parameters);
        body.writeTo(new NullOutputStream());

        // Then
        final ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(transformer).buildExecutable(any(), captor.capture());
        assertEquals("prep-1234", captor.getValue().getPreparationId());
        assertEquals("head", captor.getValue().getPreparation().getHeadId());
    }
}
