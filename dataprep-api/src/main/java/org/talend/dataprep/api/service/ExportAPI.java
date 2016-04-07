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

package org.talend.dataprep.api.service;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.talend.dataprep.format.export.ExportFormat.PREFIX;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.api.ExportParameters;
import org.talend.dataprep.api.service.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.api.service.command.export.Export;
import org.talend.dataprep.api.service.command.export.ExportTypes;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.http.HttpRequestContext;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.PublicAPI;

import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
public class ExportAPI extends APIService {

    @RequestMapping(value = "/api/export", method = GET)
    @ApiOperation(value = "Export a dataset", consumes = APPLICATION_FORM_URLENCODED_VALUE, notes = "Export a dataset or a preparation to file. The file type is provided in the request body.")
    public void export(@ApiParam(value = "Export configuration") @Valid final ExportParameters input, //
                       final OutputStream output) {
        try {
            Map<String, String> arguments = new HashMap<>();
            final Enumeration<String> names = HttpRequestContext.parameters();
            while (names.hasMoreElements()) {
                final String paramName = names.nextElement();
                if (StringUtils.contains(paramName, ExportFormat.PREFIX)) {
                    final String paramValue = HttpRequestContext.parameter(paramName);
                    arguments.put(paramName, StringUtils.isNotEmpty(paramValue)? paramValue : EMPTY);
                }
            }
            input.setArguments(arguments);


            String exportName = getExportNameAndConsolidateParameters(input);
            final GenericCommand<InputStream> command = getCommand(Export.class, input, exportName);

            // copy all headers from the command response so that the mime-type is correctly forwarded for instance
            try (InputStream commandInputStream = command.execute()) {

                final Header[] commandResponseHeaders = command.getCommandResponseHeaders();
                for (Header header : commandResponseHeaders) {
                    HttpResponseContext.header(header.getName(), header.getValue());
                }

                IOUtils.copyLarge(commandInputStream, output);
                output.flush();
            }
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
            exportName = parameters.getArguments().get(PREFIX + "fileName");
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
        }
        // deal export name in case of dataset
        else if (StringUtils.isBlank(exportName)){
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
    public void exportTypes(final OutputStream output) {
        final HystrixCommand<InputStream> command = getCommand(ExportTypes.class);
        try (InputStream commandResult = command.execute()) {
            IOUtils.copyLarge(commandResult, output);
            output.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_EXPORT_CONTENT, e);
        }
    }
}
