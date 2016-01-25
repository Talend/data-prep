package org.talend.dataprep.api.service.command.folder;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.talend.dataprep.api.service.command.common.Defaults.asNull;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_DELETE_FOLDER;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.api.service.command.common.HttpResponse;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope("request")
public class RemoveFolder extends GenericCommand<HttpResponse> {

    public RemoveFolder(HttpClient client, String path) {
        super(APIService.DATASET_GROUP, client);
        execute(() -> onExecute(path));
        onError(e -> new TDPException(UNABLE_TO_DELETE_FOLDER, e, ExceptionContext.build()));
        on(OK, CONFLICT).then((httpRequestBase, httpResponse) -> {
            try {
                // we transfer status code and content type
                return new HttpResponse(httpResponse.getStatusLine().getStatusCode(), //
                        IOUtils.toString(httpResponse.getEntity().getContent()), //
                        httpResponse.getStatusLine().getStatusCode() == OK.value() ? //
                                TEXT_PLAIN_VALUE :
                                APPLICATION_JSON_VALUE);
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            } finally {
                httpRequestBase.releaseConnection();
            }
        });
    }

    private HttpRequestBase onExecute(String path) {
        try {

            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/folders");
            uriBuilder.addParameter("path", path);
            return new HttpDelete(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
