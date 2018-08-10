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

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.command.AsyncGenericCommand;
import org.talend.dataprep.api.service.command.common.ChainedAsyncCommand;

@Component
@Scope(SCOPE_PROTOTYPE)
public class SuggestAsyncActionParams extends ChainedAsyncCommand<InputStream, InputStream> {

    private SuggestAsyncActionParams(final AsyncGenericCommand<InputStream> content, final String action, final String columnId) {
        super(content);
        execute(() -> {
            final String uri = transformationServiceUrl + "/transform/suggest/" + action + "/params?columnId=" + columnId;
            final HttpPost getParametersCall = new HttpPost(uri);

            ResponseEntity<InputStream> repsonse = getInput();
            final InputStreamEntity entity = new InputStreamEntity(repsonse.getBody());
            getParametersCall.setEntity(entity);
            return getParametersCall;
        });
        on(HttpStatus.OK).then(pipeStream());
    }

}
