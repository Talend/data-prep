//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.command.dataset;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.talend.daikon.exception.ExceptionContext.build;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.command.common.HttpResponse;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * Command to move a dataset to an other folder.
 */
@Component
@Scope("request")
public class MoveDataSet extends GenericCommand<HttpResponse> {

    /**
     * Constructor.
     *
     * @param dataSetId the requested dataset id.
     * @param folderPath the origin folder othe the dataset
     * @param newFolderPath the new folder path
     * @param newName the new name (optional)
     */
    public MoveDataSet(String dataSetId, String folderPath, String newFolderPath, String newName) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> {
            try {
                URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/datasets/move/" + dataSetId);
                if (StringUtils.isNotEmpty(folderPath)) {
                    uriBuilder.addParameter("folderPath", folderPath);
                }
                if (StringUtils.isNotEmpty(newFolderPath)) {
                    uriBuilder.addParameter("newFolderPath", newFolderPath);
                }
                if (StringUtils.isNotEmpty(newName)) {
                    uriBuilder.addParameter("newName", newName);
                }
                return new HttpPut(uriBuilder.build());
            } catch (URISyntaxException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        });

        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_COPY_DATASET_CONTENT, e, build().put("id", dataSetId)));

        on(HttpStatus.OK, HttpStatus.BAD_REQUEST).then((httpRequestBase, httpResponse) -> {
            try {
                // we transfer status code and content type
                return new HttpResponse(httpResponse.getStatusLine().getStatusCode(), //
                        IOUtils.toString(httpResponse.getEntity().getContent(), UTF_8), //
                        httpResponse.getStatusLine().getStatusCode() == BAD_REQUEST.value() ? //
                                APPLICATION_JSON_VALUE : TEXT_PLAIN_VALUE);
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            } finally {
                httpRequestBase.releaseConnection();
            }
        });
    }

}
