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

package org.talend.dataprep.command.preparation;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.command.Defaults.convertResponse;

import java.net.URISyntaxException;

import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.PreparationDTO;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

/**
 * Command that retrieves preparation details (NOT the content !)
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class PreparationDetailsGet extends GenericCommand<PreparationDTO> {

    /**
     * Constructor.
     *
     * @param preparationId the requested preparation id.
     */
    public PreparationDetailsGet(String preparationId) {
        this(preparationId, null);
    }

    /**
     * Constructor.
     *
     * @param preparationId the requested preparation id.
     * @param stepId the wanted step id
     */
    public PreparationDetailsGet(String preparationId, String stepId) {
        super(PREPARATION_GROUP);
        execute(() -> onExecute(preparationId, stepId));
        on(HttpStatus.NO_CONTENT).then(asNull());
        onError(Defaults.passthrough());
    }

    @PostConstruct
    public void init() {
        on(HttpStatus.OK).then(convertResponse(objectMapper, PreparationDTO.class));
    }

    private HttpGet onExecute(String preparationId, String stepId) {
        try {
            final URIBuilder uriBuilder = new URIBuilder(preparationServiceUrl);
            uriBuilder.setPath(uriBuilder.getPath() + "/preparations/" + preparationId + "/details");
            uriBuilder.addParameter("stepId", stepId);
            return new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
