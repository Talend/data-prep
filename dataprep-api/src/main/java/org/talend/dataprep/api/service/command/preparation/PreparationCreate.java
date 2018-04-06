// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service.command.preparation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.BaseErrorCodes.UNEXPECTED_EXCEPTION;
import static org.talend.dataprep.command.Defaults.asString;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_CREATE_PREPARATION;

@Component
@Scope(SCOPE_PROTOTYPE)
public class PreparationCreate extends GenericCommand<String> {

    /**
     * Private constructor to ensure the use of IoC.
     *
     * @param preparation the preparation to create.
     * @param folderId the optional folder ID to create the preparation into.
     */
    private PreparationCreate(Preparation preparation, String folderId) {
        super(GenericCommand.PREPARATION_GROUP);
        execute(() -> onExecute(preparation, folderId));
        on(HttpStatus.OK).then(asString());
    }

    private HttpRequestBase onExecute(Preparation preparation, String folderId) {
        URI uri;
        try {
            URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl);
            uriBuilder.setPath(uriBuilder.getPath() + "/preparations");
            if (StringUtils.isNotBlank(folderId)) {
                uriBuilder.setParameter("folderId", folderId);
            }

            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }

        HttpPost preparationCreation = new HttpPost(uri);

        // Serialize preparation using configured serialization
        preparationCreation.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        try {
            byte[] preparationJSONValue = objectMapper.writeValueAsBytes(preparation);
            preparationCreation.setEntity(new ByteArrayEntity(preparationJSONValue));
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_CREATE_PREPARATION, e);
        }
        return preparationCreation;
    }
}
