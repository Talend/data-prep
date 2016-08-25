package org.talend.dataprep.transformation.actions;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;

public class SerializableTest extends AbstractMetadataBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializableTest.class);

    ActionDefinition[] actions = new ActionDefinition[0];

    @Test
    public void testSerializableClass() throws Exception {
        LOGGER.info("{} actions for testing...", actions.length);
        int okActions = 0;
        for (ActionDefinition action : actions) {
            Map<String, String> parameters = new HashMap<>();
            if (action.acceptScope(ScopeCategory.COLUMN)) {
                parameters.put(ImplicitParameters.SCOPE.getKey(), "COLUMN");
            } else if (action.acceptScope(ScopeCategory.CELL)) {
                parameters.put(ImplicitParameters.SCOPE.getKey(), "CELL");
            } else if (action.acceptScope(ScopeCategory.LINE)) {
                parameters.put(ImplicitParameters.SCOPE.getKey(), "LINE");
            } else if (action.acceptScope(ScopeCategory.DATASET)) {
                parameters.put(ImplicitParameters.SCOPE.getKey(), "DATASET");
            }

            parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0000");
            parameters.put(ImplicitParameters.ROW_ID.getKey(), "0");

            final Action actionInstance = factory.create(action, parameters);
            try {
                try (ObjectOutputStream oos = new ObjectOutputStream(new NullOutputStream())) {
                    oos.writeObject(actionInstance);
                    oos.flush();
                }
                okActions++;
            } catch (IOException e) {
                LOGGER.error("Unable to serialize {}", action.getClass());
            }
        }
        LOGGER.info("{} actions for tested ({}/{} ok).", actions.length, okActions, actions.length);
    }

}
