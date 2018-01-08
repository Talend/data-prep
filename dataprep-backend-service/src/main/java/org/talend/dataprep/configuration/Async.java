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

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.talend.dataprep.processor.Wrapper;

/**
 * Configuration for non blocking HTTP handling.
 */
@Configuration
@EnableAsync
public class Async implements Wrapper<RequestMappingHandlerAdapter> {

    /**
     * Constant for the {@link org.springframework.core.task.TaskExecutor} to be picked up by Spring (based on naming
     * conventions).
     */
    static final String EXECUTOR = "taskExecutor";

    @Override
    public Class<RequestMappingHandlerAdapter> wrapped() {
        return RequestMappingHandlerAdapter.class;
    }

    @Override
    public RequestMappingHandlerAdapter doWith(RequestMappingHandlerAdapter handlerAdapter, String beanName,
            ApplicationContext applicationContext) {
        final AsyncTaskExecutor executor = (AsyncTaskExecutor) applicationContext.getBean(EXECUTOR);
        handlerAdapter.setTaskExecutor(executor);
        return handlerAdapter;
    }

}
