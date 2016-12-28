// ============================================================================
//
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

package org.talend.dataprep.transformation.service;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_DOES_NOT_EXIST;
import static org.talend.dataprep.exception.error.TransformationErrorCodes.UNEXPECTED_EXCEPTION;
import static org.talend.dataprep.quality.AnalyzerService.Analysis.SEMANTIC;
import static org.talend.dataprep.transformation.actions.category.ScopeCategory.COLUMN;
import static org.talend.dataprep.transformation.actions.category.ScopeCategory.LINE;
import static org.talend.dataprep.transformation.format.JsonFormat.JSON;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.Flag;
import org.talend.dataprep.api.dataset.statistics.SemanticDomain;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.command.dataset.DataSetGet;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.exception.json.JsonErrorCodeDescription;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.security.PublicAPI;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.dataprep.transformation.aggregation.AggregationService;
import org.talend.dataprep.transformation.aggregation.api.AggregationParameters;
import org.talend.dataprep.transformation.aggregation.api.AggregationResult;
import org.talend.dataprep.transformation.api.action.ActionParser;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.dynamic.DynamicType;
import org.talend.dataprep.transformation.api.action.dynamic.GenericParameter;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.api.transformer.configuration.PreviewConfiguration;
import org.talend.dataprep.transformation.api.transformer.suggestion.Suggestion;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngine;
import org.talend.dataprep.transformation.cache.CacheKeyGenerator;
import org.talend.dataprep.transformation.cache.TransformationMetadataCacheKey;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.preview.api.PreviewParameters;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;
import org.talend.dataquality.semantic.broadcast.BroadcastIndexObject;

