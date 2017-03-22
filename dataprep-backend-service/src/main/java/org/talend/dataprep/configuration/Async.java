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

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

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

    /**
     * <h1>{@link BeanPostProcessor} notice</h1>
     * Don't use any {@link org.springframework.beans.factory.annotation.Autowired} in the
     * configuration as it will prevent autowired beans to be processed by BeanPostProcessor.
     */
    private static class AsyncExecutionConfiguration implements BeanPostProcessor {

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            if (bean instanceof RequestMappingHandlerAdapter) {
                final RequestMappingHandlerAdapter handlerAdapter = (RequestMappingHandlerAdapter) bean;
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
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            return bean;
        }

    }

}
