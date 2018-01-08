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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public interface Registration<T> {

    Class<T> getModelClass();

    <U> BiFunction<T, U, U> getCustom(Class<U> targetClass);

    List<Class<?>> getConvertedClasses();

    /**
     * Merge another {@link RegistrationImpl registration} (and merge same conversions into a single custom rule).
     *
     * @param other The other {@link RegistrationImpl registration} to merge with. Please note {@link RegistrationImpl#modelClass}
     * <b>MUST</b> be the same (in {@link #equals(Object)} sense).
     * @return The current registration with all custom merged from other's.
     */
    default Registration<T> merge(Registration<T> other) {

//            List<Class<?>> convClasses = new ArrayList();
//            convClasses.addAll(getConvertedClasses());
//            convClasses.addAll(other.getConvertedClasses());
//
//            HashMap<Class<?>, BiFunction<T, Object, Object>> customs = new HashMap<>();
//
//            new RegistrationImpl<>(getModelClass(), convClasses, customs);



        return new Registration<T>() {

            private ArrayList<Class<?>> convertedClasses = new ArrayList<>(Registration.this.getConvertedClasses());

            {
                convertedClasses.addAll(other.getConvertedClasses());
            }

            @Override
            public Class<T> getModelClass() {
                return Registration.this.getModelClass();
            }

            @Override
            public <U> BiFunction<T, U, U> getCustom(Class<U> targetClass) {
                BiFunction<T, U, U> firstCustom = Registration.this.getCustom(targetClass);
                BiFunction<T, U, U> otherCustom = other.getCustom(targetClass);
                BiFunction<T, U, U> merge;
                if (firstCustom != null && otherCustom != null) {
                    merge = (o, o2) -> {
                        final U initial = firstCustom.apply(o, o2);
                        return otherCustom.apply(o, initial);
                    };
                } else if (firstCustom != null) {
                    merge = firstCustom;
                } else {
                    merge = otherCustom;
                }
                return merge;
            }

            @Override
            public List<Class<?>> getConvertedClasses() {
                return convertedClasses;
            }
        };
    }
}
