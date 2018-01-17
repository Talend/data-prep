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

package org.talend.dataprep.api.service.command.transformation;

import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.command.common.ChainedCommand;

import com.netflix.hystrix.HystrixCommand;

@Component
@Scope("request")
public class SuggestActionParams extends ChainedCommand<InputStream, InputStream> {

    private SuggestActionParams(final HystrixCommand<InputStream> content, final String action,
            final String columnId) {
        super(content);
        execute(() -> {
            final String uri = transformationServiceUrl + "/transform/suggest/" + action + "/params?columnId=" + columnId;
            final HttpPost getParametersCall = new HttpPost(uri);
            final InputStreamEntity entity = new InputStreamEntity(getInput());
            getParametersCall.setEntity(entity);
            return getParametersCall;
        });
        on(HttpStatus.OK).then(pipeStream());
    }

}
