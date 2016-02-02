//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.service;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory.COLUMN;
import static org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory.LINE;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.exception.error.PreparationErrorCodes;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.exception.json.JsonErrorCodeDescription;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;
import org.talend.dataprep.transformation.aggregation.AggregationService;
import org.talend.dataprep.transformation.aggregation.api.AggregationParameters;
import org.talend.dataprep.transformation.aggregation.api.AggregationResult;
import org.talend.dataprep.transformation.api.action.dynamic.DynamicType;
import org.talend.dataprep.transformation.api.action.dynamic.GenericParameter;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.api.transformer.configuration.PreviewConfiguration;
import org.talend.dataprep.transformation.api.transformer.suggestion.Suggestion;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngine;
import org.talend.dataprep.transformation.cache.TransformationCacheKey;
import org.talend.dataprep.transformation.format.JsonFormat;
import org.talend.dataprep.transformation.preview.api.PreviewParameters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(value = "transformations", basePath = "/transform", description = "Transformations on data")
public class TransformationService extends BaseTransformationService {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(TransformationService.class);

    /** The Spring application context. */
    @Autowired
    private ApplicationContext context;
    /** All available transformation actions. */
    @Autowired
    private ActionMetadata[] allActions;
    /** he aggregation service. */
    @Autowired
    private AggregationService aggregationService;
    /** The action suggestion engine. */
    @Autowired
    private SuggestionEngine suggestionEngine;

    /** The content cache to... cache transformations results. */
    @Autowired
    private ContentCache contentCache;

    /** The transformer factory. */
    @Autowired
    private TransformerFactory factory;

    /** Task executor for asynchronous processing. */
    @Resource(name = "serializer#json#executor")
    private TaskExecutor executor;

