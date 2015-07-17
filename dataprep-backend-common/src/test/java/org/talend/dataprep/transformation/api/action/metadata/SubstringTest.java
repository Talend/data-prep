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
package org.talend.dataprep.transformation.api.action.metadata;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;

/**
 * Test class for Split action. Creates one consumer, and test it.
 * 
 * @see Split
 */
public class SubstringTest {

    /** The row consumer to test. */
    private BiConsumer<DataSetRow, TransformationContext> rowClosure;

    /** The metadata consumer to test. */
    private BiConsumer<RowMetadata, TransformationContext> metadataClosure;

    /** The action to test. */
    private Substring action;

    /**
     * Constructor.
     */
    public SubstringTest() throws IOException {
        action = new Substring();
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                SubstringTest.class.getResourceAsStream("substringAction.json"));
        final Action action = this.action.create(parameters);
        rowClosure = action.getRowAction();
        metadataClosure = action.getMetadataAction();
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void should_substring() {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("last update", "01/01/2015");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("steps_substring_1", " ipsum ");
        expectedValues.put("last update", "01/01/2015");

        rowClosure.accept(row, new TransformationContext());
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void should_substring_out_of_bound_1() {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("steps", "Bacon ip");
        values.put("last update", "01/01/2015");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("steps", "Bacon ip");
        expectedValues.put("steps_substring_1", " ip");
        expectedValues.put("last update", "01/01/2015");

        rowClosure.accept(row, new TransformationContext());
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void should_substring_out_of_bound_2() {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("steps", "Bac");
        values.put("last update", "01/01/2015");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("steps", "Bac");
        expectedValues.put("steps_substring_1", "");
        expectedValues.put("last update", "01/01/2015");

        rowClosure.accept(row, new TransformationContext());
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void should_substring_out_of_bound_3() {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("steps", "");
        values.put("last update", "01/01/2015");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("steps", "");
        expectedValues.put("steps_substring_1", "");
        expectedValues.put("last update", "01/01/2015");

        rowClosure.accept(row, new TransformationContext());
        assertEquals(expectedValues, row.values());
    }

    /**
     * @throws IOException
     * @see Split#create(Map)
     */
    @Test
    public void should_substring_strange_bounds_1() throws IOException {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("last update", "01/01/2015");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("steps_substring_1", "");
        expectedValues.put("last update", "01/01/2015");

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                SubstringTest.class.getResourceAsStream("substringAction.json"));

        parameters.put(Substring.FROM_INDEX_PARAMETER, "6");
        parameters.put(Substring.TO_INDEX_PARAMETER, "1");

        Action alternativeAction = this.action.create(parameters);
        BiConsumer<DataSetRow, TransformationContext> alternativeRowClosure = alternativeAction.getRowAction();
        // =====================================================

        alternativeRowClosure.accept(row, new TransformationContext());
        assertEquals(expectedValues, row.values());
    }

    /**
     * @throws IOException
     * @see Split#create(Map)
     */
    @Test
    public void should_substring_strange_bounds_2() throws IOException {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("last update", "01/01/2015");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("steps_substring_1", "psum dolor amet swine leberkas pork belly");
        expectedValues.put("last update", "01/01/2015");

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                SubstringTest.class.getResourceAsStream("substringAction.json"));

        parameters.put(Substring.FROM_INDEX_PARAMETER, "7");
        parameters.put(Substring.TO_MODE_PARAMETER, "To end");
        parameters.put(Substring.TO_INDEX_PARAMETER, "");

        Action alternativeAction = this.action.create(parameters);
        BiConsumer<DataSetRow, TransformationContext> alternativeRowClosure = alternativeAction.getRowAction();
        // =====================================================

        alternativeRowClosure.accept(row, new TransformationContext());
        assertEquals(expectedValues, row.values());
    }

    /**
     * @throws IOException
     * @see Split#create(Map)
     */
    @Test
    public void should_substring_twice() throws IOException {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("last update", "01/01/2015");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("steps_substring_2", "acon ");
        expectedValues.put("steps_substring_1", " ipsum ");
        expectedValues.put("last update", "01/01/2015");

        rowClosure.accept(row, new TransformationContext());

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                SubstringTest.class.getResourceAsStream("substringAction.json"));

        parameters.put(Substring.FROM_INDEX_PARAMETER, "1");
        parameters.put(Substring.TO_INDEX_PARAMETER, "6");

        Action alternativeAction = this.action.create(parameters);
        BiConsumer<DataSetRow, TransformationContext> alternativeRowClosure = alternativeAction.getRowAction();
        // =====================================================

        alternativeRowClosure.accept(row, new TransformationContext());
        assertEquals(expectedValues, row.values());
    }

