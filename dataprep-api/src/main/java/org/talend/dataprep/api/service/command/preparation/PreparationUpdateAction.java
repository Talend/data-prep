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

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.api.service.command.common.Defaults.asNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.PreparationCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope("request")
public class PreparationUpdateAction extends PreparationCommand<Void> {

    private final String stepId;

    private final AppendStep updatedStep;

    private final String preparationId;

    private PreparationUpdateAction(final HttpClient client, final String preparationId, final String stepId, final AppendStep updatedStep) {
        super(APIService.PREPARATION_GROUP, client);
        this.stepId = stepId;
        this.updatedStep = updatedStep;
        this.preparationId = preparationId;
        execute(() -> onExecute(preparationId, stepId, updatedStep));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute(String preparationId, String stepId, AppendStep updatedStep) {
        try {
            final StepDiff diff = getDiffMetadata();
            updatedStep.setDiff(diff);
            final HttpPut actionAppend = new HttpPut(
                    preparationServiceUrl + "/preparations/" + preparationId + "/actions/" + stepId); //$NON-NLS-1$ //$NON-NLS-2$
            final String stepAsString = builder.build().writeValueAsString(updatedStep);
            final InputStream stepInputStream = new ByteArrayInputStream(stepAsString.getBytes());

            actionAppend.setHeader(new BasicHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));
            actionAppend.setEntity(new InputStreamEntity(stepInputStream));
            return actionAppend;
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Get the diff metadata introduced by the step to append (ex : the created columns)
     */
    private StepDiff getDiffMetadata() throws IOException {
        final Preparation preparation = getPreparation(preparationId);
        final int stepIndex = preparation.getSteps().indexOf(stepId);
        final String parentStepId = preparation.getSteps().get(stepIndex - 1);

        return getDiffMetadata(preparationId, parentStepId, updatedStep.getActions());
    }
}
