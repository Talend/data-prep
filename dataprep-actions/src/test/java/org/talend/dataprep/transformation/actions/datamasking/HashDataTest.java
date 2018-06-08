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

package org.talend.dataprep.transformation.actions.datamasking;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

/**
 * Test class for HashData action.
 *
 * @see HashData
 */
public class HashDataTest extends AbstractMetadataBaseTest<HashData> {

    private Map<String, String> parameters;

    public HashDataTest() {
        super((new HashData()));
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(HashData.class.getResourceAsStream("hashdata.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.ANY).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.COLUMNS.getDisplayName(Locale.US)));
    }

    @Override
    protected CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void test_apply_in_newcolumn() {
        //given
        final Map<String, String> values = new LinkedHashMap<>();
        values.put("0000", "Pomme de pain");
        values.put("0001", "Pomme de terre");
        values.put("0002", "06/06/2018");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "Pomme de pain");
        expectedValues.put("0001", "Pomme de terre");
        expectedValues.put("0002", "06/06/2018");
        expectedValues.put("0003", "ef0841710e395c63f12a3aeb8dc9e712e57758cd39702bd669c3d5b835d6b4a0");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_hashed").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_inplace() {
        //given
        final Map<String, String> values = new LinkedHashMap<>();
        values.put("0000", "Pomme de pain");
        values.put("0001", "Pomme de terre");
        values.put("0002", "06/06/2018");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "ef0841710e395c63f12a3aeb8dc9e712e57758cd39702bd669c3d5b835d6b4a0");
        expectedValues.put("0001", "Pomme de terre");
        expectedValues.put("0002", "06/06/2018");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

}