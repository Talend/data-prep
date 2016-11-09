package org.talend.dataprep.transformation.pipeline;

import java.util.stream.Stream;

import org.talend.dataprep.api.action.ActionDefinition;

public interface ActionRegistry {

    /**
     * Returns the {@link ActionDefinition} for given <code>name</code> or <code>null</code> if not found.
     * @param name An action name
     * @return The {@link ActionDefinition} <b>instance </b>for given action <code>name</code>.
     */
    ActionDefinition get(String name);

    /**
     * @return All the {@link ActionDefinition} as <b>instances</b>.
     */
    Stream<Class<? extends ActionDefinition>> getAll();

    /**
     * @return All the {@link ActionDefinition} as <b>classes</b>.
     */
    Stream<ActionDefinition> findAll();
}
