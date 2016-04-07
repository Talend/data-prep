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

package org.talend.dataprep.api.service.command.export;

import static org.talend.dataprep.command.Defaults.pipeStream;
import static org.talend.dataprep.format.export.ExportFormat.PREFIX;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.api.ExportParameters;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

/**
 * Command used to start an export of a dataset / preparation.
 */
@Component
@Scope("request")
public class Export extends GenericCommand<InputStream> {

    /**
     * Default constructor.
     * @param input the export parameters.
     * @param exportName the name of the export
     */
    // private constructor to ensure the IoC
    private Export(final ExportParameters input, String exportName) {
        super(TRANSFORM_GROUP);
        execute(() -> onExecute(input, exportName));
        on(HttpStatus.OK).then(pipeStream());
    }

    /**
     * @param input the export parameters.
     * @return the request to perform.
     */
    private HttpRequestBase onExecute(ExportParameters input, String exportName) {
        try {

            URIBuilder builder;

            // if there's a preparation
            if (StringUtils.isNotBlank(input.getPreparationId())) {
                String baseUri = transformationServiceUrl //
                        + "/apply/preparation/" + input.getPreparationId() + "/dataset/" + input.getDatasetId() + '/' + input.getExportType();

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

            updateBuilderParameters(input.getArguments(), builder);
            return new HttpGet(builder.build());

        } catch (URISyntaxException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_EXPORT_CONTENT, e);
        }
    }

    /**
     * Add optional/additional parameters.
     *
     * @param arguments the parameters arguments.
     * @param builder the uri builder.
     */
    private void updateBuilderParameters(Map<String, String> arguments, URIBuilder builder) {

        if (arguments == null) {
            return;
        }

        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            // skip the mandatory export name that's already taken cared of
            if (StringUtils.equals(PREFIX + "fileName", entry.getKey())) {
                continue;
            }
            builder.addParameter(entry.getKey(), entry.getValue());
        }
    }
}
