package org.talend.dataprep.api.service.command.folder;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.http.HttpStatus;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import java.io.InputStream;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.api.service.command.common.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_LIST_FOLDERS;

public class FoldersList extends GenericCommand<InputStream> {

    private FoldersList(HttpClient client, Folder folder) {
        super(APIService.PREPARATION_GROUP, client);
        execute(() -> onExecute(folder));
        onError(e -> new TDPException(UNABLE_TO_LIST_FOLDERS, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(pipeStream());
    }

    private HttpRequestBase onExecute(Folder folder) {
        try {
            final byte[] folderJSONValue = builder.build().writeValueAsBytes(folder);
            final HttpPut preparationCreation = new HttpPut(datasetServiceUrl + "/folders/childs" );
            preparationCreation.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            preparationCreation.setEntity(new ByteArrayEntity(folderJSONValue));
            return preparationCreation;
        } catch (JsonProcessingException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
