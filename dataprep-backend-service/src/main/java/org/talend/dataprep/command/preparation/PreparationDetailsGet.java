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
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

/**
 * Command that retrieves preparation details (NOT the content !)
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class PreparationDetailsGet extends GenericCommand<InputStream> {

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
        on(HttpStatus.NO_CONTENT).then(emptyStream());
        on(HttpStatus.OK).then(pipeStream());
        onError(e -> new TDPException(UNABLE_TO_READ_PREPARATION, e, build().put("id", preparationId)));
    }

    private HttpGet onExecute(String preparationId, String stepId) {
        String uri = preparationServiceUrl + "/preparations/" + preparationId + "/details";
        if (StringUtils.isNotBlank(stepId)) {
            uri += "?stepId=" + stepId;
        }
        return new HttpGet(uri);
    }
}
