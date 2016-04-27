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

package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for DeleteOnValue action. Creates one consumer, and test it.
 *
 * @see DeleteNegativeValues
 */
public class DeleteNegativeValuesTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private DeleteNegativeValues action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(DeleteNegativeValuesTest.class.getResourceAsStream("deleteNegativeValuesAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testName() throws Exception {
        assertEquals(action.getName(), "delete_negative_values");
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATA_CLEANSING.getDisplayName()));
    }

    @Test
    public void should_delete() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "-12");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("-12", row.get("0001"));
    }

    @Test
    public void should_delete_alt_format_1() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "-1 200");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_delete_alt_format_2() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "(1 200,55)");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_delete_even_with_leading_space() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", " -12"); // notice the space before ' Berlin'
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals(" -12", row.get("0001"));
    }

    @Test
    public void should_delete_even_with_trailing_space() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "-12 "); // notice the space after 'Berlin '
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("-12 ", row.get("0001"));
    }

    @Test
    public void should_delete_even_with_enclosing_spaces() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", " -12 "); // notice the spaces enclosing ' Berlin '
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals(" -12 ", row.get("0001"));
    }

    @Test
    public void should_not_delete_because_value_not_found() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_not_delete_because_value_different() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "68");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("68", row.get("0001"));
    }

    @Test
    public void should_not_delete_because_value_different_zero() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "0");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("0", row.get("0001"));
    }

    @Test
    public void should_not_delete_because_null_value() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "Michael Jordan");
        values.put("age", null);
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("Michael Jordan", row.get("name"));
        values.put("age", null);
    }

    @Test
    public void should_not_delete_because_nan() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "Michael Jordan");
        values.put("age", "undefined");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("Michael Jordan", row.get("name"));
        values.put("age", "undefined");
    }

    @Test
    public void should_delete_even_with_space_between_sign_and_value() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "- 6");
        final DataSetRow row = new DataSetRow(values);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("- 6", row.get("0001"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptColumn(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.STRING)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptColumn(getColumn(Type.ANY)));
    }

}
