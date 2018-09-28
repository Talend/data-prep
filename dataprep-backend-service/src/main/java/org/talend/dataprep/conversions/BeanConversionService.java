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

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Stream.of;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Service;

/**
 * This service provides methods to convert beans to other beans (DTOs, transient beans...). This service helps code to
 * separate between core business code and representations for various use cases.
 */
@Service
public class BeanConversionService implements ConversionService {

    private final Map<Class<?>, Registration<Object>> registrations = new HashMap<>();

    @Autowired(required = false)
    private Tracer tracer;

    /**
     * The {@link BeanUtils#copyProperties(java.lang.Object, java.lang.Object)} method does <b>NOT</b> check if parametrized type
     * are compatible when copying values, this helper method performs this additional check and ignore copy of those values.
     *
     * @param source The source bean (from which values are read).
     * @param converted The target bean (to which values are written).
     */
    private static void copyBean(Object source, Object converted) {
        // Find property(ies) to ignore during copy.
        final List<String> discardedProperties = getDiscardedProperties(source, converted);

        // Perform copy
        BeanUtils.copyProperties(source, converted, discardedProperties.toArray(new String[0]));
    }

    private static final Map<String, List<String>> discardedPropertiesCache = new HashMap<>();

    private static List<String> getDiscardedProperties(Object source, Object converted) {
        final String cacheKey = source.getClass().getName() + " to " + converted.getClass().getName();
        if (discardedPropertiesCache.containsKey(cacheKey)) {
            return discardedPropertiesCache.get(cacheKey);
        }

        final List<String> discardedProperties = new LinkedList<>();
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
                        final Type targetReturnType =
                                targetPropertyWriteMethod.getParameters()[0].getParameterizedType();
                        boolean valid =
                                Object.class.equals(targetPropertyType) || sourcePropertyType.equals(targetPropertyType)
                                        && sourceReturnType.equals(targetReturnType);
                        if (!valid) {
                            discardedProperties.add(sourceProperty.getName());
                        }
                    }
                } else {
                    discardedProperties.add(sourceProperty.getName());
                }
            }
        }
        discardedPropertiesCache.put(cacheKey, unmodifiableList(discardedProperties));

        return discardedProperties;
    }

    public static <T> RegistrationBuilder<T> fromBean(Class<T> source) {
        return new RegistrationBuilder<>(source);
    }

    public void register(Registration<?> registration) {
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
    public <U, T> T convert(U source, Class<T> aClass, BiFunction<U, T, T>... onTheFlyConvert) {
        T current = convert(source, aClass);
        for (BiFunction<U, T, T> function : onTheFlyConvert) {
            current = function.apply(source, current);
        }
        return current;
    }

    @Override
    @NewSpan("conversion")
    public <U> U convert(Object source, @SpanTag("target") Class<U> targetClass) {
        if (source == null) {
            return null;
        }
        if (tracer != null) {
            tracer.addTag(Span.SPAN_LOCAL_COMPONENT_TAG_NAME, BeanConversionService.class.getName());
            tracer.addTag("source", "class " + source.getClass().getName());
        }
        if (source.getClass().equals(targetClass)) {
            return (U) source;
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
