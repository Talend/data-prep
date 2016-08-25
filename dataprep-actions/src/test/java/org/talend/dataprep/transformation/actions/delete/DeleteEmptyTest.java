//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.actions.delete;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for DeleteEmpty action. Creates one consumer, and test it.
 *
 * @see DeleteEmpty
 */
public class DeleteEmptyTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    private DeleteEmpty action = new DeleteEmpty();

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(DeleteEmptyTest.class.getResourceAsStream("deleteEmptyAction.json"));
    }

    @Test
    public void testActionScope() throws Exception {
        assertThat(action.getActionScope(), hasItem("empty"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATA_CLEANSING.getDisplayName()));
    }

    @Test
    public void should_delete_because_value_not_set() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_delete_because_null() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", null);
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
    }

    @Test
    public void should_delete_because_empty() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
    }

    @Test
    public void should_delete_because_value_is_made_of_spaces() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", " ");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
    }

    @Test
    public void should_not_delete_because_value_set() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "-");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("-", row.get("0001"));
    }

    @Test
    public void should_not_delete_because_value_set_2() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", " a value ");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
    }

    @Test
    public void should_not_delete_because_value_set_of_boolean() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "true");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
    }

    @Test
    public void should_not_delete_because_value_set_of_number() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "45");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
    }

    @Test
    public void should_not_delete_because_value_set_of_negative_boolean() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "-12");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
    }

    @Test
    public void should_not_delete_because_value_set_of_float() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "0.001");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
        assertTrue(action.acceptField(getColumn(Type.DATE)));
        assertTrue(action.acceptField(getColumn(Type.BOOLEAN)));
        assertTrue(action.acceptField(getColumn(Type.ANY)));
    }

}
