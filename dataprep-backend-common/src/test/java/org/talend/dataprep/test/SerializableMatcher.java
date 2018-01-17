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

package org.talend.dataprep.test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Matcher matcher} to test whether a {@link Class class} is serializable or not. It allows test as follow:
 * <code>
 *     assertThat(RowMetadata.class, SerializableMatcher.isSerializable());
 * </code>
 * @see #isSerializable()
 * @see #isNotSerializable()
 */
public class SerializableMatcher extends BaseMatcher<Class> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializableMatcher.class);

    private Set<Class> metClasses = new HashSet<>();

    private SerializableMatcher() {
    }

    /**
     * @return A {@link Matcher matcher} to test if class (and its fields) <b>is</b> serializable (i.e. all fields are
     * serializable).
     */
    public static Matcher<Class<?>> isSerializable() {
        SerializableMatcher matcher = new SerializableMatcher();
        return new BaseMatcher<Class<?>>() {
            @Override
            public boolean matches(Object item) {
                return matcher.matches(item);
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
                matcher.describeMismatch(item, mismatchDescription);
            }

            @Override
            public void describeTo(Description description) {
                matcher.describeTo(description);
            }
        };
    }

    /**
     * @return A {@link Matcher matcher} to test if class (and its fields) is <b>NOT</b> serializable (i.e. at least one
     * of fields is not serializable).
     */
    public static Matcher<Class<?>> isNotSerializable() {
        SerializableMatcher matcher = new SerializableMatcher();
        return new BaseMatcher<Class<?>>() {
            @Override
            public boolean matches(Object item) {
                return !matcher.matches(item);
            }

            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
                matcher.describeMismatch(item, mismatchDescription);
            }

            @Override
            public void describeTo(Description description) {
                matcher.describeTo(description);
            }
        };
    }

    private boolean isSerializable(Class clazz) {
        if (!metClasses.add(clazz)) {
            return true;
        }
        if (clazz.isPrimitive()) {
            return true;
        }
        if (clazz.isInterface()) {
            return true;
        }
        if (!Arrays.asList(clazz.getInterfaces()).contains(Serializable.class)) {
            LOGGER.error("Class '{}' does not implement Serializable.", clazz);
            return false;
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            final Class<?> fieldClass = field.getType();
            if (!isSerializable(fieldClass)) {
                LOGGER.error("Field '{}' (of class '{}') is not serializable.", field, fieldClass);
                return false;
            }
            if (fieldClass.isArray() && !isSerializable(fieldClass.getComponentType())) {
                LOGGER.error("Field '{}' (array of '{}') is not serializable.", field, fieldClass.getComponentType());
                return false;
            }
            final Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType) genericType;
                final Type[] types = parameterizedType.getActualTypeArguments();
                for (Type type : types) {
                    try {
                        if (!isSerializable(Class.forName(type.getTypeName()))) {
                            LOGGER.error("Generic type for field '{}' (of '{}') is not serializable.", field, type.getTypeName());
                            return false;
                        }
                    } catch (ClassNotFoundException e) {
                        LOGGER.error("Unable to find class '{}'", type.getTypeName(), e);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean matches(Object item) {
        return item instanceof Class && isSerializable(((Class) item));
    }

    @Override
    public void describeMismatch(Object item, Description mismatchDescription) {
    }

    @Override
    public void describeTo(Description description) {
    }
}
