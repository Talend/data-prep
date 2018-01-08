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

import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.function.BiFunction;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope("request")
public class DataSetPreview extends GenericCommand<InputStream> {

    public DataSetPreview(String dataSetId, boolean metadata, String sheetName) {
        super(GenericCommand.TRANSFORM_GROUP);
        execute(() -> onExecute(dataSetId, metadata, sheetName));
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_DATASET_CONTENT, e,
                ExceptionContext.build().put("id", dataSetId)));
        on(HttpStatus.ACCEPTED, HttpStatus.NO_CONTENT).then(emptyStream());
        on(HttpStatus.OK).then(pipeStream());
        // Move permanently/temporarily behaviors
        BiFunction<HttpRequestBase, HttpResponse, InputStream> move = (req, res) -> {
            Exception cause = new Exception(res.getStatusLine().getStatusCode() + ":" //
                    + res.getStatusLine().getReasonPhrase());
            throw new TDPException(APIErrorCodes.DATASET_REDIRECT, cause, ExceptionContext.build().put("id", dataSetId));
        };
        on(HttpStatus.MOVED_PERMANENTLY, HttpStatus.FOUND).then(move);
    }

    private HttpRequestBase onExecute(String dataSetId, boolean metadata, String sheetName) {
        try {

            URIBuilder uriBuilder = new URIBuilder( datasetServiceUrl + "/datasets/" + dataSetId + "/preview/" );
            uriBuilder.addParameter( "metadata", Boolean.toString( metadata ) );
            if (StringUtils.isNotEmpty(sheetName)) {
                // yup this sheet name can contains weird characters space, great french accents or even chinese
                // characters
                uriBuilder.addParameter("sheetName",sheetName );
            }
            return new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
