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

package org.talend.dataprep.api.service.command.transformation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.talend.dataprep.command.Defaults.asNull;

import java.io.IOException;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.service.command.common.ChainedCommand;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.core.JsonProcessingException;

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
     * @param retrieveMetadata the previous command to execute.
     */
    private SuggestDataSetActions(DataSetGetMetadata retrieveMetadata) {
        super(GenericCommand.TRANSFORM_GROUP, retrieveMetadata);
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
                return IOUtils.toString(response.getEntity().getContent(), UTF_8);
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
            byte[] dataSetMetadataJSON = objectMapper.writer().writeValueAsBytes(metadata);
            post.setEntity(new ByteArrayEntity(dataSetMetadataJSON));
            return post;

        } catch (JsonProcessingException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
