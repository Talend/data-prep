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

package org.talend.dataprep.async.repository;

import org.talend.dataprep.async.AsyncExecution;
import org.talend.dataprep.async.AsyncExecutionWatcher;

@FunctionalInterface
public interface AsyncExecutionUpdaterRegistry {

    /**
     * Registers given <code>asyncExecutionWatcher</code> to watch <code>asyncExecution</code>.
     * @param asyncExecution The {@link AsyncExecution execution} to watch.
     * @param asyncExecutionWatcher The {@link AsyncExecutionWatcher watcher}.
     */
    void register(AsyncExecution asyncExecution, AsyncExecutionWatcher asyncExecutionWatcher);

}
