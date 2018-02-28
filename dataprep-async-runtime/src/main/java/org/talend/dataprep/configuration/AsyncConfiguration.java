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

package org.talend.dataprep.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Async aspect configuration.
 */
@Configuration
public class AsyncConfiguration {

    @Value("${async.operation.concurrent.run:5}")
    private int maxConcurrentRuns;

    /**
     * @return the 'engine' (thread pool) that runs behind the ManagedTaskExecutor.
     */
    @Bean(name = "managedTaskEngine")
    public AsyncListenableTaskExecutor getManagedTaskExecutorEngine() {
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(maxConcurrentRuns);
        threadPoolTaskExecutor.setMaxPoolSize(maxConcurrentRuns);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(false);
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }
}
