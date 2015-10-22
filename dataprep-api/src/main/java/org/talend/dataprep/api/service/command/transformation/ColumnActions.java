package org.talend.dataprep.api.service.command.transformation;

import static org.talend.dataprep.api.service.command.common.Defaults.asNull;
import static org.talend.dataprep.api.service.command.common.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

/**
 * Return all actions that can be performed on a given column. So far, simple pass through to the transformation api.
 */
@Component
@Scope("request")
public class ColumnActions extends GenericCommand<InputStream> {

    /**
     * Constructor.
     *
     * @param client the http client.
     * @param input the column metadata to get the actions for (in json).
     */
    private ColumnActions(HttpClient client, InputStream input) {
        super(PreparationAPI.TRANSFORM_GROUP, client);
        execute(() -> {
            HttpPost post = new HttpPost(transformationServiceUrl + "/actions/column");
            post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            post.setEntity(new InputStreamEntity(input));
            return post;
        });
        onError((e) -> new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_SUGGESTED_ACTIONS, e));
        on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(asNull());
        on(HttpStatus.OK).then(pipeStream());
    }

}
