// ============================================================================
//
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

package org.talend.dataprep.transformation.actions.date;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a Spring-like mechanism when non-Spring code wants to lookup for:
 * <ul>
 * <li>An object managed by IoC container: this class can substitute Spring lookup and singleton management</li>
 * <li>An interface implementation available in classpath: if no Spring is available, this class will lookup for the first
 * implementation in same package as the interface.</li>
 * </ul>
 */
public class Providers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Providers.class);

    private static final SingletonProvider DEFAULT_PROVIDER = new SingletonProvider(new InstantiationProvider());

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

    /**
     * Provider interface to provide how to create instance of <code>clazz</code>.
     * @see #setProvider(Provider)
     */
    @FunctionalInterface
    public interface Provider {

        <T> T get(Class<T> clazz, Object... args);
    }

    private static class SingletonProvider implements Provider {

        private final Provider next;

        Map<Class, Object> singletons = new HashMap<>();

        SingletonProvider(Provider next) {
            this.next = next;
        }

        @Override
        public <T> T get(Class<T> clazz, Object... args) {
            return clazz.cast(singletons.getOrDefault(clazz, next.get(clazz, args)));
        }
    }

    private static class InstantiationProvider implements Provider {

        private static final Object lock = new Object();

        InstantiationProvider() {
        }

        @Override
        public <T> T get(Class<T> clazz, Object... args) {
            if (clazz.isInterface()) {
                synchronized (lock) {
                    try {
                        Reflections reflections = new Reflections(clazz.getPackage().getName());
                        final Set<Class<? extends T>> subTypesOf = reflections.getSubTypesOf(clazz);
                        return clazz.cast(subTypesOf.iterator().next().newInstance());
                    } catch (Exception e) {
                        throw new UnsupportedOperationException("Unable to instantiate '" + clazz.getName() + "'.", e);
                    }
                }
            }
            try {
                final List<Class> argsClasses = Stream.of(args) //
                        .map(Object::getClass) //
                        .collect(Collectors.toList());
                if (argsClasses.isEmpty()) {
                    return clazz.newInstance();
                } else {
                    return clazz.getConstructor(argsClasses.toArray(new Class[argsClasses.size()])).newInstance();
                }
            } catch (Exception e) {
                throw new UnsupportedOperationException("Unable to instantiate '" + clazz.getName() + "'.", e);
            }
        }
    }

}
