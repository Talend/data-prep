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
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.command.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.PREPARATION_DOES_NOT_EXIST;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

import java.io.InputStream;

import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;

/**
 * Command that retrieves the non consolidated (actions are not retrieved) preparation (NOT the content!)
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class PreparationGet extends GenericCommand<InputStream> {

    /**
     * Constructor.
     *
     * @param preparationId the requested preparation id.
     */
    public PreparationGet(String preparationId) {
        super(PREPARATION_GROUP);
        execute(() -> new HttpGet(preparationServiceUrl + "/preparations/" + preparationId));
        on(HttpStatus.NOT_FOUND).then((req, resp) -> {
            throw new TDPException(PREPARATION_DOES_NOT_EXIST, build().put("id", preparationId));
        });
        on(HttpStatus.OK).then(pipeStream());
        onError(e -> new TDPException(UNABLE_TO_READ_PREPARATION, e, build().put("id", preparationId)));
    }
}
