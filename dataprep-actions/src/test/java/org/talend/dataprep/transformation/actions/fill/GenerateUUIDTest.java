// ============================================================================
//
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
package org.talend.dataprep.transformation.actions.fill;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

public class GenerateUUIDTest extends AbstractMetadataBaseTest<GenerateUUID> {

    public GenerateUUIDTest() {
        super(new GenerateUUID());
    }

    /**
     * The action parameters.
     */
    private Map<String, String> parameters;

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = GenerateUUIDTest.class.getResourceAsStream("generateUUIDAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Test
    public void test_action_name() throws Exception {
        assertEquals("generate_a_uuid", action.getName());
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.STRINGS_ADVANCED.getDisplayName(Locale.US)));
    }

    @Test
    public void should_accept_every_column() {
        assertTrue(action.acceptField(null));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_ENABLED;
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "0012.50", "tata");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(row.get("0000"), "toto");
        assertNotEquals(row.get("0001"), "0012.50");
        assertEquals(row.get("0002"), "tata");
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final DataSetRow row = getRow("toto", "0012.50", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(4, row.getRowMetadata().getColumns().size());
        assertEquals(row.get("0000"), "toto");
        assertEquals(row.get("0001"), "0012.50");
        assertEquals(row.get("0002"), "tata");
        assertNotNull(row.get("0003"));
    }
}