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

package org.talend.dataprep.api.service.command.dataset;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.talend.dataprep.command.Defaults.asNull;

import java.io.InputStream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

@Component
@Scope("request")
public class UpdateColumn extends GenericCommand<Void> {

    private UpdateColumn(final String dataSetId, final String columnId, final InputStream body) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> {
            final HttpPost post = new HttpPost(datasetServiceUrl + "/datasets/" + dataSetId + "/column/" + columnId); //$NON-NLS-1$ //$NON-NLS-2$
            post.setHeader("Content-Type", APPLICATION_JSON_VALUE);
            post.setEntity(new InputStreamEntity(body));
            return post;
        });
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_CREATE_OR_UPDATE_DATASET, e,
                ExceptionContext.build().put("id", dataSetId)));
        on(HttpStatus.OK).then(asNull());
    }

}
