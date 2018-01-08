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

package org.talend.dataprep.transformation.actions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.transformation.actions.date.DateParser;

/**
 * This class provides a Spring-like mechanism when non-Spring code wants to lookup for:
 * <ul>
 * <li>An object managed by IoC container: this class can substitute Spring lookup and singleton management</li>
 * <li>An interface implementation available in classpath: if no Spring is available, this class will lookup for the first
 * implementation in same package as the interface.</li>
 * </ul>
 */
public class Providers {

    public static final SingletonProvider DEFAULT_PROVIDER = new SingletonProvider(new InstantiationProvider());

    private static final Logger LOGGER = LoggerFactory.getLogger(Providers.class);

    private static Provider provider = DEFAULT_PROVIDER;

    public static void setProvider(Provider newProvider) {
        LOGGER.info("Change action component provider from '{}' to '{}'.", provider, newProvider);
        provider = newProvider;
    }

    /**
     * Returns a instance of the <code>clazz</code>. In non-Spring context, it creates a singleton.
     *
     * @param clazz The clazz to be instantiated. In case of interfaces, code will lookup for the first implementation of the
     * interface available in the package (and sub-packages) of the interface.
     * @param <T> The type of the instance (to prevent casts).
     * @return An instance of <code>clazz</code>.
     */
    public static <T> T get(Class<T> clazz) {
        return get(clazz, new Object[0]);
    }

    /**
     * Returns a instance of the <code>clazz</code>. In non-Spring context, it creates a singleton.
     *
     * @param clazz The clazz to be instantiated. In case of interfaces, code will lookup for the first implementation of the
     * interface available in the package (and sub-packages) of the interface.
     * @param <T> The type of the instance (to prevent casts).
     * @return An instance of <code>clazz</code>.
     */
    public static <T> T get(Class<T> clazz, Object... args) {
        return provider.get(clazz, args);
    }

    /**
     * @return A {@link DateParser} ready for use. This is a shortcut for {@link #get(Class)} using {@link DateParser} class as
     * parameter.
     */
    public static DateParser get() {
        return provider.get(DateParser.class);
    }

    public static void clear() {
        provider.clear();
    }

    /**
     * Provider interface to provide how to create instance of <code>clazz</code>.
     *
     * @see #setProvider(Provider)
     */
    public interface Provider {

        <T> T get(Class<T> clazz, Object... args);

        void clear();
    }

    private static class SingletonProvider implements Provider {

        private final Provider next;

        private final Map<Class, Object> singletons = new HashMap<>();

        SingletonProvider(Provider next) {
            this.next = next;
        }

        @Override
        public <T> T get(Class<T> clazz, Object... args) {
            final Object o;
            if (singletons.containsKey(clazz)) {
                o = singletons.get(clazz);
            } else {
                final T newValue = next.get(clazz, args);
                Annotation[] annotations = newValue.getClass().getDeclaredAnnotations();
                if (Arrays.stream(annotations).anyMatch(a -> a instanceof PrototypeScope)) {
                    o = next.get(clazz, args);
                } else {
                    singletons.put(clazz, newValue);
                    o = newValue;
                }
            }
            return clazz.cast(o);
        }

        @Override
        public void clear() {
            singletons.clear();
        }
    }

    private static class InstantiationProvider implements Provider {

        private static final Object lock = new Object();

        InstantiationProvider() {
        }

        @Override
        public <T> T get(Class<T> clazz, Object... args) {
            final List<Class> argsClasses = Stream.of(args) //
                    .map(Object::getClass) //
                    .collect(Collectors.toList());

            if (clazz.isInterface()) {
                synchronized (lock) {
                    try {
                        Reflections reflections = new Reflections(clazz.getPackage().getName());
                        final Set<Class<? extends T>> subTypesOf = reflections.getSubTypesOf(clazz);
                        LOGGER.debug("Found these sub types of " + clazz.getName() + ": {}", subTypesOf);
                        Constructor constructor = null;
                        for (Class<? extends T> subType : subTypesOf) {
                            try {
                                constructor = subType.getConstructor(argsClasses.toArray(new Class[argsClasses.size()]));
                                constructor.setAccessible(true);
                            } catch (NoSuchMethodException e) {
                                LOGGER.debug("Skip class '{}' (no constructor with arguments '{}')", clazz, args);
                            }
                        }
                        if (constructor == null) {
                            throw new IllegalArgumentException("No implementation of '" + clazz.getName() + "' with constructor '"
                                    + Arrays.toString(args) + "' found.");
                        } else {
                            if (argsClasses.isEmpty()) {
                                return clazz.cast(constructor.newInstance());
                            } else {
                                return clazz.cast(constructor.newInstance(args));
                            }
                        }
                    } catch (Exception e) {
                        throw new UnsupportedOperationException("Unable to instantiate a sub type of interface'" + clazz.getName()
                                + "' with args: '" + Arrays.toString(args) + "'.", e);
                    }
                }
            } else {
                // Is a class (not an interface).
                try {
                    if (argsClasses.isEmpty()) {
                        return clazz.newInstance();
                    } else {
                        return clazz.getConstructor(argsClasses.toArray(new Class[argsClasses.size()])).newInstance(args);
                    }
                } catch (Exception e) {
                    throw new UnsupportedOperationException("Unable to instantiate '" + clazz.getName() + "'.", e);
                }
            }
        }

        @Override
        public void clear() {
            // Nothing to do.
        }
    }

}
