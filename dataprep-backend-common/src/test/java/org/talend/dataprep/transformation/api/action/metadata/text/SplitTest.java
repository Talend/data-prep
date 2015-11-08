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
import static org.talend.dataprep.transformation.api.action.metadata.text.ReplaceOnValue.CELL_VALUE_PARAMETER;
import static org.talend.dataprep.transformation.api.action.metadata.text.ReplaceOnValue.REPLACE_ENTIRE_CELL_PARAMETER;
import static org.talend.dataprep.transformation.api.action.metadata.text.ReplaceOnValue.REPLACE_VALUE_PARAMETER;

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
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for Split action. Creates one consumer, and test it.
 *
 * @see Split
 */
public class SplitTest {

    /**
     * The action to test.
     */
    private Split action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        action = new Split();

        parameters = ActionMetadataTestUtils.parseParameters( //
                //
                SplitTest.class.getResourceAsStream("splitAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.SPLIT.getDisplayName()));
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void should_split_row() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0002", "01/01/2015");

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_TDP_831() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Je vais bien (tout va bien)");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(Split.SEPARATOR_PARAMETER, "other");
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER, "(");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Je vais bien (tout va bien)");
        expectedValues.put("0003", "");
        expectedValues.put("0004", "");
        expectedValues.put("0002", "01/01/2015");

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void should_split_row_twice() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0005", "Bacon");
        expectedValues.put("0006", "ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0002", "01/01/2015");

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Action#getRowAction()
     */
    @Test
    public void should_split_row_with_separator_at_the_end() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "");
        expectedValues.put("0002", "01/01/2015");

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Action#getRowAction()
     */
    @Test
    public void should_split_row_no_separator() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "");
        expectedValues.put("0002", "01/01/2015");

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expectedValues, row.values());
    }


    /**
     * @see Action#getRowAction()
     */
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
        expected.add(createMetadata("0003", "steps_split"));
        expected.add(createMetadata("0004", "steps_split"));
        expected.add(createMetadata("0002", "last update"));

        //when
        action.applyOnColumn(new DataSetRow(rowMetadata), new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expected, rowMetadata.getColumns());
    }

    /**
     * @see Action#getRowAction()
     */
    @Test
    public void should_update_metadata_twice() {
        //given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0005", "steps_split"));
        expected.add(createMetadata("0006", "steps_split"));
        expected.add(createMetadata("0003", "steps_split"));
        expected.add(createMetadata("0004", "steps_split"));
        expected.add(createMetadata("0002", "last update"));

        //when
        action.applyOnColumn(new DataSetRow(rowMetadata), new TransformationContext(), parameters, "0001");
        action.applyOnColumn(new DataSetRow(rowMetadata), new TransformationContext(), parameters, "0001");

        assertEquals(expected, rowMetadata.getColumns());
    }


    @Test
    public void should_not_split_because_null_separator() throws IOException {
        // given
        final Split nullSeparatorAction = new Split();
        final Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                //
                SplitTest.class.getResourceAsStream("splitActionWithNullSeparator.json"));

        final Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("last update", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        //when
        nullSeparatorAction.applyOnColumn(row, new TransformationContext(), parameters, "steps");

        // then
        assertEquals(values, row.values());
    }

    @Test
    public void should_not_update_metadata_because_null_separator() throws IOException {
        // given
        final Split nullSeparatorAction = new Split();
        final Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                //
                SplitTest.class.getResourceAsStream("splitActionWithNullSeparator.json"));

        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("recipe", "recipe"));
        input.add(createMetadata("steps", "steps"));
        input.add(createMetadata("last update", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        // when
        nullSeparatorAction.applyOnColumn(new DataSetRow(rowMetadata), new TransformationContext(), parameters, "steps");

        // then
        assertEquals(input, rowMetadata.getColumns());
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
