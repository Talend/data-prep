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

import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.command.Defaults.asString;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Hystrix command used to update a step row metadata.
 */
@Component
@Scope("prototype")
public class UpdateStepRowMetadata extends GenericCommand<String> {

    /**
     * Private constructor to ensure the IoC.
     *
     * @param stepId the step id to update .
     * @param rowMetadata the row metadata to associate with step.
     */
    private UpdateStepRowMetadata(String stepId, RowMetadata rowMetadata) {
        super(PREPARATION_GROUP);
        execute(() -> onExecute(stepId, rowMetadata));
        on(HttpStatus.OK).then(asString());
    }

    private HttpRequestBase onExecute(String stepId, RowMetadata rowMetadata) {
        try {
            final String stepsAsJson = objectMapper.writeValueAsString(rowMetadata);
            final HttpPut updater = new HttpPut(preparationServiceUrl + "/preparations/steps/" + stepId + "/metadata");
            updater.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            updater.setEntity(new StringEntity(stepsAsJson, APPLICATION_JSON));
            return updater;
        } catch (JsonProcessingException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
    }
}
