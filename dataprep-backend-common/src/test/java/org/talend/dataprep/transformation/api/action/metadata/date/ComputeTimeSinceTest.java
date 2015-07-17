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

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.ComputeLength;
import org.talend.dataprep.transformation.api.action.metadata.Split;
import org.talend.dataprep.transformation.api.action.metadata.Substring;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

/**
 * Test class for Split action. Creates one consumer, and test it.
 *
 * @see Split
 */
public class ComputeTimeSinceTest {

    /**
     * The row consumer to test.
     */
    private BiConsumer<DataSetRow, TransformationContext> rowClosure;

    /**
     * The metadata consumer to test.
     */
    private BiConsumer<RowMetadata, TransformationContext> metadataClosure;

    /**
     * The action to test.
     */
    private ComputeTimeSince action;

    /**
     * Constructor.
     */
    public ComputeTimeSinceTest() throws IOException {
        action = new ComputeTimeSince();
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                ComputeTimeSinceTest.class.getResourceAsStream("computeTimeSinceAction.json"));
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
    public void should_compute() {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("002", "01/01/2010");
        values.put("steps", "Bacon");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("002", "01/01/2010");
        expectedValues.put("since_002_in_years", "5");
        expectedValues.put("steps", "Bacon");

        TransformationContext context = new TransformationContext();
        context.put(ComputeTimeSince.PATTERN, "MM/dd/yyyy");
        context.put(ComputeTimeSince.NOW, LocalDate.of(2015, 7, 16));

        rowClosure.accept(row, context);
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void should_compute_days() throws IOException {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("002", "06/15/2015");
        values.put("steps", "Bacon");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("002", "06/15/2015");
        expectedValues.put("since_002_in_days", "31");
        expectedValues.put("steps", "Bacon");

        TransformationContext context = new TransformationContext();
        context.put(ComputeTimeSince.PATTERN, "MM/dd/yyyy");
        context.put(ComputeTimeSince.NOW, LocalDate.of(2015, 7, 16));

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                ComputeTimeSince.class.getResourceAsStream("computeTimeSinceAction.json"));

        parameters.put(ComputeTimeSince.TIME_UNIT_PARAMETER, "Days");

        Action alternativeAction = this.action.create(parameters);
        BiConsumer<DataSetRow, TransformationContext> alternativeRowClosure = alternativeAction.getRowAction();
        // =====================================================

        alternativeRowClosure.accept(row, context);
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_compute_hours() throws IOException {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("002", "07/16/2015 13:00");
        values.put("steps", "Bacon");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("002", "07/16/2015 13:00");
        expectedValues.put("since_002_in_hours", "5");
        expectedValues.put("steps", "Bacon");

        TransformationContext context = new TransformationContext();
        context.put(ComputeTimeSince.PATTERN, "MM/dd/yyyy HH:mm");
        context.put(ComputeTimeSince.NOW, LocalDateTime.of(2015, 7, 16, 18, 0));

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                ComputeTimeSince.class.getResourceAsStream("computeTimeSinceAction.json"));

        parameters.put(ComputeTimeSince.TIME_UNIT_PARAMETER, "Hours");

        Action alternativeAction = this.action.create(parameters);
        BiConsumer<DataSetRow, TransformationContext> alternativeRowClosure = alternativeAction.getRowAction();
        // =====================================================

        alternativeRowClosure.accept(row, context);
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_compute_hours_twice() throws IOException {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("002", "07/16/2015 13:00");
        values.put("steps", "Bacon");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("002", "07/16/2015 13:00");
        expectedValues.put("since_002_in_hours", "5");
        expectedValues.put("steps", "Bacon");

        TransformationContext context = new TransformationContext();
        context.put(ComputeTimeSince.PATTERN, "MM/dd/yyyy HH:mm");
        context.put(ComputeTimeSince.NOW, LocalDateTime.of(2015, 7, 16, 18, 0));

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                ComputeTimeSince.class.getResourceAsStream("computeTimeSinceAction.json"));

        parameters.put(ComputeTimeSince.TIME_UNIT_PARAMETER, "Hours");

        Action alternativeAction = this.action.create(parameters);
        BiConsumer<DataSetRow, TransformationContext> alternativeRowClosure = alternativeAction.getRowAction();
        // =====================================================

        alternativeRowClosure.accept(row, context);
        alternativeRowClosure.accept(row, context);
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_compute_twice_diff_units() throws IOException {
        Map<String, String> values = new HashMap<>();
        values.put("recipe", "lorem bacon");
        values.put("002", "07/16/2015 12:00");
        values.put("steps", "Bacon");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("recipe", "lorem bacon");
        expectedValues.put("002", "07/16/2015 12:00");
        expectedValues.put("since_002_in_hours", "25");
        expectedValues.put("since_002_in_days", "1");
        expectedValues.put("steps", "Bacon");

        TransformationContext context = new TransformationContext();
        context.put(ComputeTimeSince.PATTERN, "MM/dd/yyyy HH:mm");
        context.put(ComputeTimeSince.NOW, LocalDateTime.of(2015, 7, 17, 13, 0));

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                ComputeTimeSince.class.getResourceAsStream("computeTimeSinceAction.json"));

        parameters.put(ComputeTimeSince.TIME_UNIT_PARAMETER, "Days");

        Action alternativeAction = this.action.create(parameters);
        BiConsumer<DataSetRow, TransformationContext> alternativeRowClosure = alternativeAction.getRowAction();
        // =====================================================
        alternativeRowClosure.accept(row, context);

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        parameters.put(ComputeTimeSince.TIME_UNIT_PARAMETER, "Hours");

        alternativeAction = this.action.create(parameters);
        alternativeRowClosure = alternativeAction.getRowAction();
        // =====================================================

        alternativeRowClosure.accept(row, context);
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Action#getMetadataAction()
     */
    @Test
    public void should_update_metadata() throws IOException {

        List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("recipe", "recipe"));
        input.add(createMetadata("002", "last update"));
        input.add(createMetadata("steps", "steps"));
        RowMetadata rowMetadata = new RowMetadata(input);

        String statistics = IOUtils.toString(ComputeTimeSinceTest.class.getResourceAsStream("statistics.json"));
        input.get(1).setStatistics(statistics);

        TransformationContext context = new TransformationContext();
        context.put(ComputeTimeSince.PATTERN, "MM/dd/yyyy");

        metadataClosure.accept(rowMetadata, context);

        List<ColumnMetadata> actual = rowMetadata.getColumns();

        List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("recipe", "recipe"));
        expected.add(createMetadata("002", "last update"));
        expected.add(createMetadata("since_002_in_years", "since_last update_in_years", Type.INTEGER));
        expected.add(createMetadata("steps", "steps"));

