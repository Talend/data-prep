/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.conversions;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

class RegistrationImpl<T> implements Registration<T> {

    private final Class<T> modelClass;

    private final Map<Class<?>, BiFunction<T, Object, Object>> customs;

    private List<Class<?>> convertedClasses;

    RegistrationImpl(Class<T> modelClass, List<Class<?>> convertedClasses, Map<Class<?>, BiFunction<T, Object, Object>> customs) {
        this.modelClass = modelClass;
        this.convertedClasses = convertedClasses;
        this.customs = customs;
    }

    @Override
    public Class<T> getModelClass() {
        return modelClass;
    }

    @Override
    public <U> BiFunction<T, U, U> getCustom(Class<U> targetClass) {
        return (BiFunction<T, U, U>) customs.get(targetClass);
    }

    @Override
    public List<Class<?>> getConvertedClasses() {
        return convertedClasses;
    }

}
