// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Hystrix command used to update a step row etadata.
 */
@Component
@Scope("request")
public class UpdateStepRowMetadata extends GenericCommand<String> {

    /**
     * Private constructor to ensure the IoC.
     *
     * @param preparationId the preparation id to update the step from.
     * @param steps the steps to update.
     */
    private UpdateStepRowMetadata(String preparationId, List<Step> steps) {
        super(PREPARATION_GROUP);
        execute(() -> onExecute(preparationId, steps));
        on(HttpStatus.OK).then(asString());
    }

    private HttpRequestBase onExecute(String preparationId, List<Step> steps) {
        try {
            final String stepsAsJson = objectMapper.writeValueAsString(steps);
            final HttpPut preparationCreation = new HttpPut(preparationServiceUrl + "/preparations/" + preparationId + "/steps");
            preparationCreation.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            preparationCreation.setEntity(new StringEntity(stepsAsJson));
            return preparationCreation;
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
    }
}
