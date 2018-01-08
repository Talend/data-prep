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

package org.talend.dataprep.processor;

import static java.util.stream.Collectors.joining;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link BeanPostProcessor} that processes {@link Wrapper wrappers}.
 *
 * @see BeanPostProcessor#postProcessAfterInitialization(Object, String)
 */
@Component
public class WrapperProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * Tests if a {@link Wrapper wrapper} has {@link Autowired} fields or not. Wrappers are not allowed to have
     * autowired fields.
     *
     * @param wrapper
     * @return <code>true</code> if wrapper is valid, <code>false</code> otherwise.
     */
    static boolean isValidWrapper(Wrapper wrapper) {
        if (wrapper == null) {
            return true;
        }
        AtomicBoolean isValid = new AtomicBoolean(true);
        ReflectionUtils.doWithFields(wrapper.getClass(), field -> {
            final Autowired annotation = AnnotationUtils.findAnnotation(field, Autowired.class);
            if (annotation != null) {
                isValid.set(false);
            }
        });
        return isValid.get();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        final Map<String, Wrapper> wrappers = applicationContext.getBeansOfType(Wrapper.class);
        Object current = bean;

        final Set<Wrapper> invalidWrappers = new HashSet<>();
        for (Wrapper wrapper : wrappers.values()) {
            // A little check on Wrapper (@Autowired fields are forbidden)
            if (!isValidWrapper(wrapper)) {
                invalidWrappers.add(wrapper);
            }

            // Invoke Wrapper
            if (wrapper.wrapped().isAssignableFrom(bean.getClass())) {
                current = wrapper.doWith(current, beanName, applicationContext);
            }
        }

        // Throw typed Exception for correct startup failure analysis
        if (!invalidWrappers.isEmpty()) {
            throw new WrapperProcessor.InvalidWrapperDefinitionException(invalidWrappers);
        }

        return current;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private class InvalidWrapperDefinitionException extends BeansException {

        private InvalidWrapperDefinitionException(Set<Wrapper> invalidWrappers) {
            super("Invalid Wrapper definition(s) found in context for: '" + invalidWrappers.stream() //
                    .map(wrapper -> wrapper.getClass().getName()) //
                    .collect(joining(",")) + "'.");
        }
    }
}
