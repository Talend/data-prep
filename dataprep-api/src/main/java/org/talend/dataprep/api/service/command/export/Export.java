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

package org.talend.dataprep.api.service.command.export;

import static org.talend.dataprep.api.service.command.common.Defaults.pipeStream;
import static org.talend.dataprep.format.export.ExportFormat.PREFIX;
import static org.talend.dataprep.format.export.ExportFormat.Parameter.FILENAME_PARAMETER;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.api.ExportParameters;
import org.talend.dataprep.api.service.command.common.PreparationCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

@Component
@Scope("request")
public class Export extends PreparationCommand<InputStream> {

    private Export(final HttpClient client, final ExportParameters input) {
        super(APIService.TRANSFORM_GROUP, client);
        execute(() -> onExecute(input));
        on(HttpStatus.OK).then(pipeStream());
    }

    /**
     * @param input the export parameters.
     * @return the request to perform.
     */
    private HttpRequestBase onExecute(ExportParameters input) {
        try {

            // export file name comes from :
            // 1. the form parameter
            // 2. the preparation name
            // 3. the dataset name

            String exportName;
            if (input.getArguments().containsKey(PREFIX + FILENAME_PARAMETER)) {
                exportName = input.getArguments().get(PREFIX + FILENAME_PARAMETER);
            } else if (StringUtils.isNotBlank(input.getPreparationId())) {
                final Preparation preparation = getPreparation(input.getPreparationId());
                exportName = preparation.getName();
            }
 else {
                exportName = getDatasetMetadata(input.getDatasetId()).getName();
            }

            return getExportRequest(input, exportName);

        } catch (IOException | URISyntaxException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_EXPORT_CONTENT, e);
        }
    }

    /**
     * @param input the export parameters.
     * @return the http request to perform for the export.
     * @throws URISyntaxException if the transformation url to call is not valid.
     */
    private HttpRequestBase getExportRequest(ExportParameters input, String exportName) throws URISyntaxException, IOException {
        URIBuilder builder;

        // if there's a preparation
        if (StringUtils.isNotBlank(input.getPreparationId())) {

            // dataset id may be null...
            String datasetId;
            if (StringUtils.isBlank(input.getDatasetId())) {
                final Preparation preparation = getPreparation(input.getPreparationId());
                datasetId = preparation.getDataSetId();
            }
 else {
                datasetId = input.getDatasetId();
            }

            String baseUri = transformationServiceUrl //
                    + "/apply/preparation/" + input.getPreparationId() + "/dataset/" + datasetId + '/' + input.getExportType();

            builder = new URIBuilder(baseUri) //
                    .addParameter("stepId", input.getStepId()) //
                    .addParameter("name", exportName);
        }
        // dataset only
        else {
            String baseUri = transformationServiceUrl //
                    + "/export/dataset/" + input.getDatasetId() + '/' + input.getExportType();
            builder = new URIBuilder(baseUri) //
                    .addParameter("name", exportName);
        }

        // add optional/additional parameters
        if (input.getArguments() != null) {
            for (Map.Entry<String, String> entry : input.getArguments().entrySet()) {
                // skip the mandatory export name that's already taken cared of
                if (StringUtils.equals(PREFIX + FILENAME_PARAMETER, entry.getKey())) {
                    continue;
                }
                builder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        return new HttpGet(builder.build());
    }



}
