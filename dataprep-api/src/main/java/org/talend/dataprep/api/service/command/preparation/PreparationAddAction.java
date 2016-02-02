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

import static org.talend.dataprep.api.service.command.common.Defaults.asNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.PreparationCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope("request")
public class PreparationAddAction extends PreparationCommand<Void> {

    private PreparationAddAction(final HttpClient client, final String preparationId, final AppendStep step) {
        super(APIService.PREPARATION_GROUP, client);
        execute(() -> onExecute(preparationId, step));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute(String preparationId, AppendStep step) {
        try {
            final StepDiff diff = getDiffMetadata(preparationId, "head", step.getActions());
            step.setDiff(diff);
            final HttpPost actionAppend = new HttpPost(preparationServiceUrl + "/preparations/" + preparationId + "/actions"); //$NON-NLS-1$ //$NON-NLS-2$
            final String stepAsString = builder.build().writeValueAsString(step);
            final InputStream stepInputStream = new ByteArrayInputStream(stepAsString.getBytes());
            actionAppend.setHeader(new BasicHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)); //$NON-NLS-1$
            actionAppend.setEntity(new InputStreamEntity(stepInputStream));
            return actionAppend;
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
