package org.talend.dataprep.api.service.command.transformation;

import static org.talend.dataprep.api.service.command.common.Defaults.asNull;

import java.io.IOException;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.common.ChainedCommand;
import org.talend.dataprep.api.service.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * Retrieve actions for a dataset.
 * </p>
 *
 * <p>
 * A string is returned instead of the usual InputStream because :
 * <ol>
 * <li>the actions will not be too big</li>
 * <li>this command is chained with the SuggestLookupActions, hence if the latter fails, it is easy to fallback</li>
 * </ol>
 * </p>
 */
@Component
@Scope("request")
public class SuggestDataSetActions extends ChainedCommand<String, DataSetMetadata> {

    /**
     * Constructor.
     *
     * @param client the http client to use.
     * @param retrieveMetadata the previous command to execute.
     */
    private SuggestDataSetActions(HttpClient client, DataSetGetMetadata retrieveMetadata) {
        super(PreparationAPI.TRANSFORM_GROUP, client, retrieveMetadata);
        execute(this::onExecute);
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_SUGGESTED_ACTIONS, e));
        on(HttpStatus.OK).then(onOk());
        on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(asNull());
    }

    /**
     * @return the function to use when processing the response.
     */
    private BiFunction<HttpRequestBase, HttpResponse, String> onOk() {
        return (request, response) -> {
            try {
                return IOUtils.toString(response.getEntity().getContent());
            } catch (IOException e) {
                throw new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_SUGGESTED_ACTIONS, e);
            }
        };
    }

    /**
     * Retrieve the dataset metadata and look for the possible actions.
     * 
     * @return the dataset possible actions.
     */
    private HttpRequestBase onExecute() {
        try {

            // retrieve dataset metadata
            DataSetMetadata metadata = getInput();

            // queries its possible actions
            final HttpPost post = new HttpPost(transformationServiceUrl + "/suggest/dataset");
            post.setHeader(new BasicHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE));
            ObjectMapper objectMapper = builder.build();
            byte[] dataSetMetadataJSON = objectMapper.writer().writeValueAsBytes(metadata);
            post.setEntity(new ByteArrayEntity(dataSetMetadataJSON));
            return post;

        } catch (JsonProcessingException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
