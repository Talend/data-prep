// ============================================================================
//
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

import static org.talend.dataprep.command.Defaults.asString;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

/**
 * Command used to move a step within a preparation.
 */
@Component
@Scope("request")
public class PreparationReorderStep extends GenericCommand<String> {

    /**
     * Constructor.
     *
     * @param preparationId the id of the preparation containing the step to move
     * @param stepId the id of the step to move
     * @param parentStepId the id of the step which wanted as the parent of the step to move
     */
    // private constructor to ensure use of IoC
    private PreparationReorderStep(final String preparationId, final String stepId, final String parentStepId) {
        super(PREPARATION_GROUP);
        execute(() -> onExecute(preparationId, stepId, parentStepId));
        on(HttpStatus.OK).then(asString());
    }

    private HttpRequestBase onExecute(final String preparationId, final String stepId, final String parentStepId) {
        try {
            URIBuilder builder = new URIBuilder(
                    preparationServiceUrl + "/preparations/"+ preparationId + "/steps/" + stepId + "/order");
            if (StringUtils.isNotBlank(parentStepId)) {
                builder.addParameter("parentStepId", parentStepId);
            }
            return new HttpPost(builder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
    }
}