        assertEquals(expected, actual);
    }

    /**
     * @see Action#getMetadataAction()
     */
    @Test
    public void should_update_metadata_twice() throws IOException {

        List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("recipe", "recipe"));
        input.add(createMetadata("002", "last update"));
        input.add(createMetadata("steps", "steps"));
        RowMetadata rowMetadata = new RowMetadata(input);

        String statistics = IOUtils.toString(ComputeTimeSinceTest.class.getResourceAsStream("statistics.json"));
        input.get(1).setStatistics(statistics);

        TransformationContext context = new TransformationContext();
        context.put(ComputeTimeSince.PATTERN, "MM/dd/yyyy");

        metadataClosure.accept(rowMetadata, context);
        metadataClosure.accept(rowMetadata, context);
        List<ColumnMetadata> actual = rowMetadata.getColumns();

        List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("recipe", "recipe"));
        expected.add(createMetadata("002", "last update"));
        expected.add(createMetadata("since_002_in_years", "since_last update_in_years", Type.INTEGER));
        expected.add(createMetadata("steps", "steps"));

        assertEquals(expected, actual);
    }

    /**
     * @see Action#getMetadataAction()
     */
    @Test
    public void should_update_metadata_twice_diff_units() throws IOException {

        List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("recipe", "recipe"));
        input.add(createMetadata("002", "last update"));
        input.add(createMetadata("steps", "steps"));
        RowMetadata rowMetadata = new RowMetadata(input);

        String statistics = IOUtils.toString(ComputeTimeSinceTest.class.getResourceAsStream("statistics.json"));
        input.get(1).setStatistics(statistics);

        TransformationContext context = new TransformationContext();
        context.put(ComputeTimeSince.PATTERN, "MM/dd/yyyy");

        metadataClosure.accept(rowMetadata, context);

        // =====================================================
        // Create a new rowClosure with different params:
        // =====================================================
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                ComputeTimeSince.class.getResourceAsStream("computeTimeSinceAction.json"));

        parameters.put(ComputeTimeSince.TIME_UNIT_PARAMETER, "Days");

        Action alternativeAction = this.action.create(parameters);
        BiConsumer<RowMetadata, TransformationContext> alternativeMetadataClosure = alternativeAction.getMetadataAction();
        // =====================================================
        alternativeMetadataClosure.accept(rowMetadata, context);

        List<ColumnMetadata> actual = rowMetadata.getColumns();

        List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("recipe", "recipe"));
        expected.add(createMetadata("002", "last update"));
        expected.add(createMetadata("since_002_in_days", "since_last update_in_days", Type.INTEGER));
        expected.add(createMetadata("since_002_in_years", "since_last update_in_years", Type.INTEGER));
        expected.add(createMetadata("steps", "steps"));

        assertEquals(expected, actual);
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.accept(getColumn(Type.DATE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.accept(getColumn(Type.NUMERIC)));
        assertFalse(action.accept(getColumn(Type.FLOAT)));
        assertFalse(action.accept(getColumn(Type.STRING)));
        assertFalse(action.accept(getColumn(Type.BOOLEAN)));
    }

    private ColumnMetadata createMetadata(String id, String name) {
        return createMetadata(id, name, Type.STRING);
    }

    private ColumnMetadata createMetadata(String id, String name, Type type) {
        return ColumnMetadata.Builder.column().computedId(id).name(name).type(type).headerSize(12).empty(0).invalid(2)
                .valid(5).build();
    }

}
