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

package org.talend.dataprep.api.service;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.talend.dataprep.command.CommandHelper.toStream;
import static org.talend.dataprep.format.export.ExportFormat.PREFIX;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.command.export.DataSetExportTypes;
import org.talend.dataprep.api.service.command.export.Export;
import org.talend.dataprep.api.service.command.export.ExportTypes;
import org.talend.dataprep.api.service.command.export.PreparationExportTypes;
import org.talend.dataprep.command.CommandHelper;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.format.export.ExportFormatMessage;
import org.talend.dataprep.http.HttpRequestContext;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.PublicAPI;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
public class ExportAPI extends APIService {

    @RequestMapping(value = "/api/export", method = GET)
    @ApiOperation(value = "Export a dataset", consumes = APPLICATION_FORM_URLENCODED_VALUE, notes = "Export a dataset or a preparation to file. The file type is provided in the request body.")
    public ResponseEntity<StreamingResponseBody> export(@ApiParam(value = "Export configuration") @Valid final ExportParameters parameters) {
        try {
            Map<String, String> arguments = new HashMap<>();
            final Enumeration<String> names = HttpRequestContext.parameters();
            while (names.hasMoreElements()) {
                final String paramName = names.nextElement();
                if (StringUtils.contains(paramName, ExportFormat.PREFIX)) {
                    final String paramValue = HttpRequestContext.parameter(paramName);
                    arguments.put(paramName, StringUtils.isNotEmpty(paramValue) ? paramValue : StringUtils.EMPTY);
                }
            }
            parameters.getArguments().putAll(arguments);
            final String exportName = getExportNameAndConsolidateParameters(parameters);
            parameters.setExportName(exportName);

            LOG.info("New Export {}", parameters);

            final GenericCommand<InputStream> command = getCommand(Export.class, parameters);
            return CommandHelper.toStreaming(command);
        } catch (TDPException e) {
            throw e;
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_EXPORT_CONTENT, e);
        }
    }

    private String getExportNameAndConsolidateParameters(ExportParameters parameters) {
        // export file name comes from :
        // 1. the form parameter
        // 2. the preparation name
        // 3. the dataset name
        String exportName = EMPTY;
        if (parameters.getArguments().containsKey(PREFIX + "fileName")) {
            return parameters.getArguments().get(PREFIX + "fileName");
        }

        // deal with preparation (update the export name and dataset id if needed)
        if (StringUtils.isNotBlank(parameters.getPreparationId())) {
            final PreparationDetailsGet preparationDetailsGet = getCommand(PreparationDetailsGet.class, parameters.getPreparationId());
            try (InputStream details = preparationDetailsGet.execute()) {
                final Preparation preparation = mapper.readerFor(Preparation.class).readValue(details);
                if (StringUtils.isBlank(exportName)) {
                    exportName = preparation.getName();
                }
                // update the dataset id in the parameters if needed
                if (StringUtils.isBlank(parameters.getDatasetId())) {
                    parameters.setDatasetId(preparation.getDataSetId());
                }
            } catch (IOException e) {
                LOG.warn("unable to get the preparation to for the export", e);
            }
        } else if (StringUtils.isBlank(exportName)){
            // deal export name in case of dataset
            DataSetGetMetadata dataSetGetMetadata = getCommand(DataSetGetMetadata.class, parameters.getDatasetId());
            final DataSetMetadata metadata = dataSetGetMetadata.execute();
            exportName = metadata.getName();
        }
        return exportName;
    }

    /**
     * Get the available export formats
     */
    @RequestMapping(value = "/api/export/formats", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the available format types")
    @Timed
    @PublicAPI
    public Callable<Stream<ExportFormatMessage>> exportTypes() {
        return () -> toStream(ExportFormatMessage.class, mapper, getCommand(ExportTypes.class));
    }

    /**
     * Get the available export formats for preparation
     */
    @RequestMapping(value = "/api/export/formats/preparations/{preparationId}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the available format types for preparation.")
    @Timed
    public Callable<Stream<ExportFormatMessage>> exportTypesForPreparation(@PathVariable("preparationId") String preparationId) {
        return () -> toStream(ExportFormatMessage.class, mapper, getCommand(PreparationExportTypes.class, preparationId));
    }

    /**
     * Get the available export formats for dataset
     */
    @RequestMapping(value = "/api/export/formats/datasets/{dataSetId}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the available format types for preparation.")
    @Timed
    public Callable<Stream<ExportFormatMessage>> exportTypesForDataSet(@PathVariable("dataSetId") String dataSetId) {
        return () -> toStream(ExportFormatMessage.class, mapper, getCommand(DataSetExportTypes.class, dataSetId));
    }
}
