package org.talend.dataprep.api.service.command.folder;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.api.service.command.common.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_LIST_FOLDERS;

@Component
@Scope("request")
public class CreateChildFolder extends GenericCommand<InputStream> {

    private CreateChildFolder(HttpClient client, Folder folder, String parentId) {
        super(APIService.DATASET_GROUP, client);
        execute(() -> onExecute(folder, parentId));
        onError(e -> new TDPException(UNABLE_TO_LIST_FOLDERS, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(pipeStream());
    }

    private HttpRequestBase onExecute(Folder folder, String parentId) {
        try {
            byte[] folderJSONValue = builder.build().writeValueAsBytes(folder);

            StringBuilder url = new StringBuilder(datasetServiceUrl + "/folders");
            if (StringUtils.isNotEmpty(parentId)){
                url.append("?parentId=").append(parentId);
            }

            HttpPut folderList = new HttpPut( url.toString() );
            folderList.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            folderList.setEntity(new ByteArrayEntity(folderJSONValue));
            return folderList;
        } catch (JsonProcessingException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
