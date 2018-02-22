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

package org.talend.dataprep.transformation.async.command;

import static org.talend.dataprep.command.Defaults.asNull;

import java.io.IOException;
import java.io.StringWriter;

import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.databind.ObjectMapper;

@Scope("prototype")
@Component
public class SaveAsyncExecution extends GenericCommand<AsyncExecution> {

    @Value("${execution.store.remote.url}")
    private String remoteRepositoryUrl;

    @Autowired
    private ObjectMapper mapper;

    private final AsyncExecution execution;

    protected SaveAsyncExecution(AsyncExecution execution) {
        super(() -> "ASYNC");
        this.execution = execution;
        on(HttpStatus.OK).then(asNull());
    }

    @PostConstruct
    public void init() {
        execute(() -> {
            try {
                final HttpPost post = new HttpPost(remoteRepositoryUrl + "/executions");
                post.setHeader(new BasicHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE));
                final StringWriter executionAsString = new StringWriter();
                mapper.writerFor(AsyncExecution.class).writeValue(executionAsString, execution);
                post.setEntity(new StringEntity(executionAsString.toString()));

                return post;
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        });
    }
}
