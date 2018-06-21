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

package org.talend.dataprep.api.service.command.info;

import static org.talend.dataprep.command.Defaults.asNull;

import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.info.Version;

import com.netflix.hystrix.HystrixCommandGroupKey;

@Component
@Scope("prototype")
public class VersionCommand extends GenericCommand<Version> {

    public static final HystrixCommandGroupKey VERSION_GROUP = HystrixCommandGroupKey.Factory.asKey("version");

    private VersionCommand(String serviceUrl, String entryPoint) {
        super(VERSION_GROUP);

        execute(() -> {
            String url = serviceUrl + entryPoint;
            return new HttpGet(url);
        });
        onError(e -> new TDPException(CommonErrorCodes.UNABLE_TO_GET_SERVICE_VERSION, e,
                ExceptionContext.build().put("version", serviceUrl)));
        on(HttpStatus.NO_CONTENT).then(asNull());
    }

    @PostConstruct
    public void init() {
        on(HttpStatus.OK).then(Defaults.convertResponse(objectMapper, Version.class));
    }

}