    /**
     * Apply the preparation to the dataset out of the given IDs.
     *
     * @param preparationId the preparation id to apply on the dataset.
     * @param datasetId the dataset id to transform.
     * @param formatName The output {@link ExportFormat format}. This format also set the MIME response type.
     * @param stepId the preparation step id to use (default is 'head').
     * @param name the transformation name.
     * @param exportParams additional (optional) export parameters.
     * @param output Where to write the response.
     */
    //@formatter:off
    @RequestMapping(value = "/apply/preparation/{preparationId}/dataset/{datasetId}/{format}", method = GET)
    @ApiOperation(value = "Transform the given preparation to the given format on the given dataset id", notes = "This operation transforms the dataset using preparation id in the provided format.")
    @VolumeMetered
    public void applyOnDataset(@ApiParam(value = "Preparation id to apply.") @PathVariable(value = "preparationId") final String preparationId,
                               @ApiParam(value = "DataSet id to transform.") @PathVariable(value = "datasetId") final String datasetId,
                               @ApiParam(value = "Output format") @PathVariable("format") final String formatName,
                               @ApiParam(name = "Sample size", value = "Optional sample size to use for the dataset, if missing, the full dataset is returned") @RequestParam(value="sample", required = false) Long sample,
                               @ApiParam(value = "Step id", defaultValue = "head") @RequestParam(value = "stepId", required = false, defaultValue = "head") final String stepId,
                               @ApiParam(value = "Name of the transformation", defaultValue = "untitled") @RequestParam(value = "name", required = false, defaultValue = "untitled") final String name,
                               final @RequestParam Map<String, String> exportParams,
                               final OutputStream output) {
        //@formatter:on

        final ObjectMapper mapper = builder.build();

        final HttpRequestBase datasetRetrieval = getDataSetRequest(datasetId, sample);
        try {
            // get the dataset content
            InputStream datasetContent = getDataSetContent(datasetRetrieval);

            // the parser need to be encapsulated within an auto closeable block so that its records can be fully
            // streamed
            try (JsonParser parser = mapper.getFactory().createParser(datasetContent)) {
                final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
                useCache(preparationId, dataSet, output, formatName, stepId, name, sample, filterRawExportParams(exportParams));
            }

        } catch (Exception e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TRANSFORM_DATASET, e);
        }
        // need to release the connection eventually
        finally {
            datasetRetrieval.releaseConnection();
        }
    }

    /**
     * Export the dataset to the given format.
     *
     * @param datasetId the dataset id to transform.
     * @param formatName The output {@link ExportFormat format}. This format also set the MIME response type.
     * @param name the transformation name.
     * @param exportParams additional (optional) export parameters.
     * @param output Where to write the response.
     */
    //@formatter:off
    @RequestMapping(value = "/export/dataset/{datasetId}/{format}", method = GET)
    @ApiOperation(value = "Export the given dataset")
    @Timed
    public void exportDataset(
            @ApiParam(value = "DataSet id to transform.") @PathVariable(value = "datasetId") final String datasetId,
            @ApiParam(value = "Output format") @PathVariable("format") final String formatName,
            @ApiParam(name = "Sample size", value = "Optional sample size to use for the dataset, if missing, the full dataset is returned") @RequestParam(value="sample", required = false) Long sample,
            @ApiParam(value = "Name of the transformation", defaultValue = "untitled") @RequestParam(value = "name", required = false, defaultValue = "untitled") final String name,
            final @RequestParam Map<String, String> exportParams,
            final OutputStream output) {
        //@formatter:on
        applyOnDataset(null, datasetId, formatName, sample, null, name, exportParams, output);
    }

    /**
     * Get the transformation out of the cache, if it's not cached, performs the transformation and cache its result.
     *
     * @param preparationId the preparation id.
     * @param dataSet the DataSet.
     * @param output where to write the output.
     * @param formatName the format name.
     * @param stepId the preparation step id.
     * @param name the preparation name.
     * @param sample the sample size.
     * @param optionalParams list of optional parameters.
     * @throws IOException if an error occurs.
     */
    private void useCache(String preparationId, DataSet dataSet, OutputStream output, String formatName, String stepId,
            String name, Long sample, Map<String, String> optionalParams) throws IOException {

        String version = stepId;

        // head is not allowed as step id
        if (StringUtils.equals("head", stepId)) {
            Preparation preparation = getPreparation(preparationId);
            version = preparation.getSteps().get(preparation.getSteps().size() - 1);
        }

        // compute the cache key
        TransformationCacheKey key;
        try {
            key = new TransformationCacheKey(preparationId, dataSet.getMetadata(), formatName, version, sample);
        } catch (IOException e) {
            LOG.warn("cannot generate transformation cache key for {}. Cache will not be used.", dataSet.getMetadata(), e);
            internalTransform(preparationId, dataSet, output, formatName, stepId, name, optionalParams);
            return;
        }

        // get it from the cache if available
        final InputStream inputStream = contentCache.get(key);
        if (inputStream != null) {
            setExportHeaders(name, getFormat(formatName));
            IOUtils.copyLarge(inputStream, output);
            return;
        }

        // or save it into the cache
        final OutputStream newCacheEntry = contentCache.put(key, ContentCache.TimeToLive.DEFAULT);
        OutputStream outputStreams = new TeeOutputStream(output, newCacheEntry);
        internalTransform(preparationId, dataSet, outputStreams, formatName, stepId, name, optionalParams);
    }

    /**
     * Compute the given aggregation.
     *
     * @param rawParams the aggregation rawParams as body rawParams.
     */
    // @formatter:off
    @RequestMapping(value = "/aggregate", method = POST, produces = APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Compute the aggregation according to the request body rawParams", consumes = MediaType.APPLICATION_JSON_VALUE)
    @VolumeMetered
    public AggregationResult aggregate(@ApiParam(value = "The aggregation rawParams in json") @RequestBody final String rawParams) {
        // @formatter:on

        final ObjectMapper mapper = builder.build();

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
        HttpRequestBase getContentRequest = null;
        if (StringUtils.isNotBlank(parameters.getPreparationId())) {
            try {
                Preparation preparation = getPreparation(parameters.getPreparationId());
                PipedOutputStream temp = new PipedOutputStream();
                contentToAggregate = new PipedInputStream(temp);

                // because of piped streams, processing must be asynchronous
                Runnable r = () -> applyOnDataset(parameters.getPreparationId(), //
                        preparation.getDataSetId(), //
                        "JSON", //
                        parameters.getSampleSize(), //
                        parameters.getStepId(), //
                        "untitled", //
                        Collections.emptyMap(), // no optional parameters
                        temp);
                executor.execute(r);

            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNABLE_TO_AGGREGATE, e);
            }
        } else {
            // or from the dataset
            getContentRequest = getDataSetRequest(parameters.getDatasetId(), parameters.getSampleSize());
            contentToAggregate = getDataSetContent(getContentRequest);
        }

        // apply the aggregation
        try (JsonParser parser = mapper.getFactory().createParser(contentToAggregate)) {
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
            return aggregationService.aggregate(parameters, dataSet);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        } finally {
            // don't forget to release the connection
            if (getContentRequest != null) {
                getContentRequest.releaseConnection();
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
     * @param rawParameters The preview parameters, encoded in json within the request body.
     * @param output Where to write the response.
     */
    //@formatter:off
    @RequestMapping(value = "/transform/preview", method = POST, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Preview the transformation on input data", notes = "This operation returns the input data diff between the old and the new transformation actions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @VolumeMetered
    public void transformPreview(@ApiParam(name = "body", value = "Preview parameters.") @RequestBody final String rawParameters,
                                 final OutputStream output) {
        //@formatter:on

        final ObjectMapper mapper = builder.build();

        // parse the preview parameters from the request body
        PreviewParameters previewParameters;
        try {
            previewParameters = builder.build().readerFor(PreviewParameters.class).readValue(rawParameters);
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PERFORM_PREVIEW, e);
        }

        // get the dataset content
        final HttpRequestBase dataSetRequest = getDataSetRequest(previewParameters.getDataSetId(), null);
        final InputStream dataSetContent = getDataSetContent(dataSetRequest);

        // because of dataset records streaming, the dataset content must be within an auto closeable block
        try (JsonParser parser = mapper.getFactory().createParser(dataSetContent)) {
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);

            // execute the... preview !
            executePreview(previewParameters.getNewActions(), previewParameters.getBaseActions(), previewParameters.getTdpIds(),
                    dataSet, output);

        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PERFORM_PREVIEW, e);
        }
        // don't forget to release the connection in any case
        finally {
            dataSetRequest.releaseConnection();
        }
    }

    /**
     * Compare the results of 2 sets of actions, and return the diff metadata Ex : the created columns ids
     */
    //@formatter:off
    @RequestMapping(value = "/transform/diff/metadata", method = POST, produces = APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Apply a diff between 2 sets of actions and return the diff (containing created columns ids for example)", notes = "This operation returns the diff metadata", consumes = MediaType.APPLICATION_JSON_VALUE)
    @VolumeMetered
    public StepDiff getCreatedColumns(@ApiParam(name = "body", value = "Preview parameters in json.") @RequestBody final String rawParameters) {
        //@formatter:on

        final ObjectMapper mapper = builder.build();

        // parse parameters
        PreviewParameters previewParameters;
        try {
            previewParameters = mapper.readerFor(PreviewParameters.class).readValue(rawParameters);
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PERFORM_PREVIEW, e);
        }

        // get the dataset content
        final HttpRequestBase datasetGet = getDataSetRequest(previewParameters.getDataSetId(), 1L);
        final InputStream content = getDataSetContent(datasetGet);

        try (JsonParser parser = mapper.getFactory().createParser(content)) {
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);

            final OutputStream output = new ByteArrayOutputStream();

            // call diff
            executePreview(previewParameters.getNewActions(), previewParameters.getBaseActions(), null, dataSet, output);

            // extract created columns ids
            final JsonNode node = mapper.readTree(output.toString());
            final JsonNode columnsNode = node.findPath("columns");
            final List<String> createdColumns = StreamSupport.stream(columnsNode.spliterator(), false)
                    .filter(col -> "new".equals(col.path("__tdpColumnDiff").asText())).map(col -> col.path("id").asText())
                    .collect(toList());

            // create/return diff
            final StepDiff diff = new StepDiff();
            diff.setCreatedColumns(createdColumns);
            LOG.debug("{} creates {} columns", previewParameters, diff);
            return diff;
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        } finally {
            datasetGet.releaseConnection();
        }
    }

    /**
     * Execute the preview and write result in the provided output stream
     *
     * @param actions The actions to execute to diff with reference
     * @param referenceActions The reference actions
     * @param indexes The record indexes to diff. If null, it will process all records
     * @param dataSet The dataset (column metadata and records)
     * @param output The output stream where to write the result
     */
    private void executePreview(final String actions, final String referenceActions, final String indexes, final DataSet dataSet,
            final OutputStream output) {
        final PreviewConfiguration configuration = PreviewConfiguration.preview() //
                .withActions(actions) //
                .withIndexes(indexes) //
                .fromReference( //
                        Configuration.builder() //
                                .format(JsonFormat.JSON) //
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
            @ApiParam(value = "The column id.") @RequestParam(value = "columnId", required = true) final String columnId,
            @ApiParam(value = "Data set content as JSON")  final InputStream content) {
        //@formatter:on

        final DynamicType actionType = DynamicType.fromAction(action);
        if (actionType == null) {
            final ExceptionContext exceptionContext = ExceptionContext.build().put("name", action);
            throw new TDPException(TransformationErrorCodes.UNKNOWN_DYNAMIC_ACTION, exceptionContext);
        }
        final ObjectMapper mapper = builder.build();
        try (JsonParser parser = mapper.getFactory().createParser(content)) {
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
            return actionType.getGenerator(context).getParameters(columnId, dataSet);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    /**
     * Returns all {@link ActionMetadata actions} data prep may apply to a column. Column is optional and only needed to
     * fill out default parameter values.
     *
     * @return A list of {@link ActionMetadata} that can be applied to this column.
     * @see #suggest(ColumnMetadata, int)
     */
    @RequestMapping(value = "/actions/column", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Return all actions for a column (regardless of column metadata)", notes = "This operation returns an array of actions.")
    @ResponseBody
    public List<ActionMetadata> columnActions(@RequestBody(required = false) ColumnMetadata column) {
        return Stream.of(allActions) //
                .filter(action -> action.acceptScope(COLUMN)) //
                .map(am -> column != null ? am.adapt(column) : am) //
                .collect(toList());
    }

    /**
     * Suggest what {@link ActionMetadata actions} can be applied to <code>column</code>.
     *
     * @param column A {@link ColumnMetadata column} definition.
     * @param limit An optional limit parameter to return the first <code>limit</code> suggestions.
     * @return A list of {@link ActionMetadata} that can be applied to this column.
     * @see #suggest(DataSet)
     */
    @RequestMapping(value = "/suggest/column", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Suggest actions for a given column metadata", notes = "This operation returns an array of suggested actions in decreasing order of importance.")
    @ResponseBody
    public List<ActionMetadata> suggest(@RequestBody(required = false) ColumnMetadata column, //
            @ApiParam(value = "How many actions should be suggested at most", defaultValue = "5") @RequestParam(value = "limit", defaultValue = "5", required = false) int limit) {
        if (column == null) {
            return Collections.emptyList();
        }
        // look for all actions applicable to the column type
        final List<ActionMetadata> actions = Stream.of(allActions) //
                .filter(am -> am.acceptColumn(column)) // Filter on acceptable columns (for type)
                .collect(toList());
        final List<Suggestion> suggestions = suggestionEngine.score(actions, column);
        return suggestions.stream() //
                .filter(s -> s.getScore() > 0) // Keep only strictly positive score (negative and 0 indicates not
                // applicable)
                .limit(limit) //
                .map(Suggestion::getAction) // Get the action for positive suggestions
                .map(am -> am.adapt(column)) // Adapt default values (e.g. column name)
                .collect(Collectors.toList());
    }

    /**
     * Returns all {@link ActionMetadata actions} data prep may apply to a line.
     *
     * @return A list of {@link ActionMetadata} that can be applied to a line.
     */
    @RequestMapping(value = "/actions/line", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Return all actions on lines", notes = "This operation returns an array of actions.")
    @ResponseBody
    public List<ActionMetadata> lineActions() {
        return Stream.of(allActions) //
                .filter(action -> action.acceptScope(LINE)) //
                .map(action -> action.adapt(LINE)) //
                .collect(toList());
    }

    /**
     * Suggest what {@link ActionMetadata actions} can be applied to <code>dataSetMetadata</code>.
     *
     * @param dataSet A {@link DataSetMetadata dataset} definition.
     * @return A list of {@link ActionMetadata} that can be applied to this data set.
     * @see #suggest(ColumnMetadata, int)
     */
    @RequestMapping(value = "/suggest/dataset", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Suggest actions for a given data set metadata", notes = "This operation returns an array of suggested actions in decreasing order of importance.")
    @ResponseBody
    public List<ActionMetadata> suggest(DataSet dataSet) {
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
    public List<ExportFormat> exportTypes() {
        return formatRegistrationService.getExternalFormats().stream() //
                .sorted((f1, f2) -> f1.getOrder() - f2.getOrder()) // Enforce strict order.
                .collect(Collectors.toList());
    }

    /**
     * Return the http request to use to get the dataset content.
     *
     * @param datasetId the wanted dataset id.
     * @param sample the optional sample size.
     * @return the http request to use to get the dataset content.
     */
    private HttpRequestBase getDataSetRequest(String datasetId, Long sample) {

        String datasetGetUrl = datasetServiceUrl + "/datasets/" + datasetId + "/content";
        if (sample != null) {
            datasetGetUrl += "?sample=" + sample;
        }

        return new HttpGet(datasetGetUrl);
    }

    /**
     * Return the dataset content as input stream out of the given http get request.
     *
     * @param datasetRetrieval the http request to perform.
     * @return the dataset content as input stream out of the given http get request.
     */
    private InputStream getDataSetContent(HttpRequestBase datasetRetrieval) {
        try {
            final HttpResponse datasetGet = httpClient.execute(datasetRetrieval);
            final HttpStatus response = HttpStatus.valueOf(datasetGet.getStatusLine().getStatusCode());
            if (response.is4xxClientError() || response.is5xxServerError() || response.value() == HttpStatus.NO_CONTENT.value()) {
                throw new IOException("could not read dataset");
            }
            return datasetGet.getEntity().getContent();
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT, e);
        }
    }

    /**
     * @param preparationId the wanted preparation id.
     * @return the preparation out of its id.
     */
    private Preparation getPreparation(String preparationId) {
        String datasetGetUrl = preparationServiceUrl + "/preparations/" + preparationId;
        final HttpGet httpRequest = new HttpGet(datasetGetUrl);
        try {
            final HttpResponse preparationGet = httpClient.execute(httpRequest);
            final HttpStatus response = HttpStatus.valueOf(preparationGet.getStatusLine().getStatusCode());
            if (response.is4xxClientError() || response.is5xxServerError() || response.value() == HttpStatus.NO_CONTENT.value()) {
                throw new IOException("could not read preparation " + preparationId + " -> " + preparationGet.getStatusLine());
            }
            return (Preparation) builder.build().readerFor(Preparation.class).readValue(preparationGet.getEntity().getContent());
        } catch (IOException e) {
            throw new TDPException(PreparationErrorCodes.UNABLE_TO_READ_PREPARATION, e);
        }
    }

}
