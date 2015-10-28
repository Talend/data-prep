// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.transformation.api.action.metadata.date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for Split action. Creates one consumer, and test it.
 *
 * @see TimestampToDate
 */
public class TimestampToDateTest extends BaseDateTests {

    /** The action to test. */
    @Autowired
    private TimestampToDate action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters( //
                //
                TimestampToDateTest.class.getResourceAsStream("timestampToDate.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATE.getDisplayName()));
    }

    @Test
    public void should_convert_to_date() {
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
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

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
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testgetTimeStamp(){
        assertEquals("1970-01-01", action.getTimeStamp("0", DateTimeFormatter.ISO_LOCAL_DATE));
        assertEquals("2015-09-09",action.getTimeStamp("1441815638", DateTimeFormatter.ISO_LOCAL_DATE));
        assertEquals("2015-09-09T16:20:38",action.getTimeStamp("1441815638", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertEquals("",action.getTimeStamp("", DateTimeFormatter.ISO_LOCAL_DATE));
        assertEquals("",action.getTimeStamp(null, DateTimeFormatter.ISO_LOCAL_DATE));
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
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

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
        action.applyOnColumn(new DataSetRow(rowMetadata), new TransformationContext(), parameters, "0001");

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
        action.applyOnColumn(new DataSetRow(rowMetadata), new TransformationContext(), parameters, "0001");
        action.applyOnColumn(new DataSetRow(rowMetadata), new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.INTEGER)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.STRING)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

    private ColumnMetadata createMetadata(String id, String name) {
        return createMetadata(id, name, Type.STRING);
    }

    private ColumnMetadata createMetadata(String id, String name, Type type) {
        return ColumnMetadata.Builder.column().computedId(id).name(name).type(type).headerSize(12).empty(0).invalid(2).valid(5)
                .build();
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
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
        assertThat(row.getRowMetadata().getById("0003").getType(), is("string"));
    }


}
