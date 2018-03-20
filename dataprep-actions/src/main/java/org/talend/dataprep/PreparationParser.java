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

package org.talend.dataprep;

import static org.talend.dataprep.api.action.ActionDefinition.Behavior.FORBID_DISTRIBUTED;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.METADATA_CREATE_COLUMNS;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.json.MixedContentMapModule;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides utility to parses actions and associated {@link RowMetadata}.
 */
public class PreparationParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationParser.class);

    public static final ActionRegistry actionRegistry = new ClassPathActionRegistry("org.talend.dataprep.transformation.actions");

    private static final ActionFactory actionFactory = new ActionFactory();

    private static final ObjectMapper mapper = new ObjectMapper();

    private static void assertPreparation(Object preparation) {
        if (preparation == null) {
            throw new IllegalArgumentException("Preparation can not be null.");
        }
    }

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new MixedContentMapModule());
    }

    public static StandalonePreparation parsePreparation(InputStream preparation) {
        assertPreparation(preparation);
        try {
            final StandalonePreparation preparationMessage = mapper.reader(StandalonePreparation.class).readValue(preparation);
            if (preparationMessage.getRowMetadata() == null) {
                preparationMessage.setRowMetadata(new RowMetadata());
            }
            return preparationMessage;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to parse preparation", e);
        }
    }

    public static List<RunnableAction> ensureActionRowsExistence(List<Action> actions, boolean allowNonDistributedActions) {
        return actions.stream() //
                .map(action -> {
                    final ActionDefinition actionDefinition = actionRegistry.get(action.getName());
                    // Distributed run check for action
                    final Set<ActionDefinition.Behavior> behavior = actionDefinition.getBehavior();
                    // if non distributed actions are forbidden (e.g. running locally)
                    if (!allowNonDistributedActions) {
                        // if some actions cannot be run in distributed environment, let's see how bad it is...
                        if (behavior.contains(FORBID_DISTRIBUTED)) {
                            // actions that changes the schema (potentially really harmful for the preparation) throws an exception
                            if (behavior.contains(METADATA_CREATE_COLUMNS)) {
                                throw new IllegalArgumentException("Action '" + actionDefinition.getName() + "' cannot run in distributed environments.");
                            } else {
                                // else the action is just skipped
                                LOGGER.warn("Action '{}' cannot run in distributed environment, skip its execution.", actionDefinition.getName());
                                return null;
                            }
                        }
                    }
                    return action;
                }) //
                .filter(Objects::nonNull) //
                .map(a -> actionFactory.create(actionRegistry.get(a.getName()), a.getParameters())) //
                .collect(Collectors.toList());
    }

}
