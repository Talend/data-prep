// ============================================================================
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

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.talend.dataprep.processor.Wrapper;

/**
 * Configuration for non blocking HTTP handling.
 */
@Configuration
@EnableAsync
public class Async {

    @Bean
    public AsyncExecutionConfiguration requestMappingHandlerMappingPostProcessor() {
        return new AsyncExecutionConfiguration();
    }

    private static class AsyncExecutionConfiguration implements Wrapper<RequestMappingHandlerAdapter> {

        @Override
        public Class<RequestMappingHandlerAdapter> wrapped() {
            return RequestMappingHandlerAdapter.class;
        }

        @Override
        public RequestMappingHandlerAdapter doWith(RequestMappingHandlerAdapter handlerAdapter, String beanName, ApplicationContext applicationContext) {
            SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
            // Set async thread pool
            final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
            threadPoolTaskExecutor.setQueueCapacity(50);
            threadPoolTaskExecutor.setMaxPoolSize(50);
            threadPoolTaskExecutor.initialize();
            asyncTaskExecutor.setThreadFactory(threadPoolTaskExecutor);
            // Add authentication
            final AsyncListenableTaskExecutor authenticated = AuthenticatedTaskExecutor.authenticated(asyncTaskExecutor);
            handlerAdapter.setTaskExecutor(authenticated);
            return handlerAdapter;
        }
    }

}
