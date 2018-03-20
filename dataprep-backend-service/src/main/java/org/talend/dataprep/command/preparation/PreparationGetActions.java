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

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.talend.dataprep.command.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_GET_PREPARATION_DETAILS;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_DOES_NOT_EXIST;

import java.io.InputStream;

import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

/**
 * Command that returns the preparation actions.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class PreparationGetActions extends GenericCommand<InputStream> {

    /**
     * Default constructor that retrieves the prepration actions at for the head.
     *
     * @param preparationId preparation id to list the actions from.
     */
    // private constructor to ensure the IoC
    private PreparationGetActions(String preparationId) {
        this(preparationId, "head");
    }

    /**
     * Default constructor.
     *
     * @param preparationId preparation id to list the actions from.
     * @param stepId the step id for the wanted preparation.
     */
    private PreparationGetActions(String preparationId, String stepId) {
        super(PREPARATION_GROUP);
        execute(() -> new HttpGet(preparationServiceUrl + "/preparations/" + preparationId + "/actions/" + stepId));
        on(HttpStatus.OK).then(pipeStream());
        on(HttpStatus.NOT_FOUND).then((req, resp) -> {
            throw new TDPException(PREPARATION_DOES_NOT_EXIST);
        });
        onError(e -> new TDPException(UNABLE_TO_GET_PREPARATION_DETAILS, e));
    }

}
