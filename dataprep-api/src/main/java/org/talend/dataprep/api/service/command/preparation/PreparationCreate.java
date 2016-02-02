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

import static org.talend.dataprep.api.service.command.common.Defaults.asString;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

@Component
@Scope("request")
public class PreparationCreate extends GenericCommand<String> {

    private PreparationCreate(HttpClient client, Preparation preparation) {
        super(APIService.PREPARATION_GROUP, client);
        execute(() -> onExecute(preparation));
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_CREATE_PREPARATION, e));
        on(HttpStatus.OK).then(asString());
    }

    private HttpRequestBase onExecute(Preparation preparation) {
        HttpPut preparationCreation = new HttpPut(preparationServiceUrl + "/preparations");
        // Serialize preparation using configured serialization
        preparationCreation.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        try {
            byte[] preparationJSONValue = builder.build().writeValueAsBytes(preparation);
            preparationCreation.setEntity(new ByteArrayEntity(preparationJSONValue));
        } catch (IOException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_CREATE_PREPARATION, e);
        }
        return preparationCreation;
    }

}
