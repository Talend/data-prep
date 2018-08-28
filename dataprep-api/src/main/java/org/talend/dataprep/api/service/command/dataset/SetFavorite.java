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
package org.talend.dataprep.api.service.command.dataset;

import org.apache.http.client.methods.HttpPut;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * API command to execute the dataset favorite api
 *
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class SetFavorite extends GenericCommand<String> {

    private SetFavorite(String dataSetId, boolean unset) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> new HttpPut(datasetServiceUrl + "/datasets/" + dataSetId + "/favorite?unset=" + unset));
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_SET_FAVORITE_DATASET, e,
                ExceptionContext.build().put("id", dataSetId)));
        on(HttpStatus.OK).then(Defaults.<String> asNull());
    }

}
