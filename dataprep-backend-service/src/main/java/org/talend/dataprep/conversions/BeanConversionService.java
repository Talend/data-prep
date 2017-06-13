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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Service;

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
                final Method readMethod = sourceProperty.getReadMethod();
                if (readMethod != null) {
                    final Type sourceReturnType = readMethod.getGenericReturnType();
                    final Method targetPropertyWriteMethod = targetProperty.getWriteMethod();
                    if (targetPropertyWriteMethod != null) {
                        final Type targetReturnType = targetPropertyWriteMethod.getParameters()[0].getParameterizedType();
                        boolean valid = Object.class.equals(targetPropertyType) ||
                                sourcePropertyType.equals(targetPropertyType) && sourceReturnType.equals(targetReturnType);
                        if (!valid) {
                            discardedProperties.add(sourceProperty.getName());
                        }
                    }
                } else {
                    discardedProperties.add(sourceProperty.getName());
                }
            }
        }

        // Perform copy
        BeanUtils.copyProperties(source, converted, discardedProperties.toArray(new String[discardedProperties.size()]));
    }

    public void register(Registration registration) {
        final Registration existingRegistration = registrations.get(registration.getModelClass());
        if (existingRegistration != null) {
            registrations.put(registration.getModelClass(), existingRegistration.merge(registration));
        } else {
            registrations.put(registration.getModelClass(), registration);
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
        return Objects.equals(aClass, aClass1) || has(aClass) && of(registrations.get(aClass))
                .anyMatch(registration -> registration.getConvertedClasses().stream().anyMatch(aClass1::equals));
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
            T convert = convert(source, aClass);
            return onTheFlyConvert.apply(source, convert);
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
                Class<? super T> currentClass = targetClass;
                while (currentClass != null) {
                    final BiFunction<Object, Object, Object> custom = registration.getCustom(currentClass);
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

    public interface Registration<T> {

        Class<T> getModelClass();

        BiFunction<Object, Object, Object> getCustom(Class<?> targetClass);

        List<Class<?>> getConvertedClasses();

        /**
         * Merge another {@link RegistrationImpl registration} (and merge same conversions into a single custom rule).
         *
         * @param other The other {@link RegistrationImpl registration} to merge with. Please note {@link RegistrationImpl#modelClass}
         * <b>MUST</b> be the same (in {@link #equals(Object)} sense).
         * @return The current registration with all custom merged from other's.
         */
        default Registration<T> merge(Registration<T> other) {

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
                public BiFunction<Object, Object, Object> getCustom(Class<?> targetClass) {
                    BiFunction<Object, Object, Object> firstCustom = Registration.this.getCustom(targetClass);
                    BiFunction<Object, Object, Object> otherCustom = other.getCustom(targetClass);
                    BiFunction<Object, Object, Object> merge;
                    if (firstCustom != null && otherCustom != null) {
                        merge = (o, o2) -> {
                            final Object initial = firstCustom.apply(o, o2);
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

    private static class RegistrationImpl<T> implements Registration<T> {

        private final Class<T> modelClass;

        private final Map<Class<?>, BiFunction<Object, Object, Object>> customs;

        private List<Class<?>> convertedClasses;

        private RegistrationImpl(Class<T> modelClass, Class<?>[] convertedClasses,
                                 Map<Class<?>, BiFunction<Object, Object, Object>> customs) {
            this.modelClass = modelClass;
            this.convertedClasses = Arrays.asList(convertedClasses);
            this.customs = customs;
        }

        @Override
        public Class<T> getModelClass() {
            return modelClass;
        }

        @Override
        public BiFunction<Object, Object, Object> getCustom(Class<?> targetClass) {
            return customs.get(targetClass);
        }

        @Override
        public List<Class<?>> getConvertedClasses() {
            return convertedClasses;
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
            return new RegistrationImpl<>(source, destinations.toArray(new Class<?>[destinations.size()]), customs);
        }

    }

}
