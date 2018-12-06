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

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.talend.dataprep.command.CommandHelper.toStream;
import static org.talend.dataprep.format.export.ExportFormat.PREFIX;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.api.service.command.export.DataSetExportTypes;
import org.talend.dataprep.api.service.command.export.Export;
import org.talend.dataprep.api.service.command.export.ExportTypes;
import org.talend.dataprep.api.service.command.export.PreparationExportTypes;
import org.talend.dataprep.audit.BaseDataprepAuditService;
import org.talend.dataprep.command.CommandHelper;
import org.talend.dataprep.command.preparation.PreparationSummaryGet;
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

    @Autowired
    private BaseDataprepAuditService auditService;

    @GetMapping(value = "/api/export")
    @ApiOperation(value = "Export a dataset", consumes = APPLICATION_FORM_URLENCODED_VALUE,
            notes = "Export a dataset or a preparation to file. The file type is provided in the request body.")
    public ResponseEntity<StreamingResponseBody>
            export(@ApiParam(value = "Export configuration") @Valid final ExportParameters parameters) {
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
            ResponseEntity<StreamingResponseBody> responseEntity =
                    CommandHelper.toStreaming(getCommand(Export.class, parameters));
            if (!"head".equals(parameters.getStepId())) {
                // This endpoint is called 2 times by the front, once with http.HEAD, once with http.GET
                // When called with http.HEAD request, stepId is always equals to "head"
                // As we only want to log a message when called with http.GET we check that stepId=="head"
                auditService.auditPreparationSampleExport(parameters.getPreparationId(), parameters.getStepId(),
                        parameters.getExportType(), parameters.getArguments());
            }
            return responseEntity;
        } catch (TDPException e) {
            throw e;
        } catch (Exception e) {
            final ExceptionContext context = ExceptionContext.build().put("ExportParameters", parameters);
            throw new TDPException(APIErrorCodes.UNABLE_TO_EXPORT_CONTENT, e, context);
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
            final PreparationSummaryGet preparationSummaryGet =
                    getCommand(PreparationSummaryGet.class, parameters.getPreparationId());
            final PreparationDTO preparation = preparationSummaryGet.execute();
            if (StringUtils.isBlank(exportName)) {
                exportName = preparation.getName();
            }
            // update the dataset id in the parameters if needed
            if (StringUtils.isBlank(parameters.getDatasetId())) {
                parameters.setDatasetId(preparation.getDataSetId());
            }
        } else if (StringUtils.isBlank(exportName)) {
            // deal export name in case of dataset
            final DataSetMetadata metadata = datasetClient.getDataSetMetadata(parameters.getDatasetId());
            exportName = metadata.getName();
        }
        return exportName;
    }

    /**
     * Get the available export formats
     */
    @GetMapping(value = "/api/export/formats", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the available format types")
    @Timed
    @PublicAPI
    public Callable<Stream<ExportFormatMessage>> exportTypes() {
        return () -> toStream(ExportFormatMessage.class, mapper, getCommand(ExportTypes.class));
    }

    /**
     * Get the available export formats for preparation
     */
    @GetMapping(value = "/api/export/formats/preparations/{preparationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the available format types for preparation.")
    @Timed
    public Callable<Stream<ExportFormatMessage>>
            exportTypesForPreparation(@PathVariable("preparationId") String preparationId) {
        return () -> toStream(ExportFormatMessage.class, mapper,
                getCommand(PreparationExportTypes.class, preparationId));
    }

    /**
     * Get the available export formats for dataset
     */
    @GetMapping(value = "/api/export/formats/datasets/{dataSetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the available format types for preparation.")
    @Timed
    public Callable<Stream<ExportFormatMessage>> exportTypesForDataSet(@PathVariable("dataSetId") String dataSetId) {
        return () -> toStream(ExportFormatMessage.class, mapper, getCommand(DataSetExportTypes.class, dataSetId));
    }
}