    /**
     * @throws IOException
     * @see Split#create(Map)
     */
    @Test
    public void should_substring_begining() throws IOException {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("last update", "01/01/2015");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("steps_substring_1", "Bacon ipsum ");
        expectedValues.put("last update", "01/01/2015");

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                SubstringTest.class.getResourceAsStream("substringAction.json"));

        parameters.put(Substring.FROM_MODE_PARAMETER, "From begining");

        Action alternativeAction = this.action.create(parameters);
        BiConsumer<DataSetRow, TransformationContext> alternativeRowClosure = alternativeAction.getRowAction();
        // =====================================================

        alternativeRowClosure.accept(row, new TransformationContext());
        assertEquals(expectedValues, row.values());
    }

    /**
     * @throws IOException
     * @see Split#create(Map)
     */
    @Test
    public void should_substring_end() throws IOException {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("last update", "01/01/2015");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("steps_substring_1", " ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("last update", "01/01/2015");

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                SubstringTest.class.getResourceAsStream("substringAction.json"));

        parameters.put(Substring.TO_MODE_PARAMETER, "To end");

        Action alternativeAction = this.action.create(parameters);
        BiConsumer<DataSetRow, TransformationContext> alternativeRowClosure = alternativeAction.getRowAction();
        // =====================================================

        alternativeRowClosure.accept(row, new TransformationContext());
        assertEquals(expectedValues, row.values());
    }

    /**
     * @throws IOException
     * @see Split#create(Map)
     */
    @Test
    public void should_substring_the_new_substring() throws IOException {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("last update", "01/01/2015");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("steps", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("steps_substring_1", " ipsum ");
        expectedValues.put("steps_substring_1_substring_1", "ips");
        expectedValues.put("last update", "01/01/2015");

        rowClosure.accept(row, new TransformationContext());

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                SubstringTest.class.getResourceAsStream("substringAction.json"));

        parameters.put(Substring.COLUMN_ID, "steps_substring_1");
        parameters.put(Substring.FROM_INDEX_PARAMETER, "1");
        parameters.put(Substring.TO_INDEX_PARAMETER, "4");

        Action alternativeAction = this.action.create(parameters);
        BiConsumer<DataSetRow, TransformationContext> alternativeRowClosure = alternativeAction.getRowAction();
        // =====================================================

        alternativeRowClosure.accept(row, new TransformationContext());
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Action#getMetadataAction()
     */
    @Test
    public void should_update_metadata() {

        List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("recipe", "recipe"));
        input.add(createMetadata("steps", "steps"));
        input.add(createMetadata("last update", "last update"));
        RowMetadata rowMetadata = new RowMetadata(input);

        metadataClosure.accept(rowMetadata, new TransformationContext());
        List<ColumnMetadata> actual = rowMetadata.getColumns();

        List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("recipe", "recipe"));
        expected.add(createMetadata("steps", "steps"));
        expected.add(createMetadata("steps_substring_1", "steps_substring_1"));
        expected.add(createMetadata("last update", "last update"));

        assertEquals(expected, actual);
    }

    /**
     * @see Action#getMetadataAction()
     */
    @Test
    public void should_update_metadata_twice() {

        List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("recipe", "recipe"));
        input.add(createMetadata("steps", "steps"));
        input.add(createMetadata("last update", "last update"));
        RowMetadata rowMetadata = new RowMetadata(input);

        metadataClosure.accept(rowMetadata, new TransformationContext());
        metadataClosure.accept(rowMetadata, new TransformationContext());

        List<ColumnMetadata> actual = rowMetadata.getColumns();

        List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("recipe", "recipe"));
        expected.add(createMetadata("steps", "steps"));
        expected.add(createMetadata("steps_substring_2", "steps_substring_2"));
        expected.add(createMetadata("steps_substring_1", "steps_substring_1"));
        expected.add(createMetadata("last update", "last update"));

        assertEquals(expected, actual);
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.accept(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.accept(getColumn(Type.NUMERIC)));
        assertFalse(action.accept(getColumn(Type.FLOAT)));
        assertFalse(action.accept(getColumn(Type.DATE)));
        assertFalse(action.accept(getColumn(Type.BOOLEAN)));
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
