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

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.command.CommandHelper.toStream;
import static org.talend.dataprep.command.Defaults.asNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.api.service.command.common.ChainedCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * Command that updates an action on a preparation.
 */
@Component
@Scope("request")
public class PreparationUpdateAction extends ChainedCommand<Void, InputStream> {

    /**
     * Default constructor.
     *
     * @param preparationId the preparation id.
     * @param stepId the step id.
     * @param updatedStep the updated step.
     * @param diffInput the metadata difference for this update (as command).
     */
    // private constructor to ensure the IoC use
    private PreparationUpdateAction(final String preparationId, final String stepId, final AppendStep updatedStep, DiffMetadata diffInput) {
        super(PREPARATION_GROUP, diffInput);
        execute(() -> onExecute(preparationId, stepId, updatedStep));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute(String preparationId, String stepId, AppendStep updatedStep) {
        try {
            final String url = preparationServiceUrl + "/preparations/" + preparationId + "/actions/" + stepId;

            final Optional<StepDiff> firstStepDiff = toStream(StepDiff.class, objectMapper, input).findFirst();
            if (firstStepDiff.isPresent()) { // Only interested in first one
                final StepDiff diff = firstStepDiff.get();
                updatedStep.setDiff(diff);
            }
            final String stepAsString = objectMapper.writeValueAsString(updatedStep);

            final HttpPut actionAppend = new HttpPut(url);
            final InputStream stepInputStream = new ByteArrayInputStream(stepAsString.getBytes());

            actionAppend.setHeader(new BasicHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));
            actionAppend.setEntity(new InputStreamEntity(stepInputStream));
            return actionAppend;
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
