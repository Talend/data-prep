//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.command.preparation;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.command.Defaults.asString;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_CREATE_PREPARATION;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.elasticsearch.common.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

@Component
@Scope("request")
public class PreparationCreate extends GenericCommand<String> {

    /**
     * Private constructor to ensure the use of IoC.
     * @param preparation the preparation to create.
     * @param folder the optional folder name to create the preparation into.
     */
    private PreparationCreate(Preparation preparation, String folder) {
        super(GenericCommand.PREPARATION_GROUP);
        execute(() -> onExecute(preparation, folder));
        onError(e -> new TDPException(UNABLE_TO_CREATE_PREPARATION, e));
        on(HttpStatus.OK).then(asString());
    }

    private HttpRequestBase onExecute(Preparation preparation, String folder) {

        String uri = preparationServiceUrl + "/preparations";
        if (StringUtils.isNotBlank(folder)) {
            uri += "?folder="+folder;
        }

        HttpPost preparationCreation = new HttpPost(uri);

        // Serialize preparation using configured serialization
        preparationCreation.setHeader("Content-Type", APPLICATION_JSON_VALUE);
        try {
            byte[] preparationJSONValue = objectMapper.writeValueAsBytes(preparation);
            preparationCreation.setEntity(new ByteArrayEntity(preparationJSONValue));
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_CREATE_PREPARATION, e);
        }
        return preparationCreation;
    }
}
