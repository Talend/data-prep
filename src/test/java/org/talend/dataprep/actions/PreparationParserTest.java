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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.talend.dataprep.PreparationParser;
import org.talend.dataprep.StandalonePreparation;
import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.transformation.actions.common.RunnableAction;

public class PreparationParserTest {

    @Test
    public void testActionSample() throws Exception {
        final PreparationMessage corePreparation;
        try (final InputStream resourceAsStream = DefaultActionParserTest.class.getResourceAsStream("single_lookup.json")) {
            corePreparation = PreparationParser.parseCorePreparation(resourceAsStream);
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
            preparation = PreparationParser.parseExportableCorePreparation(resourceAsStream);
        }

        assertNotNull(preparation);
        assertNotNull(preparation.getActions());
        List<RunnableAction> actionsWithRowActions = PreparationParser.ensureActionRowsExistence(preparation.getActions());
        actionsWithRowActions.size();

    }

}
