package org.talend.dataprep.transformation.pipeline;

import java.util.stream.Stream;

import org.talend.dataprep.api.action.ActionDefinition;

public interface ActionRegistry {

    ActionDefinition get(String name);

    Stream<Class<? extends ActionDefinition>> getAll();
}
