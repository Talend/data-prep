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

package org.talend.dataprep.async;

import org.springframework.context.ApplicationContext;

@FunctionalInterface
public interface AsyncExecutionWatcher {

    /**
     * Called by {@link org.talend.dataprep.async.repository.AsyncExecutionUpdaterRegistry} when its checks the
     * {@link AsyncExecution executions} it is asked to
     * watch.
     * 
     * @param asyncExecution The registered {@link AsyncExecution execution}
     * @param context {@link ApplicationContext} to be used for command lookup for example.
     * @return <code>null</code> if execution did not change, a non-null value will be saved by
     * {@link AsyncExecutionWatcher}.
     * @see org.talend.dataprep.async.repository.AsyncExecutionUpdaterRegistry#register(AsyncExecution,
     * AsyncExecutionWatcher)
     *
     */
    AsyncExecution watch(AsyncExecution asyncExecution, ApplicationContext context);
}
