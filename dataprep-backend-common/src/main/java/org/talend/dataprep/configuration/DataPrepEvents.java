// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.talend.dataprep.event.DataPrepEventsCaster;

/**
 * Setup the events management in data prep.
 */
@SuppressWarnings("InsufficientBranchCoverage")
@Configuration
public class DataPrepEvents {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * @return The default application context ApplicationEventMulticaster.
     */
    // do NOT change the name as it is important to replace the default application context event multi caster
    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster getDataPrepEventsCaster() {
        return new DataPrepEventsCaster(dataPrepAsyncTaskExecutor(), beanFactory);
    }

    /**
     * @return an Authenticated task executor ready to run.
     */
    private AsyncListenableTaskExecutor dataPrepAsyncTaskExecutor() {
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(2);
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(false);
        taskExecutor.initialize();

        // wrap this task executor into a friendly authenticated one
        return AuthenticatedTaskExecutor.authenticated(taskExecutor);
    }
}
