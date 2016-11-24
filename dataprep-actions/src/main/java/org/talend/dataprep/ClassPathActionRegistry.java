package org.talend.dataprep;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

public class ClassPathActionRegistry implements ActionRegistry {

    private static final Object lock = new Object();

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathActionRegistry.class);

    private final Map<String, Class<? extends ActionDefinition>> nameToActionClass = new TreeMap<>();

    public ClassPathActionRegistry(String... actionPackages) {
        synchronized (lock) { // Reflections is not thread safe (see https://github.com/ronmamo/reflections/issues/81).
            for (String actionPackage : actionPackages) {
                LOGGER.info("Scanning classpath @ '{}'", actionPackage);
                Reflections reflections = new Reflections(actionPackage);
                final Set<Class<? extends ActionDefinition>> allActions = reflections.getSubTypesOf(ActionDefinition.class);
                LOGGER.info("Found {} possible action class(es) in '{}'", allActions.size(), actionPackage);
                for (Class<? extends ActionDefinition> action : allActions) {
                    try {
                        if (!Modifier.isAbstract(action.getModifiers())) {
                            final Constructor<?>[] constructors = action.getConstructors();
                            for (Constructor<?> constructor : constructors) {
                                if(constructor.getParameters().length == 0) {
                                    final ActionDefinition actionMetadata = action.newInstance();
                                    nameToActionClass.put(actionMetadata.getName(), action);
                                    break;
                                }
                            }
                        } else {
                            LOGGER.info("Skip class '{}' (abstract class).", action.getName());
                        }
                    } catch (Exception e) {
                        LOGGER.error("Unable to register action '{}'", action.getName(), e);
                    }
                }
            }
        }
        LOGGER.info("{} actions registered for usage.", nameToActionClass.size());
    }

    private ActionDefinition getDefinition(Class<? extends ActionDefinition> aClass) {
        try {
            return aClass.newInstance();
        } catch (Exception e) {
            LOGGER.error("Unable to return action definition '{}'", aClass.getName(), e);
            return null;
        }
    }

    @Override
    public ActionDefinition get(String name) {
        final Class<? extends ActionDefinition> aClass = nameToActionClass.get(name);
        if (aClass == null) {
            LOGGER.error("Action definition '{}' does not exist.", name);
            return null;
        } else {
            return getDefinition(aClass);
        }
    }

    @Override
    public Stream<Class<? extends ActionDefinition>> getAll() {
        return nameToActionClass.values().stream();
    }

    @Override
    public Stream<ActionDefinition> findAll() {
        return nameToActionClass.values().stream().map(this::getDefinition);
    }
}
