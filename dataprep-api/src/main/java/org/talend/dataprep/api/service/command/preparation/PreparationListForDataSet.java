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

import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

/**
 * Command used to retrieve the preparations used by a given dataset.
 */
@Component
@Scope("request")
public class PreparationListForDataSet extends GenericCommand<InputStream> {

    /**
     * Private constructor.
     *
     * @param dataSetId the wanted dataset id.
     */
    private PreparationListForDataSet(String dataSetId) {
        super(GenericCommand.PREPARATION_GROUP);
        execute(() -> new HttpGet(preparationServiceUrl + "/preparations/search?dataSetId=" + dataSetId));
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_PREPARATION_LIST, e));
        on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(emptyStream());
        on(HttpStatus.OK).then(pipeStream());
    }

}
