//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.actions.bool;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.*;

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

/**
 * Test class for Negate action.
 *
 * @see Negate
 */
public class NegateTest extends AbstractMetadataBaseTest<Negate> {

    private Map<String, String> parameters;

    /**
     * Default empty constructor
     */
    public NegateTest() throws IOException {
        super(new Negate());
        parameters = ActionMetadataTestUtils.parseParameters(NegateTest.class.getResourceAsStream("negateAction.json"));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.BOOLEAN.getDisplayName(Locale.US)));
    }

    @Test
    public void testActionScope() throws Exception {
        assertThat(action.getActionScope(), is(new ArrayList<>()));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        Map<String, String> values = new LinkedHashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "R&D");
        values.put("0002", "true");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "R&D");
        expectedValues.put("0002", "true");
        expectedValues.put("0003", "False");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0002_negate").type(Type.BOOLEAN).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_inplace() {
        // given
        Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "R&D");
        values.put("0002", "true");
        DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row.get("0002"), is("False"));
    }

    @Test
    public void should_negate_false() {
        // given
        Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "R&D");
        values.put("0002", "false");
        DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row.get("0002"), is("True"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }
}
