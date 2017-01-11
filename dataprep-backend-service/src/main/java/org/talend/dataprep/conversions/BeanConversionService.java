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

package org.talend.dataprep.conversions;

import static java.util.stream.Stream.of;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * This service provides methods to convert beans to other beans (DTOs, transient beans...). This service helps code to
 * separate between core business code and representations for various use cases.
 */
@Service
public class BeanConversionService implements ConversionService {

    private final Map<Class<?>, Registration> registrations = new HashMap<>();

    /**
     * The {@link BeanUtils#copyProperties(java.lang.Object, java.lang.Object)} method does <b>NOT</b> check if parametrized type
     * are compatible when copying values, this helper method performs this additional check and ignore copy of those values.
     *
     * @param source The source bean (from which values are read).
     * @param converted The target bean (to which values are written).
     */
    private static void copyBean(Object source, Object converted) {
        // Find property(ies) to ignore during copy.
        List<String> discardedProperties = new LinkedList<>();
        final BeanWrapper sourceBean = new BeanWrapperImpl(source);
        final BeanWrapper targetBean = new BeanWrapperImpl(converted);
        final PropertyDescriptor[] sourceProperties = sourceBean.getPropertyDescriptors();
        for (PropertyDescriptor sourceProperty : sourceProperties) {
            if (targetBean.isWritableProperty(sourceProperty.getName())) {
                final PropertyDescriptor targetProperty = targetBean.getPropertyDescriptor(sourceProperty.getName());
                final Class<?> sourcePropertyType = sourceProperty.getPropertyType();
                final Class<?> targetPropertyType = targetProperty.getPropertyType();
                final Type sourceReturnType = sourceProperty.getReadMethod().getGenericReturnType();
                final Method targetPropertyWriteMethod = targetProperty.getWriteMethod();
                if (targetPropertyWriteMethod != null) {
                    final Type targetReturnType = targetPropertyWriteMethod.getParameters()[0].getParameterizedType();
                    boolean valid = sourcePropertyType.equals(targetPropertyType) && sourceReturnType.equals(targetReturnType);
                    if (!valid) {
                        discardedProperties.add(sourceProperty.getName());
                    }
                }
            }
        }

        // Perform copy
        BeanUtils.copyProperties(source, converted, discardedProperties.toArray(new String[discardedProperties.size()]));
    }

    public void register(Registration registration) {
        final Registration existingRegistration = registrations.get(registration.modelClass);
        if (existingRegistration != null) {
            registrations.put(registration.modelClass, existingRegistration.merge(registration));
        } else {
            registrations.put(registration.modelClass, registration);
        }
    }

    public boolean has(Class<?> modelClass) {
        return registrations.containsKey(modelClass);
    }

    public void clear() {
        registrations.clear();
    }

    @Override
    public boolean canConvert(TypeDescriptor typeDescriptor, TypeDescriptor typeDescriptor1) {
        return canConvert(typeDescriptor.getType(), typeDescriptor1.getType());
    }

    @Override
    public boolean canConvert(Class<?> aClass, Class<?> aClass1) {
        return ObjectUtils.nullSafeEquals(aClass, aClass1) || has(aClass) && of(registrations.get(aClass))
                .anyMatch(registration -> of(registration.convertedClasses).anyMatch(aClass1::equals));
    }

