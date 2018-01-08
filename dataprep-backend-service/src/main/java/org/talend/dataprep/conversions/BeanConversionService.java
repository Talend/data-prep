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

package org.talend.dataprep.conversions;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;

import static java.util.stream.Stream.of;

/**
 * This service provides methods to convert beans to other beans (DTOs, transient beans...). This service helps code to
 * separate between core business code and representations for various use cases.
 */
@Service
public class BeanConversionService implements ConversionService {

    private final Map<Class<?>, Registration<Object>> registrations = new HashMap<>();

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

    public static <T> RegistrationBuilder<T> fromBean(Class<T> source) {
        return new RegistrationBuilder<>(source);
    }

    public  void register(Registration<?> registration) {
        registrations.merge(registration.getModelClass(), (Registration<Object>) registration, Registration::merge);
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
    public <U> U convert(Object source, Class<U> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            U converted = targetClass.newInstance();
            copyBean(source, converted);

            List<Registration<Object>> registrationsFound = getRegistrationsForSourceClass(source.getClass());

            List<BiFunction<Object, U, U>> customs = new ArrayList<>();
            for (Registration<Object> registrationFound : registrationsFound) {
                customs.addAll(getRegistrationFunctions(targetClass, registrationFound));
            }

            U result = converted;
            for (BiFunction<Object, U, U> current : customs) {
                result = current.apply(source, converted);
            }

            return result;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /** Find all registrations that can convert this type of source. */
    private List<Registration<Object>> getRegistrationsForSourceClass(Class<?> currentSourceClass) {
        List<Registration<Object>> registrationsFound = new ArrayList<>();
        for (Map.Entry<Class<?>, Registration<Object>> availableRegistration : registrations.entrySet()) {
            if (availableRegistration.getKey().isAssignableFrom(currentSourceClass)) {
                registrationsFound.add(availableRegistration.getValue());
            }
        }
        return registrationsFound;
    }

    /** Get all available transformations in this registration. */
    private <T, U> List<BiFunction<T, U, U>> getRegistrationFunctions(Class<U> targetClass,
                                                                                  Registration<T> registration) {
        List<BiFunction<T, U, U>> customs = new ArrayList<>();
        Class<U> currentClass = targetClass;
        while (currentClass != null) {
            final BiFunction<T, U, U> custom = registration.getCustom(currentClass);
            if (custom != null) {
                customs.add(custom);
            }
            currentClass = (Class<U>) currentClass.getSuperclass();
        }
        return customs;
    }

    @Override
    public Object convert(Object o, TypeDescriptor typeDescriptor, TypeDescriptor typeDescriptor1) {
        return convert(o, typeDescriptor1.getObjectType());
    }

}
