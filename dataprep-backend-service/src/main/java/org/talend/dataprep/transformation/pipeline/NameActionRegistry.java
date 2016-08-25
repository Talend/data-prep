package org.talend.dataprep.transformation.pipeline;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.action.ActionDefinition;

@Component
public class NameActionRegistry implements ActionRegistry { // NOSONAR

    @Autowired(required = false)
    private List<ActionDefinition> actions;

    @Override
    public ActionDefinition get(String name) {
        for (ActionDefinition action : actions) {
            if (action.getName().equals(name)) {
                return action;
            }
        }
        return null;
    }

    @Override
    public Stream<Class<? extends ActionDefinition>> getAll() {
        return actions.stream().map(ActionDefinition::getClass);
    }
}
