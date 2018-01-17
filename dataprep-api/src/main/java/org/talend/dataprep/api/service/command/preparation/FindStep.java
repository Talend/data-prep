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

import static org.talend.dataprep.command.Defaults.convertResponse;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.net.URISyntaxException;

import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

/**
 * Fetches a preparation step {@link Step } containing all its ancestors.
 */
@Component
@Scope("prototype")
public class FindStep extends GenericCommand<Step> {

    private final String stepId;

    private FindStep(String stepId) {
        super(PREPARATION_GROUP);
        this.stepId = stepId;
    }

    @PostConstruct
    private void init() {
        execute(() -> onExecute(stepId));
        on(HttpStatus.OK).then(convertResponse(objectMapper, Step.class));
    }

    private HttpRequestBase onExecute(String id) {
        try {
            URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl + "/steps/" + id);
            return new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
    }

}
