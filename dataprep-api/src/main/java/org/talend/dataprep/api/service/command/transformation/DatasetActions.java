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

package org.talend.dataprep.api.service.command.transformation;

import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

/**
 * Return all actions that can be performed on the whole dataset.
 */
@Component
@Scope("request")
public class DatasetActions extends GenericCommand<InputStream> {

    /**
     * Constructor.
     */
    private DatasetActions() {
        super(TRANSFORM_GROUP);
        execute(() -> new HttpGet(transformationServiceUrl + "/actions/dataset"));
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_SUGGESTED_ACTIONS, e));
        on(OK).then(pipeStream());
    }

}
