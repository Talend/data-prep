//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import com.netflix.hystrix.HystrixCommand;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.command.common.ChainedCommand;
import org.talend.dataprep.exception.TDPException;

import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_DELETE_PREPARATION;

@Component
@Scope("request")
public class PreparationDelete extends ChainedCommand<String, Void> {

    private PreparationDelete(final String id, final HystrixCommand<Void> evictCacheOnPrep) {
        super(PREPARATION_GROUP, evictCacheOnPrep);
        execute(() -> onExecute(id));
        onError(e -> new TDPException(UNABLE_TO_DELETE_PREPARATION, e));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute(final String id) {
        getInput(); // evict cache on preparation
        return new HttpDelete(preparationServiceUrl + "/preparations/" + id); //$NON-NLS-1$
    }

}
