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

import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;

@Scope("prototype")
@Component
public class GetGroupExecutions extends GenericCommand<InputStream> {

    @Value("${execution.store.remote.url}")
    private String remoteRepositoryUrl;

    private String groupId;

    protected GetGroupExecutions(String groupId) {
        super(() -> "ASYNC");
        this.groupId = groupId;
        on(HttpStatus.OK).then(pipeStream());
    }

    @PostConstruct
    public void init() {
        execute(() -> new HttpGet(remoteRepositoryUrl + "/executions/groups/" + groupId));
    }
}
