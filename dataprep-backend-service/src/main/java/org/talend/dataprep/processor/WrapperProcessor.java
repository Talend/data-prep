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

package org.talend.dataprep.processor;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * A {@link BeanPostProcessor} that processes {@link Wrapper wrappers}.
 *
 * @see BeanPostProcessor#postProcessAfterInitialization(Object, String)
 */
@Component
public class WrapperProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        final Map<String, Wrapper> wrappers = applicationContext.getBeansOfType(Wrapper.class);
        Object current = bean;
        for (Wrapper wrapper : wrappers.values()) {
            if (wrapper.wrapped().isAssignableFrom(bean.getClass())) {
                current = wrapper.doWith(bean, beanName, applicationContext);
            }
        }
        return current;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
