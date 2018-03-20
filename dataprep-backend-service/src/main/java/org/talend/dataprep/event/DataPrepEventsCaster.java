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

package org.talend.dataprep.event;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.core.task.TaskExecutor;

/**
 * <p>
 * An {@link org.springframework.context.event.ApplicationEventMulticaster ApplicationEventMulticaster} for the DataSet
 * component.
 * </p>
 * <p>
 * It wraps both a synchronous and an asynchronous ApplicationEventMulticaster.
 * </p>
 */
@SuppressWarnings("InsufficientBranchCoverage")
public class DataPrepEventsCaster extends SimpleApplicationEventMulticaster {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(DataPrepEventsCaster.class);

    /**
     * Default constructor.
     *
     * @param taskExecutor the task executor for asynchronous event managing.
     * @param beanFactory the spring bean factory.
     */
    public DataPrepEventsCaster(TaskExecutor taskExecutor, BeanFactory beanFactory) {
        super(beanFactory);
        setTaskExecutor(taskExecutor);
    }

    @Override
    public void multicastEvent(final ApplicationEvent event, ResolvableType eventType) {
        ResolvableType type = (eventType != null ? eventType : ResolvableType.forInstance(event));

        for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
            if (AsyncApplicationListener.class.isAssignableFrom(listener.getClass())) {
                LOGGER.trace("processing {} asynchronously to {}", event, listener);
                getTaskExecutor().execute(() -> invokeListener(listener, event));
            } else {
                LOGGER.trace("processing {} synchronously to {}", event, listener);
                invokeListener(listener, event);
            }
        }
    }

}
