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
package org.talend.dataprep.transformation.actions.date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for Split action. Creates one consumer, and test it.
 *
 * @see TimestampToDate
 */
public class TimestampToDateTest extends BaseDateTest<TimestampToDate> {

    private Map<String, String> parameters;

    public TimestampToDateTest() {
        super(new TimestampToDate());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(TimestampToDateTest.class.getResourceAsStream("timestampToDate.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.DATE.getDisplayName(Locale.US)));
    }

    @Override
    protected CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.VISIBLE_ENABLED;
    }

    @Test
    public void test_apply_inplace() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "0");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "01-01-1970");
        expectedValues.put("0002", "01/01/2015");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "0");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "0");
        expectedValues.put("0003", "01-01-1970");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_convert_to_date_empty() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "");
        expectedValues.put("0003", "");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * Behavior of this case, originally defined TDP-925, has been changed with TDP-1108.
     */
    @Test
    public void test_TDP_925() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "1441815638");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "1441815638");
        expectedValues.put("0002", "01/01/2015");

        parameters.put("new_pattern", "custom");
        parameters.put("custom_date_pattern", "not a valid date pattern");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testGetTimeStamp() {
        assertEquals("1970-01-01", action.getTimeStamp("0", DateTimeFormatter.ISO_LOCAL_DATE));
        assertEquals("2015-09-09", action.getTimeStamp("1441815638", DateTimeFormatter.ISO_LOCAL_DATE));
        assertEquals("2015-09-09T16:20:38", action.getTimeStamp("1441815638", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertEquals("", action.getTimeStamp("", DateTimeFormatter.ISO_LOCAL_DATE));
        assertEquals("", action.getTimeStamp(null, DateTimeFormatter.ISO_LOCAL_DATE));
    }

    @Test
    public void should_compute_length_twice() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "1441815638");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "1441815638");
        expectedValues.put("0004", "09-09-2015");
        expectedValues.put("0003", "09-09-2015");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters), factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_update_metadata() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "timestamp"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "timestamp"));
        expected.add(createMetadata("0003", "timestamp_as_date", Type.DATE));
        expected.add(createMetadata("0002", "last update"));

        // when
        ActionTestWorkbench.test(rowMetadata, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    @Test
    public void should_update_metadata_twice() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "timestamp"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "timestamp"));
        expected.add(createMetadata("0004", "timestamp_as_date", Type.DATE));
        expected.add(createMetadata("0003", "timestamp_as_date", Type.DATE));
        expected.add(createMetadata("0002", "last update"));

        // when
        ActionTestWorkbench.test(rowMetadata, actionRegistry, factory.create(action, parameters), factory.create(action, parameters));

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Override
    protected ColumnMetadata.Builder columnBaseBuilder() {
        return super.columnBaseBuilder().headerSize(12).valid(5).invalid(2).empty(0);
    }

    @Test
    public void should_create_string_column_for_custom_pattern() {
        // given
        parameters.put("new_pattern", "custom");
        parameters.put("custom_date_pattern", "yyyy");
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "0");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "0");
        expectedValues.put("0003", "1970");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
        assertThat(row.getRowMetadata().getById("0003").getType(), is("string"));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

}
