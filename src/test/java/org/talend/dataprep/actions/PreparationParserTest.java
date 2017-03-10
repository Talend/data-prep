// ============================================================================
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

package org.talend.dataprep.actions;

import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.*;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.PreparationParser;
import org.talend.dataprep.StandalonePreparation;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.actions.text.ReplaceOnValue;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

public class PreparationParserTest {

    ActionRegistry registry = new ClassPathActionRegistry("org.talend.dataprep.transformation.actions");

    ActionFactory factory = new ActionFactory();

    @Test
    public void testActionSample() throws Exception {
        final PreparationMessage corePreparation;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("single_lookup.json")) {
            corePreparation = PreparationParser.parsePreparation(resourceAsStream);
        }

        assertNotNull(corePreparation);
        assertNotNull(corePreparation.getActions());
        assertNotNull(corePreparation.getRowMetadata());
        assertEquals(1, corePreparation.getActions().size());
        assertEquals("lookup", corePreparation.getActions().get(0).getName());
        assertEquals("[{\"id\":\"0001\",\"name\":\"State\"},{\"id\":\"0002\",\"name\":\"Capital\"}]",
                corePreparation.getActions().get(0).getParameters().get("lookup_selected_cols"));
    }

    @Test
    public void testActionRows() throws Exception {
        final StandalonePreparation preparation;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class
                .getResourceAsStream("standalone_preparation_single_lookup.json")) {
            preparation = PreparationParser.parsePreparation(resourceAsStream);
        }

        assertNotNull(preparation);
        assertNotNull(preparation.getActions());
        List<RunnableAction> actionsWithRowActions = PreparationParser.ensureActionRowsExistence(preparation.getActions(), false);
        actionsWithRowActions.size();

    }

    @Test
    public void testIgnoreNonDistributableActions() throws Exception {
        registry.findAll() //
                .filter(actionDefinition -> !(actionDefinition instanceof ReplaceOnValue)) // Done in next unit tests
                .filter(actionDefinition -> actionDefinition.getBehavior().contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED)) //
                .map(actionDefinition -> {
                    final Map<String, String> emptyParameters;
                    if (actionDefinition.acceptScope(ScopeCategory.CELL)) {
                        emptyParameters = new HashMap<String, String>() {

                            {
                                put(SCOPE.getKey(), "cell");
                                put(ROW_ID.getKey(), "0");
                                put(COLUMN_ID.getKey(), "0");
                            }
                        };
                    } else if (actionDefinition.acceptScope(ScopeCategory.COLUMN)) {
                        emptyParameters = new HashMap<String, String>() {

                            {
                                put(SCOPE.getKey(), "column");
                                put(COLUMN_ID.getKey(), "0");
                            }
                        };
                    } else if (actionDefinition.acceptScope(ScopeCategory.LINE)) {
                        emptyParameters = new HashMap<String, String>() {

                            {
                                put(SCOPE.getKey(), "line");
                                put(ROW_ID.getKey(), "0");
                            }
                        };
                    } else if (actionDefinition.acceptScope(ScopeCategory.DATASET)) {
                        emptyParameters = Collections.singletonMap(SCOPE.getKey(), "dataset");
                    } else {
                        emptyParameters = Collections.singletonMap(SCOPE.getKey(), "unknown");
                    }
                    final Action runnableAction = factory.create(actionDefinition, emptyParameters);
                    return PreparationParser.ensureActionRowsExistence(Collections.singletonList(runnableAction), false);
                }) //
                .forEach(runnableActions -> assertTrue(runnableActions.isEmpty()));
    }


    @Test
    public void testNotIgnoreColumnEdition() throws Exception {
        final ReplaceOnValue replaceOnValue = new ReplaceOnValue();
        final Map<String, String> emptyParameters = new HashMap<String, String>() {

            {
                put(SCOPE.getKey(), ScopeCategory.COLUMN.name());
                put(COLUMN_ID.getKey(), "0");
            }
        };
        final Action runnableAction = factory.create(replaceOnValue, emptyParameters);
        final List<RunnableAction> runnableActions = PreparationParser.ensureActionRowsExistence(Collections.singletonList(runnableAction), false);

        assertEquals(1, runnableActions.size());
    }


}
