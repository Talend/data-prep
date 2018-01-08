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

package org.talend.dataprep.command.preparation;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.command.Defaults.asString;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_UPDATE_PREPARATION;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.core.JsonProcessingException;

@Component
@Scope("prototype")
public class PreparationUpdate extends GenericCommand<String> {

    private PreparationUpdate(String id, Preparation preparation) {
        super(GenericCommand.PREPARATION_GROUP);
        execute(() -> onExecute(id, preparation));
        onError(e -> new TDPException(UNABLE_TO_UPDATE_PREPARATION, e, ExceptionContext.build().put("id", id)));
        on(HttpStatus.OK).then(asString());
    }

    private HttpRequestBase onExecute(String id, Preparation preparation) {
        try {
            final byte[] preparationJSONValue = objectMapper.writeValueAsBytes(preparation);
            final HttpPut preparationCreation = new HttpPut(preparationServiceUrl + "/preparations/" + id);
            preparationCreation.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            preparationCreation.setEntity(new ByteArrayEntity(preparationJSONValue));
            return preparationCreation;
        } catch (JsonProcessingException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