import com.fasterxml.jackson.core.JsonParser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(value = "transformations", basePath = "/transform", description = "Transformations on data")
public class TransformationService extends BaseTransformationService {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TransformationService.class);

    @Autowired
    private AnalyzerService analyzerService;

    /**
     * The Spring application context.
     */
    @Autowired
    private WebApplicationContext context;

    /**
     * All available transformation actions.
     */
    @Autowired
    private ActionRegistry actionRegistry;

    /**
     * he aggregation service.
     */
    @Autowired
    private AggregationService aggregationService;

    /**
     * The action suggestion engine.
     */
    @Autowired
    private SuggestionEngine suggestionEngine;

    /**
     * The transformer factory.
     */
    @Autowired
    private TransformerFactory factory;

    /**
     * Task executor for asynchronous processing.
     */
    @Resource(name = "serializer#json#executor")
    private TaskExecutor executor;

    /**
     * Security proxy enable a thread to borrow the identity of another user.
     */
    @Autowired
    private SecurityProxy securityProxy;

    @Autowired
    private ActionParser actionParser;

    @Autowired
    private CacheKeyGenerator cacheKeyGenerator;

    @Autowired
    private ContentCache contentCache;

    /**
     * The root step.
     */
    @Resource(name = "rootStep")
    private Step rootStep;

    @RequestMapping(value = "/apply", method = POST, consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Run the transformation given the provided export parameters", notes = "This operation transforms the dataset or preparation using parameters in export parameters.")
    @VolumeMetered
    public StreamingResponseBody execute(
            @ApiParam(value = "Preparation id to apply.") @RequestBody @Valid final ExportParameters parameters) {
        return executeSampleExportStrategy(parameters);
    }

    /**
     * Apply the preparation to the dataset out of the given IDs.
     *
     * @param preparationId the preparation id to apply on the dataset.
     * @param datasetId the dataset id to transform.
     * @param formatName The output {@link ExportFormat format}. This format also set the MIME response type.
     * @param stepId the preparation step id to use (default is 'head').
     * @param name the transformation name.
     * @param exportParams additional (optional) export parameters.
     */
    //@formatter:off
    @RequestMapping(value = "/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", method = GET)
    @ApiOperation(value = "Transform the given preparation to the given format on the given dataset id", notes = "This operation transforms the dataset using preparation id in the provided format.")
    @VolumeMetered
    public StreamingResponseBody applyOnDataset(@ApiParam(value = "Preparation id to apply.") @PathVariable(value = "preparationId") final String preparationId,
                                                @ApiParam(value = "DataSet id to transform.") @PathVariable(value = "datasetId") final String datasetId,
                                                @ApiParam(value = "Output format") @PathVariable("format") final String formatName,
                                                @ApiParam(value = "Step id", defaultValue = "head") @RequestParam(value = "stepId", required = false, defaultValue = "head") final String stepId,
                                                @ApiParam(value = "Name of the transformation", defaultValue = "untitled") @RequestParam(value = "name", required = false, defaultValue = "untitled") final String name,
                                                @RequestParam final Map<String, String> exportParams) {
        //@formatter:on
        final ExportParameters exportParameters = new ExportParameters();
        exportParameters.setPreparationId(preparationId);
        exportParameters.setDatasetId(datasetId);
        exportParameters.setExportType(formatName);
        exportParameters.setStepId(stepId);
        exportParameters.setExportName(name);
        exportParameters.getArguments().putAll(exportParams);

        return executeSampleExportStrategy(exportParameters);
    }

    /**
     * Export the dataset to the given format.
     *
     * @param datasetId the dataset id to transform.
     * @param formatName The output {@link ExportFormat format}. This format also set the MIME response type.
     * @param name the transformation name.
     * @param exportParams additional (optional) export parameters.
     */
    //@formatter:off
    @RequestMapping(value = "/export/dataset/{datasetId}/{format}", method = GET)
    @ApiOperation(value = "Export the given dataset")
    @Timed
    public StreamingResponseBody exportDataset(
            @ApiParam(value = "DataSet id to transform.") @PathVariable(value = "datasetId") final String datasetId,
            @ApiParam(value = "Output format") @PathVariable("format") final String formatName,
            @ApiParam(value = "Name of the transformation", defaultValue = "untitled") @RequestParam(value = "name", required = false, defaultValue = "untitled") final String name,
            @RequestParam final Map<String, String> exportParams) {
        //@formatter:on
        return applyOnDataset(null, datasetId, formatName, null, name, exportParams);
    }

    /**
     * Compute the given aggregation.
     *
     * @param rawParams the aggregation rawParams as body rawParams.
     */
    // @formatter:off
    @RequestMapping(value = "/aggregate", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Compute the aggregation according to the request body rawParams", consumes = APPLICATION_JSON_VALUE)
    @VolumeMetered
    public AggregationResult aggregate(@ApiParam(value = "The aggregation rawParams in json") @RequestBody final String rawParams) {
        // @formatter:on

        // parse the aggregation parameters
        final AggregationParameters parameters;
        try {
            parameters = mapper.readerFor(AggregationParameters.class).readValue(rawParams);
            LOG.debug("Aggregation requested {}", parameters);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.BAD_AGGREGATION_PARAMETERS, e);
        }

        InputStream contentToAggregate;

        // get the content of the preparation (internal call with piped streams)
        if (StringUtils.isNotBlank(parameters.getPreparationId())) {
            try {
                PipedOutputStream temp = new PipedOutputStream();
                contentToAggregate = new PipedInputStream(temp);

                // because of piped streams, processing must be asynchronous
                Runnable r = () -> {
                    try {
                        final ExportParameters exportParameters = new ExportParameters();
                        exportParameters.setPreparationId(parameters.getPreparationId());
                        exportParameters.setDatasetId(parameters.getDatasetId());
                        if (parameters.getFilter() != null) {
                            exportParameters.setFilter(mapper.readTree(parameters.getFilter()));
                        }
                        exportParameters.setExportType(JSON);
                        exportParameters.setStepId(parameters.getStepId());

                        final StreamingResponseBody body = executeSampleExportStrategy(exportParameters);
                        body.writeTo(temp);
                    } catch (IOException e) {
                        throw new TDPException(CommonErrorCodes.UNABLE_TO_AGGREGATE, e);
                    }
                };
                executor.execute(r);
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNABLE_TO_AGGREGATE, e);
            }
        } else {
            final DataSetGet dataSetGet = context.getBean(DataSetGet.class, parameters.getDatasetId(), false, true);
            contentToAggregate = dataSetGet.execute();
        }

        // apply the aggregation
        try (JsonParser parser = mapper.getFactory().createParser(contentToAggregate)) {
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
            return aggregationService.aggregate(parameters, dataSet);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        } finally {
            // don't forget to release the connection
            if (contentToAggregate != null) {
                try {
                    contentToAggregate.close();
                } catch (IOException e) {
                    LOG.warn("Could not close dataset input stream while aggregating", e);
                }
            }
        }
    }

    /**
     * This operation allow client to create a diff between 2 list of actions starting from the same data. For example,
     * sending:
     * <ul>
     * <li>{a1, a2} as old actions</li>
     * <li>{a1, a2, a3} as new actions</li>
     * </ul>
     * ... will highlight changes done by a3.
     * <p>
     * To prevent the actions to exceed URL length limit, everything is shipped within via the multipart request body.
     *
     * @param previewParameters The preview parameters, encoded in json within the request body.
     * @param output Where to write the response.
     */
    //@formatter:off
    @RequestMapping(value = "/transform/preview", method = POST, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Preview the transformation on input data", notes = "This operation returns the input data diff between the old and the new transformation actions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @VolumeMetered
    public void transformPreview(@ApiParam(name = "body", value = "Preview parameters.") @RequestBody final PreviewParameters previewParameters,
                                 final OutputStream output) {
        //@formatter:on
        if (shouldApplyDiffToSampleSource(previewParameters)) {
            executeDiffOnSample(previewParameters, output);
        } else {
            executeDiffOnDataset(previewParameters, output);
        }
    }

    private void executeDiffOnSample(final PreviewParameters previewParameters, final OutputStream output) {
        final TransformationMetadataCacheKey metadataKey = cacheKeyGenerator.generateMetadataKey( //
                previewParameters.getPreparationId(), //
                rootStep.id(), //
                previewParameters.getSourceType() //
        );

        final ContentCacheKey contentKey = cacheKeyGenerator.generateContentKey( //
                previewParameters.getDataSetId(), //
                previewParameters.getPreparationId(), //
                rootStep.id(), //
                JSON, //
                previewParameters.getSourceType() //
        );

        try (final InputStream metadata = contentCache.get(metadataKey); //
                final InputStream content = contentCache.get(contentKey); //
                final JsonParser contentParser = mapper.getFactory().createParser(content)) {

            // build metadata
            final RowMetadata rowMetadata = mapper.readerFor(RowMetadata.class).readValue(metadata);
            final DataSetMetadata dataSetMetadata = new DataSetMetadata();
            dataSetMetadata.setRowMetadata(rowMetadata);

            // build dataset
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(contentParser);
            dataSet.setMetadata(dataSetMetadata);

            // trigger diff
            executePreview( //
                    previewParameters.getNewActions(), //
                    previewParameters.getBaseActions(), //
                    previewParameters.getTdpIds(), //
                    dataSet, //
                    output //
            );
        } catch (final IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PERFORM_PREVIEW, e);
        }
    }

    private void executeDiffOnDataset(final PreviewParameters previewParameters, final OutputStream output) {
        // because of dataset records streaming, the dataset content must be within an auto closeable block
        final DataSetGet dataSetGet = context.getBean(DataSetGet.class, previewParameters.getDataSetId(), false, true);

        boolean identityReleased = false;
        securityProxy.asTechnicalUser();
        try (final InputStream dataSetContent = dataSetGet.execute(); //
                final JsonParser parser = mapper.getFactory().createParser(dataSetContent)) {

            securityProxy.releaseIdentity();
            identityReleased = true;

            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
            executePreview( //
                    previewParameters.getNewActions(), //
                    previewParameters.getBaseActions(), //
                    previewParameters.getTdpIds(), //
                    dataSet, //
                    output //
            );

        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PERFORM_PREVIEW, e);
        } finally {
            // make sure the technical identity is released
            if (!identityReleased) {
                securityProxy.releaseIdentity();
            }
        }
    }

    private boolean shouldApplyDiffToSampleSource(final PreviewParameters previewParameters) {
        if (previewParameters.getSourceType() != HEAD && previewParameters.getPreparationId() != null) {
            final TransformationMetadataCacheKey metadataKey = cacheKeyGenerator.generateMetadataKey( //
                    previewParameters.getPreparationId(), //
                    rootStep.id(), //
                    previewParameters.getSourceType() //
            );

            final ContentCacheKey contentKey = cacheKeyGenerator.generateContentKey( //
                    previewParameters.getDataSetId(), //
                    previewParameters.getPreparationId(), //
                    rootStep.id(), //
                    JSON, //
                    previewParameters.getSourceType() //
            );

            return contentCache.has(metadataKey) && contentCache.has(contentKey);
        }
        return false;
    }

    /**
     * Given a list of requested preview, it applies the diff to each one.
     * A diff is between 2 sets of actions and return the info like created columns ids
     */
    //@formatter:off
    @RequestMapping(value = "/transform/diff/metadata", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Given a list of requested preview, it applies the diff to each one. A diff is between 2 sets of actions and return the info like created columns ids", notes = "This operation returns the diff metadata", consumes = APPLICATION_JSON_VALUE)
    @VolumeMetered
    public List<StepDiff> getCreatedColumns(@ApiParam(name = "body", value = "Preview parameters list in json.") @RequestBody final List<PreviewParameters> previewParameters) {
        return previewParameters.stream().map(this::getCreatedColumns).collect(toList());
    }

    @RequestMapping(value = "/preparation/{preparationId}/cache", method = DELETE)
    @ApiOperation(value = "Evict content entries related to the preparation", notes = "This operation remove content entries related to the preparation.")
    @VolumeMetered
    public void evictCache(@ApiParam(value = "Preparation Id.") @PathVariable(value = "preparationId") final String preparationId) {
        for(final ExportParameters.SourceType sourceType : ExportParameters.SourceType.values()) {
            evictCache(preparationId, sourceType);
        }
    }

    private void evictCache(final String preparationId, final ExportParameters.SourceType sourceType) {
        final ContentCacheKey metadataKey = cacheKeyGenerator.metadataBuilder()
                .preparationId(preparationId)
                .sourceType(sourceType)
                .build();
        final ContentCacheKey contentKey = cacheKeyGenerator.contentBuilder()
                .preparationId(preparationId)
                .sourceType(sourceType)
                .build();
        contentCache.evictMatch(metadataKey);
        contentCache.evictMatch(contentKey);
    }

    /**
     * Compare the results of 2 sets of actions, and return the diff metadata Ex : the created columns ids
     */
    private StepDiff getCreatedColumns(final PreviewParameters previewParameters) {
        final DataSetGetMetadata dataSetGetMetadata = context.getBean(DataSetGetMetadata.class, previewParameters.getDataSetId());
        DataSetMetadata dataSetMetadata = dataSetGetMetadata.execute();
        StepDiff stepDiff;
        TransformationContext context = new TransformationContext();
        if (dataSetGetMetadata.isSuccessfulExecution() && dataSetMetadata != null) {
            RowMetadata metadataBase = dataSetMetadata.getRowMetadata();
            RowMetadata metadataAfter = metadataBase.clone();

            applyActionsOnMetadata(context, metadataBase, previewParameters.getBaseActions());
            applyActionsOnMetadata(context, metadataAfter, previewParameters.getNewActions());

            metadataAfter.diff(metadataBase);

            List<String> createdColumnIds = metadataAfter.getColumns().stream()
                    .filter(c -> Flag.NEW.getValue().equals(c.getDiffFlagValue()))
                    .map(ColumnMetadata::getId)
                    .collect(Collectors.toList());

            stepDiff = new StepDiff();
            stepDiff.setCreatedColumns(createdColumnIds);
        } else {
            stepDiff = null;
            // maybe throw an exception...
        }
        return stepDiff;
    }

    private void applyActionsOnMetadata(TransformationContext context, RowMetadata metadata, String actionsAsJson) {
        List<Action> actions = actionParser.parse(actionsAsJson);
        ActionContext contextWithMetadata = new ActionContext(context, metadata);
        for (Action action : actions) {
            action.getRowAction().compile(contextWithMetadata);

        }
    }

    /**
     * Execute the preview and write result in the provided output stream
     *
     * @param actions          The actions to execute to diff with reference
     * @param referenceActions The reference actions
     * @param indexes          The record indexes to diff. If null, it will process all records
     * @param dataSet          The dataset (column metadata and records)
     * @param output           The output stream where to write the result
     */
    private void executePreview(final String actions, final String referenceActions, final String indexes, final DataSet dataSet,
                                final OutputStream output) {
        final PreviewConfiguration configuration = PreviewConfiguration.preview() //
                .withActions(actions) //
                .withIndexes(indexes) //
                .fromReference( //
                        Configuration.builder() //
                                .format(JSON) //
                                .output(output) //
                                .actions(referenceActions) //
                                .build() //
                ) //
                .build();
        factory.get(configuration).transform(dataSet, configuration);
    }

    /**
     * Get the action dynamic params.
     */
    //@formatter:off
    @RequestMapping(value = "/transform/suggest/{action}/params", method = POST)
    @ApiOperation(value = "Get the transformation dynamic parameters", notes = "Returns the transformation parameters.")
    @Timed
    public GenericParameter dynamicParams(
            @ApiParam(value = "Action name.") @PathVariable("action") final String action,
            @ApiParam(value = "The column id.") @RequestParam(value = "columnId") final String columnId,
            @ApiParam(value = "Data set content as JSON") final InputStream content) {
        //@formatter:on

        final DynamicType actionType = DynamicType.fromAction(action);
        if (actionType == null) {
            final ExceptionContext exceptionContext = build().put("name", action);
            throw new TDPException(TransformationErrorCodes.UNKNOWN_DYNAMIC_ACTION, exceptionContext);
        }
        try (JsonParser parser = mapper.getFactory().createParser(content)) {
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
            return actionType.getGenerator(context).getParameters(columnId, dataSet);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    /**
     * Returns all {@link ActionDefinition actions} data prep may apply to a column. Column is optional and only needed to
     * fill out default parameter values.
     *
     * @return A list of {@link ActionDefinition} that can be applied to this column.
     * @see #suggest(ColumnMetadata, int)
     */
    @RequestMapping(value = "/actions/column", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Return all actions for a column (regardless of column metadata)", notes = "This operation returns an array of actions.")
    @ResponseBody
    public List<ActionDefinition> columnActions(@RequestBody(required = false) ColumnMetadata column) {
        return actionRegistry.findAll() //
                .filter(action -> !"TEST".equals(action.getCategory()) && action.acceptScope(COLUMN)) //
                .map(am -> column != null ? am.adapt(column) : am) //
                .collect(toList());
    }

    /**
     * Suggest what {@link ActionDefinition actions} can be applied to <code>column</code>.
     *
     * @param column A {@link ColumnMetadata column} definition.
     * @param limit An optional limit parameter to return the first <code>limit</code> suggestions.
     * @return A list of {@link ActionDefinition} that can be applied to this column.
     * @see #suggest(DataSet)
     */
    @RequestMapping(value = "/suggest/column", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Suggest actions for a given column metadata", notes = "This operation returns an array of suggested actions in decreasing order of importance.")
    @ResponseBody
    public List<ActionDefinition> suggest( //
            @RequestBody(required = false) ColumnMetadata column, //
            @ApiParam(value = "How many actions should be suggested at most", defaultValue = "5") @RequestParam(value = "limit", defaultValue = "5") int limit) {

        if (column == null) {
            return Collections.emptyList();
        }

        // look for all actions applicable to the column type
        final List<ActionDefinition> actions;
        try (Stream<ActionDefinition> stream = actionRegistry.findAll()) {
            actions = stream.filter(am -> am.acceptField(column)).collect(toList());
        }
        final List<Suggestion> suggestions = suggestionEngine.score(actions, column);
        return suggestions.stream() //
                .filter(s -> s.getScore() > 0) // Keep only strictly positive score (negative and 0 indicates not
                // applicable)
                .limit(limit) //
                .map(Suggestion::getAction) // Get the action for positive suggestions
                .map(am -> am.adapt(column)) // Adapt default values (e.g. column name)
                .collect(toList());
    }

    /**
     * Returns all {@link ActionDefinition actions} data prep may apply to a line.
     *
     * @return A list of {@link ActionDefinition} that can be applied to a line.
     */
    @RequestMapping(value = "/actions/line", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Return all actions on lines", notes = "This operation returns an array of actions.")
    @ResponseBody
    public List<ActionDefinition> lineActions() {
        try (Stream<ActionDefinition> stream = actionRegistry.findAll()) {
            return stream //
                    .filter(action -> action.acceptScope(LINE)) //
                    .map(action -> action.adapt(LINE)) //
                    .collect(toList());
        }
    }

    /**
     * Suggest what {@link ActionDefinition actions} can be applied to <code>dataSetMetadata</code>.
     *
     * @param dataSet A {@link DataSetMetadata dataset} definition.
     * @return A list of {@link ActionDefinition} that can be applied to this data set.
     * @see #suggest(ColumnMetadata, int)
     */
    @RequestMapping(value = "/suggest/dataset", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Suggest actions for a given data set metadata", notes = "This operation returns an array of suggested actions in decreasing order of importance.")
    @ResponseBody
    public List<ActionDefinition> suggest(DataSet dataSet) {
        return Collections.emptyList();
    }

    /**
     * List all transformation related error codes.
     */
    @RequestMapping(value = "/transform/errors", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all transformation related error codes.", notes = "Returns the list of all transformation related error codes.")
    @Timed
    public Iterable<JsonErrorCodeDescription> listErrors() {
        // need to cast the typed dataset errors into mock ones to use json parsing
        List<JsonErrorCodeDescription> errors = new ArrayList<>(TransformationErrorCodes.values().length);
        for (TransformationErrorCodes code : TransformationErrorCodes.values()) {
            errors.add(new JsonErrorCodeDescription(code));
        }
        return errors;
    }

    /**
     * Get the available export formats
     */
    @RequestMapping(value = "/export/formats", method = GET)
    @ApiOperation(value = "Get the available format types")
    @Timed
    @PublicAPI
    public List<ExportFormat> exportTypes() {
        return formatRegistrationService.getExternalFormats().stream() //
                .sorted(Comparator.comparingInt(ExportFormat::getOrder)) // Enforce strict order.
                .collect(toList());
    }

    @RequestMapping(value = "/dictionary", method = GET, produces = APPLICATION_OCTET_STREAM_VALUE)
    @ApiOperation(value = "Get current dictionary (as serialized object).")
    @Timed
    public StreamingResponseBody getDictionary() {
        return outputStream -> {
            final File dictionaryPath = new File(analyzerService.getIndexesLocation() + "/index/dictionary/default/");
            LOG.debug("Returning dictionary at path '{}'", dictionaryPath.getAbsoluteFile());
            final File keywordPath = new File(analyzerService.getIndexesLocation() + "/index/keyword/default/");
            LOG.debug("Returning keywords at path '{}'", keywordPath.getAbsoluteFile());

            // Read lucene directory and build BroadcastIndexObject instance
            final BroadcastIndexObject dictionary, keyword;
            try (FSDirectory directory = FSDirectory.open(dictionaryPath)) {
                dictionary = new BroadcastIndexObject(directory);
            }
            try (FSDirectory directory = FSDirectory.open(keywordPath)) {
                keyword = new BroadcastIndexObject(directory);
            }

            // Serialize it to output
            LOG.debug("Returning dictionaries '{}' / '{}'", dictionary, keyword);
            Dictionaries result = new Dictionaries(dictionary, keyword);
            try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(outputStream))) {
                oos.writeObject(result);
            }
        };
    }

    /**
     * Return the semantic types for a given preparation / column.
     *
     * @param preparationId the preparation id.
     * @param columnId the column id.
     * @param stepId the step id (optional, if not specified, it's 'head')
     * @return the semantic types for a given preparation / column.
     */
    @RequestMapping(value = "/preparations/{preparationId}/columns/{columnId}/types", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "list the types of the wanted column", notes = "This list can be used by user to change the column type.")
    @Timed
    @PublicAPI
    public List<SemanticDomain> getPreparationColumnSemanticCategories(
            @ApiParam(value = "The preparation id") @PathVariable String preparationId,
            @ApiParam(value = "The column id") @PathVariable String columnId,
            @ApiParam(value = "The preparation version") @RequestParam(defaultValue = "head") String stepId) {

        LOG.debug("listing preparation semantic categories for preparation #{} column #{}@{}", preparationId, columnId, stepId);

        // get the preparation
        final Preparation preparation = getPreparation(preparationId);

        // get the step (in case of 'head', the real step id must be found)
        final String version = StringUtils.equals("head", stepId) ? //
                preparation.getSteps().get(preparation.getSteps().size() - 1) : stepId;

        /*
         * OK, this one is a bit tricky so pay attention.
         *
         * To be able to get the semantic types, the analyzer service needs to run on the result of the preparation.
         *
         * The result must be found in the cache, so if the preparation is not cached, the preparation is run so that
         * it gets cached.
         *
         * Then, the analyzer service just gets the data from the cache. That's it.
         */

        // generate the cache keys for both metadata & content
        final ContentCacheKey metadataKey = cacheKeyGenerator.metadataBuilder() //
                .preparationId(preparationId).stepId(version).sourceType(HEAD).build();

        final ContentCacheKey contentKey = cacheKeyGenerator.contentBuilder() //
                .datasetId(preparation.getDataSetId()).preparationId(preparationId).stepId(version) //
                .format(JSON).sourceType(HEAD) //
                .build();

        // if the preparation is not cached, let's compute it to have some cache
        if (!contentCache.has(metadataKey) || !contentCache.has(contentKey)) {
            addPreparationInCache(preparation, stepId);
        }

        // run the analyzer service on the cached content
        try {
            final DataSetMetadata metadata = mapper.readerFor(DataSetMetadata.class).readValue(contentCache.get(metadataKey));
            final List<SemanticDomain> semanticDomains = getSemanticDomains(metadata, columnId, contentCache.get(contentKey));
            LOG.debug("found {} for preparation #{}, column #{}", semanticDomains, preparationId, columnId);
            return semanticDomains;

        } catch (IOException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Get the preparation from the preparation service.
     *
     * @param preparationId the wanted preparation id.
     * @return the preparation from the preparation service.
     */
    private Preparation getPreparation(@ApiParam(value = "The preparation id") @PathVariable String preparationId) {
        final Preparation preparation;
        try {
            final PreparationDetailsGet details = applicationContext.getBean(PreparationDetailsGet.class, preparationId);
            preparation = mapper.readerFor(Preparation.class).readValue(details.execute());
        } catch (IOException e) {
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, e, build().put("id", preparationId));
        }
        return preparation;
    }

    /**
     * Return the semantic domains for the given parameters.
     *
     * @param metadata the dataset metadata.
     * @param columnId the column id to analyze.
     * @param records the dataset records.
     * @return the semantic domains for the given parameters.
     * @throws IOException can happen...
     */
    private List<SemanticDomain> getSemanticDomains(DataSetMetadata metadata, String columnId, InputStream records)
            throws IOException {

        final ColumnMetadata columnMetadata = metadata.getRowMetadata().getById(columnId);
        final Analyzer<Analyzers.Result> analyzer = analyzerService.build(columnMetadata, SEMANTIC);
        analyzer.init();

        try (final JsonParser parser = mapper.getFactory().createParser(records)) {
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
            dataSet.getRecords() //
                    .map(r -> r.get(columnId)) //
                    .forEach(analyzer::analyze);
            analyzer.end();
        }

        final List<Analyzers.Result> analyzerResult = analyzer.getResult();
        final StatisticsAdapter statisticsAdapter = new StatisticsAdapter(40);
        statisticsAdapter.adapt(singletonList(columnMetadata), analyzerResult);

        return columnMetadata.getSemanticDomains();
    }

    /**
     * Add the following preparation in cache.
     *
     * @param preparation the preparation to cache.
     * @param stepId the preparation step id.
     */
    private void addPreparationInCache(Preparation preparation, String stepId) {
        final ExportParameters exportParameters = new ExportParameters();
        exportParameters.setPreparationId(preparation.getId());
        exportParameters.setExportType("JSON");
        exportParameters.setStepId(stepId);
        exportParameters.setDatasetId(preparation.getDataSetId());

        final StreamingResponseBody streamingResponseBody = executeSampleExportStrategy(exportParameters);
        try {
            // the result is not important here as it will be cached !
            streamingResponseBody.writeTo(new NullOutputStream());
        } catch (IOException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
    }

}
