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
package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.text.Substring.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;

/**
 * Test class for Split action. Creates one consumer, and test it.
 *
 * @see Split
 */
public class SubstringTest {

    private Substring action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        action = new Substring();
        parameters = ActionMetadataTestUtils.parseParameters(SubstringTest.class.getResourceAsStream("substringAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS.getDisplayName()));
    }

    @Test
    public void should_substring() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", " ipsum ");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_to_the_end_when_end_index_is_too_big_and_out_of_bound() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ip");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ip");
        expectedValues.put("0003", " ip");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_from_the_end_when_start_index_is_too_big_and_out_of_bound() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bac");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bac");
        expectedValues.put("0003", "");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_from_the_end_when_start_index_is_negative() {
        //given
        parameters.put(FROM_INDEX_PARAMETER, "-1");
        parameters.put(TO_MODE_PARAMETER, "to_end");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bac");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bac");
        expectedValues.put("0003", "Bac");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_to_the_beginning_when_end_index_is_negative() {
        //given
        parameters.put(TO_INDEX_PARAMETER, "-1");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bac");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bac");
        expectedValues.put("0003", "");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_to_the_beginning_when_n_before_end_is_too_big() throws IOException {
        //given
        parameters.put(FROM_INDEX_PARAMETER, "1");
        parameters.put(TO_MODE_PARAMETER, Substring.TO_N_BEFORE_END_PARAMETER);
        parameters.put(TO_N_BEFORE_END_PARAMETER, "15");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon");
        expectedValues.put("0003", "");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_from_the_beginning_when_n_before_end_is_too_big() throws IOException {
        //given
        parameters.put(FROM_MODE_PARAMETER, Substring.FROM_N_BEFORE_END_PARAMETER);
        parameters.put(FROM_N_BEFORE_END_PARAMETER, "15");
        parameters.put(TO_MODE_PARAMETER, Substring.TO_N_BEFORE_END_PARAMETER);
        parameters.put(TO_N_BEFORE_END_PARAMETER, "1");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon");
        expectedValues.put("0003", "Baco");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_resulting_to_empty_on_strange_bounds_start_index_sup_to_end_index() throws IOException {
        //given
        parameters.put(FROM_INDEX_PARAMETER, "6");
        parameters.put(TO_INDEX_PARAMETER, "1");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", "");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_twice() throws IOException {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0004", "acon ");
        expectedValues.put("0003", " ipsum ");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        parameters.put(FROM_INDEX_PARAMETER, "1");
        parameters.put(TO_INDEX_PARAMETER, "6");
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_begining() throws IOException {
        //given
        parameters.put(FROM_MODE_PARAMETER, Substring.FROM_BEGINNING);

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", "Bacon ipsum ");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_end() throws IOException {
        //given
        parameters.put(TO_MODE_PARAMETER, Substring.TO_END);

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", " ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_n_before_end_1() throws IOException {
        //given
        parameters.put(FROM_MODE_PARAMETER, Substring.FROM_N_BEFORE_END_PARAMETER);
        parameters.put(FROM_N_BEFORE_END_PARAMETER, "5");
        parameters.put(TO_MODE_PARAMETER, Substring.TO_END);

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", "belly");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_n_before_end_2() throws IOException {
        //given
        parameters.put(FROM_MODE_PARAMETER, Substring.FROM_N_BEFORE_END_PARAMETER);
        parameters.put(FROM_N_BEFORE_END_PARAMETER, "5");
        parameters.put(TO_MODE_PARAMETER, TO_INDEX_PARAMETER);
        parameters.put(TO_INDEX_PARAMETER, "9");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum");
        expectedValues.put("0003", "ips");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_n_before_end_3() throws IOException {
        //given
        parameters.put(FROM_MODE_PARAMETER, Substring.FROM_INDEX_PARAMETER);
        parameters.put(FROM_INDEX_PARAMETER, "6");
        parameters.put(TO_MODE_PARAMETER, Substring.TO_N_BEFORE_END_PARAMETER);
        parameters.put(TO_N_BEFORE_END_PARAMETER, "1");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum");
        expectedValues.put("0003", "ipsu");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_n_before_end_4() throws IOException {
        //given
        parameters.put(FROM_MODE_PARAMETER, Substring.FROM_N_BEFORE_END_PARAMETER);
        parameters.put(FROM_N_BEFORE_END_PARAMETER, "5");
        parameters.put(TO_MODE_PARAMETER, Substring.TO_N_BEFORE_END_PARAMETER);
        parameters.put(TO_N_BEFORE_END_PARAMETER, "1");

        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum");
        expectedValues.put("0003", "ipsu");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_substring_the_new_substring() throws IOException {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", " ipsum ");
        expectedValues.put("0004", "ips");
        expectedValues.put("0002", "01/01/2015");

        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //when
        parameters.put("column_id", "0003");
        parameters.put(FROM_INDEX_PARAMETER, "1");
        parameters.put(TO_INDEX_PARAMETER, "4");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0003");
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_update_metadata() {
        //given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0003", "steps_substring"));
        expected.add(createMetadata("0002", "last update"));

        //when
        ActionTestWorkbench.test(rowMetadata, action.create(parameters).getRowAction());

        //then
        assertEquals(expected, rowMetadata.getColumns());
    }

    @Test
    public void should_update_metadata_twice() {
        //given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);
        final DataSetRow row = new DataSetRow(rowMetadata);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0004", "steps_substring"));
        expected.add(createMetadata("0003", "steps_substring"));
        expected.add(createMetadata("0002", "last update"));

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction(), action.create(parameters).getRowAction());

        //then
        assertEquals(expected, row.getRowMetadata().getColumns());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

    /**
     * @param name name of the column metadata to create.
     * @return a new column metadata
     */
    private ColumnMetadata createMetadata(String id, String name) {
        return ColumnMetadata.Builder.column().computedId(id).name(name).type(Type.STRING).headerSize(12).empty(0).invalid(2)
                .valid(5).build();
    }

}
