package org.talend.dataprep.transformation.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.ExportType;
import org.talend.dataprep.exception.CommonErrorCodes;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.TDPExceptionContext;
import org.talend.dataprep.exception.json.JsonErrorCodeDescription;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;
import org.talend.dataprep.transformation.api.action.dynamic.DynamicType;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadata;
import org.talend.dataprep.transformation.api.action.parameters.GenericParameter;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.api.transformer.configuration.PreviewConfiguration;
import org.talend.dataprep.transformation.exception.TransformationErrorCodes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@RestController
@Api(value = "transformations", basePath = "/transform", description = "Transformations on data")
public class TransformationService {

    @Autowired
    private ApplicationContext context;

    @Autowired(required = true)
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private ActionMetadata[] allActions;

    @Autowired
    private TransformerFactory factory;

    /**
     * Apply all <code>actions</code> to <code>content</code>. Actions is a Base64-encoded JSON list of
     * {@link ActionMetadata} with parameters.
     *
     * To prevent the actions to exceed URL length limit, everything is shipped within via the multipart request body.
     * Operation allows client to customize the output format (see {@link ExportType available export types}).
     *
     * To prevent the actions to exceed URL length limit, everything is shipped within via the multipart request body.
     *
     * @param format The output {@link ExportType format}. This format also set the MIME response type.
     * @param actions A Base64-encoded list of actions.
     * @param content A JSON input that complies with {@link DataSet} bean.
     * @param response The response used to send transformation result back to client.
     */
    @RequestMapping(value = "/transform/{format}", method = POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Export the preparation applying the transformation", notes = "This operation export the input data transformed using the supplied actions in the provided format.", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @VolumeMetered
    public void transform(@ApiParam(value = "Output format.") @PathVariable("format") final ExportType format, //
            @ApiParam(value = "Actions to perform on content.") @RequestPart(value = "actions", required = false) final Part actions, //
            @ApiParam(value = "Data set content as JSON.") @RequestPart(value = "content", required = false) final Part content, //
            final HttpServletResponse response, final HttpServletRequest request) {
        final ObjectMapper mapper = builder.build();
        try (JsonParser parser = mapper.getFactory().createParser(content.getInputStream())) {
            Map<String, Object> arguments = new HashMap<>();
            final Enumeration<String> names = request.getParameterNames();
            while(names.hasMoreElements()){

                final String paramName = names.nextElement();

                // filter out the content and the actions
                if (StringUtils.equals("actions", paramName) || StringUtils.equals("content", paramName)) {
                    continue;
                }

                final String paramValue = request.getParameter(paramName);
                final String decodeParamValue = new String(Base64.getDecoder().decode(paramValue));

                arguments.put(paramName, decodeParamValue);
            }
            String decodedActions = actions == null ? StringUtils.EMPTY : IOUtils.toString(actions.getInputStream());
            final DataSet dataSet = mapper.reader(DataSet.class).readValue(parser);
            response.setContentType(format.getMimeType());

            Configuration configuration = Configuration.builder() //
                    .format(format)
                    .args(arguments)
                    .output(response.getOutputStream()) //
                    .actions(decodedActions) //
                    .build();
            factory.get(configuration).transform(dataSet, configuration);
        } catch(JsonMappingException e) {
            // Ignore (end of input)
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PARSE_JSON, e);
        } catch (UnsupportedOperationException e) {
            if (format != null) {
                throw new TDPException(TransformationErrorCodes.OUTPUT_TYPE_NOT_SUPPORTED, e);
            }
            throw e;
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
     *
     * To prevent the actions to exceed URL length limit, everything is shipped within via the multipart request body.
     *
     * @param oldActions A list of actions.
     * @param newActions A list of actions.
     * @param indexes Allows client to indicates specific line numbers to focus on.
     * @param content A JSON input that complies with {@link DataSet} bean.
     * @param response The response used to send transformation result back to client.
     */
    @RequestMapping(value = "/transform/preview", method = POST, produces = APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Preview the transformation on input data", notes = "This operation returns the input data diff between the old and the new transformation actions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @VolumeMetered
    public void transformPreview(@ApiParam(value = "Old actions to perform on content.") @RequestPart(value = "oldActions", required = false) final Part oldActions, //
            @ApiParam(value = "New actions to perform on content.") @RequestPart(value = "newActions", required = false) final Part newActions, //
            @ApiParam(value = "The row indexes to return") @RequestPart(value = "indexes", required = false) final Part indexes, //
            @ApiParam(value = "Data set content as JSON") @RequestPart(value = "content", required = false) final Part content, //
            final HttpServletResponse response) {
        final ObjectMapper mapper = builder.build();
        try (JsonParser parser = mapper.getFactory().createParser(content.getInputStream())) {
            final String decodedIndexes = indexes == null ? null : IOUtils.toString(indexes.getInputStream());
            final String decodedOldActions = oldActions == null ? null : IOUtils.toString(oldActions.getInputStream());
            final String decodedNewActions = newActions == null ? null : IOUtils.toString(newActions.getInputStream());
            final DataSet dataSet = mapper.reader(DataSet.class).readValue(parser);

            final PreviewConfiguration configuration = PreviewConfiguration.preview() //
                    .withActions(decodedNewActions) //
                    .withIndexes(decodedIndexes) //
                    .fromReference( //
                            Configuration.builder() //
                                    .format(ExportType.JSON) //
                                    .output(response.getOutputStream()) //
                                    .actions(decodedOldActions) //
                                    .build() //
                    ) //
                    .build();
            factory.get(configuration).transform(dataSet, configuration);
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    /**
     * Suggest what {@link ActionMetadata actions} can be applied to <code>column</code>.
     * @param column A {@link ColumnMetadata column} definition.
     * @return A list of {@link ActionMetadata} that can be applied to this column.
     * @see #suggest(DataSetMetadata)
     */
    @RequestMapping(value = "/suggest/column", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Suggest actions for a given column metadata", notes = "This operation returns an array of suggested actions in decreasing order of importance.")
    @ResponseBody
    public List<ActionMetadata> suggest(@RequestBody(required = false) ColumnMetadata column) {
        if (column == null) {
            return Collections.emptyList();
        }
        // look for all actions applicable to the column type
        return Stream.of(allActions) //
                .filter(am -> am.accept(column)) //
                .map(am -> am.adapt(column)) //
                .collect(Collectors.toList());
    }

    /**
     * Suggest what {@link ActionMetadata actions} can be applied to <code>dataSetMetadata</code>.
     * @param dataSetMetadata A {@link DataSetMetadata dataset} definition.
     * @return A list of {@link ActionMetadata} that can be applied to this data set.
     * @see #suggest(ColumnMetadata)
     */
    @RequestMapping(value = "/suggest/dataset", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Suggest actions for a given data set metadata", notes = "This operation returns an array of suggested actions in decreasing order of importance.")
    @ResponseBody
    public List<ActionMetadata> suggest(DataSetMetadata dataSetMetadata) {
        if (dataSetMetadata == null) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    /**
     * List all transformation related error codes.
     */
    @RequestMapping(value = "/transform/errors", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all transformation related error codes.", notes = "Returns the list of all transformation related error codes.")
    @Timed
    public void listErrors(HttpServletResponse response) {
        try {
            // need to cast the typed dataset errors into mock ones to use json parsing
            List<JsonErrorCodeDescription> errors = new ArrayList<>(TransformationErrorCodes.values().length);
            for (TransformationErrorCodes code : TransformationErrorCodes.values()) {
                errors.add(new JsonErrorCodeDescription(code));
            }
            builder.build().writer().writeValue(response.getOutputStream(), errors);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Get the transformation dynamic params
     */
    @RequestMapping(value = "/transform/suggest/{action}/params", method = POST)
    @ApiOperation(value = "Get the transformation dynamic parameters", notes = "Returns the transformation parameters.")
    @Timed
    public GenericParameter dynamicParams(@ApiParam(value = "Transformation name.") @PathVariable("action") final String action, //
                                          @ApiParam(value = "The column id.") @RequestParam(value = "columnId", required = true) final String columnId, //
                                          @ApiParam(value = "Data set content as JSON") final InputStream content) {
        final DynamicType actionType = DynamicType.fromAction(action);
        if (actionType == null) {
            final TDPExceptionContext context = TDPExceptionContext.build().put("name", action);
            throw new TDPException(TransformationErrorCodes.UNKNOWN_DYNAMIC_ACTION, context);
        }
        final ObjectMapper mapper = builder.build();
        try (JsonParser parser = mapper.getFactory().createParser(content)) {
            final DataSet dataSet = mapper.reader(DataSet.class).readValue(parser);
            return actionType.getGenerator(context).getParameters(columnId, dataSet);
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    /**
     * Get the available export types
     */
    @RequestMapping(value = "/export/types", method = GET)
    @ApiOperation(value = "Get the available export types")
    @Timed
    public void exportTypes(final HttpServletResponse response) {
        List<ExportType> exportTypes = new ArrayList<>(Arrays.asList(ExportType.values()));
        exportTypes.remove(ExportType.JSON); // Don't expose JSON to external callers.
        try {
            builder.build() //
                    .writer() //
                    .writeValue(response.getOutputStream(), exportTypes);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
