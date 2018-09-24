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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.async.repository.ManagedTaskRepository;

/**
 * In-Memory implementation for the ManagedTaskRepository (only for tests)
 */
@Component
@ConditionalOnProperty(name = "execution.store", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryManagedTaskRepository implements ManagedTaskRepository {

    private Map<String, AsyncExecution> executions = new ConcurrentHashMap<>();

    @Override
    public AsyncExecution get(String id) {
        return executions.get(id);
    }

    @Override
    public Stream<AsyncExecution> list(String groupId) {
        return executions.values().stream().filter((task) -> StringUtils.equals(task.getGroup(), groupId));
    }

    @Override
    public Stream<AsyncExecution> list() {
        return executions.values().stream();
    }

    @Override
    public void remove(String id) {
        executions.remove(id);
    }

    @Override
    public void save(AsyncExecution asyncExecution) {
        executions.put(asyncExecution.getId(), asyncExecution);
    }

    @Override
    public void clear() {
        executions.clear();
    }

}
