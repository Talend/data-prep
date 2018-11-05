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
import static org.talend.dataprep.command.Defaults.convertResponse;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_GET_PREPARATION_DETAILS;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_DOES_NOT_EXIST;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Command that returns the preparation actions.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class PreparationGetActions extends GenericCommand<List<Action>> {

    /**
     * Default constructor that retrieves the preparation actions at for the head.
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
        on(HttpStatus.NOT_FOUND).then((req, resp) -> {
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, ExceptionContext.withBuilder().put("id", preparationId).build());
        });
        onError(e -> new TDPException(UNABLE_TO_GET_PREPARATION_DETAILS, e));
    }

    @PostConstruct
    public void init() {
        on(HttpStatus.OK).then(convertResponse(objectMapper, new TypeReference<List<Action>>() {
        }));
    }

}
