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

import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpDelete;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.command.GenericCommand;

@Scope("prototype")
@Component
public class RemoveAsyncExecution extends GenericCommand<AsyncExecution> {

    @Value("${execution.store.remote.url}")
    private String remoteRepositoryUrl;

    private String executionId;

    protected RemoveAsyncExecution(String executionId) {
        super(() -> "ASYNC");
        this.executionId = executionId;
        on(HttpStatus.OK).then(asNull());
    }

    @PostConstruct
    public void init() {
        execute(() -> new HttpDelete(remoteRepositoryUrl + "/executions/" + executionId));
    }
}
