package org.talend.dataprep.transformation.service.export;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.command.dataset.DataSetGet;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.command.preparation.PreparationGetActions;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.dataprep.transformation.api.transformer.ExecutableTransformer;
import org.talend.dataprep.transformation.api.transformer.Transformer;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;
import org.talend.dataprep.transformation.format.FormatRegistrationService;
import org.talend.dataprep.transformation.format.JsonFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

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
    private PreparationDetailsGet preparationDetailsGet;

    @Before
    public void setUp() throws Exception {
        // Given
        mapper.registerModule(new Jdk8Module());
        strategy.setMapper(new ObjectMapper());

        when(formatRegistrationService.getByName(eq("JSON"))).thenReturn(new JsonFormat());

        final DataSetGetMetadata dataSetGetMetadata = mock(DataSetGetMetadata.class);
        when(applicationContext.getBean(eq(DataSetGetMetadata.class), anyVararg())).thenReturn(dataSetGetMetadata);

        DataSetGet dataSetGet = mock(DataSetGet.class);
        final StringWriter dataSetAsString = new StringWriter();
        DataSet dataSet = new DataSet();
        final DataSetMetadata dataSetMetadata = new DataSetMetadata("ds-1234", "", "", 0L, 0L, new RowMetadata(), "");
        final DataSetContent content = new DataSetContent();
        dataSetMetadata.setContent(content);
        dataSet.setMetadata(dataSetMetadata);
        dataSet.setRecords(Stream.empty());
        mapper.writerFor(DataSet.class).writeValue(dataSetAsString, dataSet);
        when(dataSetGet.execute()).thenReturn(new ByteArrayInputStream(dataSetAsString.toString().getBytes()));
        when(applicationContext.getBean(eq(DataSetGet.class), anyVararg())).thenReturn(dataSetGet);

        final PreparationGetActions preparationGetActions = mock(PreparationGetActions.class);
        when(preparationGetActions.execute()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
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

    private void configurePreparation(Preparation preparation, String preparationId, String stepId) throws IOException {
        final StringWriter preparationAsString = new StringWriter();
        mapper.writerFor(Preparation.class).writeValue(preparationAsString, preparation);
        when(preparationDetailsGet.execute())
                .thenReturn(new ByteArrayInputStream(preparationAsString.toString().getBytes()));
        when(applicationContext.getBean(eq(PreparationDetailsGet.class), eq(preparationId), eq(stepId)))
                .thenReturn(preparationDetailsGet);
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

        final Preparation preparation = new Preparation();
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

        final Preparation preparation = new Preparation();
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
