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

import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Ease declaration of {@link org.springframework.beans.factory.config.BeanPostProcessor} when you want to target a
 * specific class.
 * </p>
 * <p>
 * Implementation should not use {@link org.springframework.beans.factory.annotation.Autowired} fields since this breaks
 * {@link org.springframework.beans.factory.config.BeanPostProcessor}.
 * </p>
 *
 * @param <T> The targeted class for this wrapper.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public interface Wrapper<T> {

    /**
     * @return The class of the object to watch for.
     */
    Class<T> wrapped();

    /**
     * Work on a being created object. Implementations of this {@link Wrapper} are only called for the <code>T</code>
     * instances.
     *
     * @param instance The instance to be wrapped.
     * @param beanName The instance bean name.
     * @param applicationContext An {@link ApplicationContext} to use to look up in context.
     * @return The modified bean or a bean that replaces the one passed as parameter.
     */
    T doWith(T instance, String beanName, ApplicationContext applicationContext);

}
