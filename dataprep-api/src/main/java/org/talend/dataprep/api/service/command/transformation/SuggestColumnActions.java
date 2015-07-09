package org.talend.dataprep.api.service.command.transformation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.APIErrorCodes;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.ReleasableInputStream;
import org.talend.dataprep.api.service.command.common.DataPrepCommand;
import org.talend.dataprep.exception.TDPException;

import com.netflix.hystrix.HystrixCommand;

/**
 * Return the suggested actions to perform on a given column. So far, simple pass through to the transformation api.
 */
@Component
@Scope("request")
public class SuggestColumnActions extends DataPrepCommand<InputStream> {

    /** The column description to get the actions for (in json). */
    private final InputStream input;

    /**
     * Constructor.
     *
     * @param client the http client.
     * @param input the column metadata to get the actions for (in json).
     */
    private SuggestColumnActions(HttpClient client, InputStream input) {
        super(PreparationAPI.TRANSFORM_GROUP, client);
        this.input = input;
    }

    /**
     * @see HystrixCommand#run()
     */
    @Override
    protected InputStream run() throws Exception {

        // if there's no metadata, there's no actions to do...
        if (input == null) {
            // Column does not exist in data set metadata.
            return new ByteArrayInputStream(new byte[0]);
        }

        HttpPost post = new HttpPost(transformationServiceUrl + "/suggest/column");
        post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        post.setEntity(new InputStreamEntity(input));

        HttpResponse response = client.execute(post);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= 200) {
            if (statusCode == HttpStatus.SC_NO_CONTENT || statusCode == HttpStatus.SC_ACCEPTED) {
                // Immediately release connection
                post.releaseConnection();
                return null;
            } else if (statusCode == HttpStatus.SC_OK) {
                return new ReleasableInputStream(response.getEntity().getContent(), post::releaseConnection);
            }
        }
        throw new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_SUGGESTED_ACTIONS);
    }
}