    /**
     * Similar {@link #convert(Object, Class)} but allow user to specify a constant conversion that overrides previously defined
     * conversions.
     *
     * @param source The bean to convert.
     * @param aClass The target class for conversion.
     * @param onTheFlyConvert The function to apply on the transformed bean.
     * @param <U> The source type.
     * @param <T> The target type.
     * @return The converted bean (typed as <code>T</code>).
     */
    public <U, T> T convert(U source, Class<T> aClass, BiFunction<U, T, T> onTheFlyConvert) {
        try {
            T converted = aClass.newInstance();
            BeanUtils.copyProperties(source, converted);
            return onTheFlyConvert.apply(source, converted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T converted = targetClass.newInstance();
            copyBean(source, converted);

            // Find registration
            Registration<T> registration = null;
            Class<?> currentSourceClass = source.getClass();
            while (registration == null && !Object.class.equals(currentSourceClass)) {
                registration = registrations.get(currentSourceClass);
                currentSourceClass = currentSourceClass.getSuperclass();
            }

            // Use registration
            if (registration != null) {
                List<BiFunction<Object, Object, Object>> customs = new ArrayList<>();
                Class currentClass = targetClass;
                while (currentClass != null) {
                    final BiFunction<Object, Object, Object> custom = registration.customs.get(currentClass);
                    if (custom != null) {
                        customs.add(custom);
                    }
                    currentClass = currentClass.getSuperclass();
                }

                T result = converted;
                for (BiFunction<Object, Object, Object> current : customs) {
                    result = (T) current.apply(source, converted);
                }
                return result;
            } else {
                return converted;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object convert(Object o, TypeDescriptor typeDescriptor, TypeDescriptor typeDescriptor1) {
        return convert(o, typeDescriptor1.getObjectType());
    }

    private static class Registration<T> {

        private final Class<T> modelClass;

        private final Map<Class<?>, BiFunction<Object, Object, Object>> customs;

        private Class<?>[] convertedClasses;

        private Registration(Class<T> modelClass, Class<?>[] convertedClasses,
                Map<Class<?>, BiFunction<Object, Object, Object>> customs) {
            this.modelClass = modelClass;
            this.convertedClasses = convertedClasses;
            this.customs = customs;
        }

        /**
         * Merge another {@link Registration registration} (and merge same conversions into a single custom rule).
         *
         * @param other The other {@link Registration registration} to merge with. Please note {@link Registration#modelClass}
         * <b>MUST</b> be the same (in {@link #equals(Object)} sense).
         * @return The current registration with all custom merged from other's.
         */
        public Registration<T> merge(Registration<T> other) {
            if (!other.modelClass.equals(this.modelClass)) {
                throw new IllegalArgumentException("Cannot merge incompatible model registration (" + other.modelClass
                        + " is not equal to " + this.modelClass + ")");
            }
            this.convertedClasses = Stream.of(ArrayUtils.addAll(this.convertedClasses, other.convertedClasses)) //
                    .collect(Collectors.toSet()) //
                    .toArray(new Class<?>[0]);
            for (Map.Entry<Class<?>, BiFunction<Object, Object, Object>> entry : other.customs.entrySet()) {
                if (customs.containsKey(entry.getKey())) {
                    // Group custom rule from other registration with existing (can't use andThen() due to type issue).
                    final BiFunction<Object, Object, Object> otherCustom = entry.getValue();
                    final BiFunction<Object, Object, Object> currentCustom = customs.get(entry.getKey());
                    final BiFunction<Object, Object, Object> mergedCustom = (o, o2) -> {
                        final Object initial = currentCustom.apply(o, o2);
                        return otherCustom.apply(o, initial);
                    };
                    this.customs.put(entry.getKey(), mergedCustom);
                } else {
                    this.customs.put(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }
    }

    public static class RegistrationBuilder<T> {

        private final List<Class<?>> destinations = new ArrayList<>();

        private final Class<T> source;

        private final Map<Class<?>, BiFunction<Object, Object, Object>> customs = new HashMap<>();

        private RegistrationBuilder(Class<T> source) {
            this.source = source;
        }

        public static <T> RegistrationBuilder<T> fromBean(Class<T> source) {
            return new RegistrationBuilder<>(source);
        }

        public RegistrationBuilder<T> toBeans(Class<?>... destinations) {
            Collections.addAll(this.destinations, destinations);
            return this;
        }

        public <U> RegistrationBuilder<T> using(Class<U> destination, BiFunction<T, U, U> custom) {
            customs.put(destination, (BiFunction<Object, Object, Object>) custom);
            return this;
        }

        public Registration<T> build() {
            return new Registration<>(source, destinations.toArray(new Class<?>[destinations.size()]), customs);
        }

    }

}
