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
package org.talend.dataprep.api.service.command.dataset;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.common.Defaults;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

/**
 * API command to execute the dataset favorite api
 *
 */
@Component
@Scope("request")
public class SetFavorite extends GenericCommand<String> {

    private SetFavorite(HttpClient client, String dataSetId, boolean unset) {
        super(PreparationAPI.DATASET_GROUP, client);
        execute(() -> new HttpPut(datasetServiceUrl + "/datasets/" + dataSetId + "/favorite?unset=" + unset));
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_SET_FAVORITE_DATASET, e,
                ExceptionContext.build().put("id", dataSetId)));
        on(HttpStatus.OK).then(Defaults.<String> asNull());
    }

}
