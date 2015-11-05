package org.talend.dataprep.transformation.api.action.metadata.delete;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for DeleteEmpty action. Creates one consumer, and test it.
 *
 * @see DeleteEmpty
 */
public class DeleteEmptyTest {

    /** The action to test. */
    private DeleteEmpty action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        action = new DeleteEmpty();

        parameters = ActionMetadataTestUtils.parseParameters( //
                //
                DeleteEmptyTest.class.getResourceAsStream("deleteEmptyAction.json"));
    }

    @Test
    public void testActionScope() throws Exception {
        assertThat(action.getActionScope(), hasItem("empty"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATA_CLEANSING.getDisplayName()));
    }

    @Test
    public void should_delete_because_value_not_set() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
    }

    @Test
    public void should_delete_because_null() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", null);
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertTrue(row.isDeleted());
    }

    @Test
    public void should_delete_because_empty() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertTrue(row.isDeleted());
    }

    @Test
    public void should_delete_because_value_is_made_of_spaces() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", " ");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertTrue(row.isDeleted());
    }

    @Test
    public void should_not_delete_because_value_set() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "-");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
        assertEquals("-", row.get("city"));
    }

    @Test
    public void should_not_delete_because_value_set_2() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", " a value ");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertFalse(row.isDeleted());
    }

    @Test
    public void should_not_delete_because_value_set_of_boolean() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "true");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertFalse(row.isDeleted());
    }

    @Test
    public void should_not_delete_because_value_set_of_number() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "45");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertFalse(row.isDeleted());
    }

    @Test
    public void should_not_delete_because_value_set_of_negative_boolean() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "-12");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertFalse(row.isDeleted());
    }

    @Test
    public void should_not_delete_because_value_set_of_float() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "0.001");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "city");

        //then
        assertFalse(row.isDeleted());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
        assertTrue(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptColumn(getColumn(Type.FLOAT)));
        assertTrue(action.acceptColumn(getColumn(Type.DATE)));
        assertTrue(action.acceptColumn(getColumn(Type.BOOLEAN)));
        assertTrue(action.acceptColumn(getColumn(Type.ANY)));
    }

}
