package org.talend.dataprep.api.service.command.folder;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import java.net.URISyntaxException;

import static org.talend.dataprep.api.service.command.common.Defaults.asNull;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_DELETE_FOLDER;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_DELETE_FOLDER_ENTRY;

@Component
@Scope("request")
public class RemoveFolderEntry
    extends GenericCommand<Void> {

    private RemoveFolderEntry( HttpClient client, String path, String contentType, String contentId ) {
        super(APIService.DATASET_GROUP, client);
        execute(() -> onExecute(path, contentId, contentType));
        onError(e -> new TDPException(UNABLE_TO_DELETE_FOLDER_ENTRY, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute( String path, String contentId, String contentType ) {
        try {

            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/folders/entries/" //
                                                       + contentType //
                                                       + '/' + contentId);

            uriBuilder.addParameter( "path", path );
            return new HttpDelete(uriBuilder.build());

        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
