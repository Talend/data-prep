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

import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

/**
 * Return the suggested actions to perform on a given column. So far, simple pass through to the transformation api.
 */
@Component
@Scope("request")
public class SuggestColumnActions extends GenericCommand<InputStream> {

    /**
     * Constructor.
     *
     * @param input the column metadata to get the actions for (in json).
     */
    private SuggestColumnActions(InputStream input) {
        super(GenericCommand.TRANSFORM_GROUP);
        execute(() -> {
            HttpPost post = new HttpPost(transformationServiceUrl + "/suggest/column");
            post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            post.setEntity(new InputStreamEntity(input));
            return post;
        });
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_SUGGESTED_ACTIONS, e));
        on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(asNull());
        on(HttpStatus.OK).then(pipeStream());
    }

}
