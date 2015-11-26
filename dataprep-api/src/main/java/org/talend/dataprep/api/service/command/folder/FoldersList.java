package org.talend.dataprep.api.service.command.folder;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import java.io.InputStream;
import java.net.URISyntaxException;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.api.service.command.common.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_LIST_FOLDERS;

@Component
@Scope("request")
public class FoldersList extends GenericCommand<InputStream> {

    private FoldersList(HttpClient client, String path) {
        super(APIService.DATASET_GROUP, client);
        execute(() -> onExecute(path));
        onError(e -> new TDPException(UNABLE_TO_LIST_FOLDERS, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(pipeStream());
    }

    private FoldersList(HttpClient client) {
        super(APIService.DATASET_GROUP, client);
        execute(() -> onExecute(null));
        onError(e -> new TDPException(UNABLE_TO_LIST_FOLDERS, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(pipeStream());
    }

    private HttpRequestBase onExecute(String path) {
        try {

            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/folders" );

            if (StringUtils.isNotEmpty(path)) {
               uriBuilder.addParameter("path", path);
            }
            return new HttpGet(uriBuilder.build());

        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
