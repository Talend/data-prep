package org.talend.dataprep.api.service.command.folder;

import static org.talend.dataprep.api.service.command.common.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_LIST_FOLDERS;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_LIST_FOLDER_ENTRIES;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope("request")
public class FolderEntriesList extends GenericCommand<InputStream> {

    private FolderEntriesList(HttpClient client, String path, String contentType) {
        super(APIService.DATASET_GROUP, client);
        execute(() -> onExecute(path, contentType));
        onError(e -> new TDPException(UNABLE_TO_LIST_FOLDER_ENTRIES, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(pipeStream());
    }

    private HttpRequestBase onExecute(String path,  String contentType) {
        try {

            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/folders/entries");

            if (StringUtils.isNotEmpty(path)) {
                uriBuilder.addParameter("path", path);
            }

            if (StringUtils.isNotEmpty(contentType)) {
                uriBuilder.addParameter("contentType", contentType);
            }

            return new HttpGet(uriBuilder.build());

        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
