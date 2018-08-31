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

package org.talend.dataprep.transformation.async;

import static org.talend.dataprep.command.CommandHelper.toStream;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.async.repository.ManagedTaskRepository;
import org.talend.dataprep.transformation.async.command.*;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Remote implementation of the ManagedTaskRepository implementation.
 */
@Component
@ConditionalOnProperty(name = "execution.store", havingValue = "remote")
public class RemoteTaskRepository implements ManagedTaskRepository {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public AsyncExecution get(String id) {
        return applicationContext.getBean(GetAsyncExecution.class, id).execute();
    }

    @Override
    public Stream<AsyncExecution> list(final String groupId) {
        return toStream(AsyncExecution.class, mapper, applicationContext.getBean(GetGroupExecutions.class, groupId));
    }

    @Override
    public Stream<AsyncExecution> list() {
        return toStream(AsyncExecution.class, mapper, applicationContext.getBean(ListAllExecutions.class));
    }

    @Override
    public void remove(String id) {
        applicationContext.getBean(RemoveAsyncExecution.class, id).execute();
    }

    @Override
    public void save(AsyncExecution asyncExecution) {
        applicationContext.getBean(SaveAsyncExecution.class, asyncExecution).execute();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

}
