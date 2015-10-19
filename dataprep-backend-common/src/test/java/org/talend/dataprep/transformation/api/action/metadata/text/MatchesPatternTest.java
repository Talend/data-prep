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
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for Match Pattern action. Creates one consumer, and test it.
 *
 * @see Split
 */
public class MatchesPatternTest {

    /**
     * The action to test.
     */
    private MatchesPattern action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        action = new MatchesPattern();

        parameters = ActionMetadataTestUtils.parseParameters( //
                action, //
                MatchesPatternTest.class.getResourceAsStream("matchesPattern.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS.getDisplayName()));
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void shouldMatchPattern() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon");
        expectedValues.put("0003", "true");
        expectedValues.put("0002", "01/01/2015");

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void shouldOrNotMatchPattern() {
        assertFalse(action.computeNewValue(" ", "[a-zA-Z]*"));
        assertTrue(action.computeNewValue("aA", "[a-zA-Z]*"));

        assertFalse(action.computeNewValue("Ouch !","[a-zA-Z0-9]*"));
        assertTrue(action.computeNewValue("Houba 2 fois", "[a-zA-Z0-9 ]*"));
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void shouldNotMatchPattern() {
        assertFalse(action.computeNewValue(" ", "[a-zA-Z]*"));
        assertFalse(action.computeNewValue("aaaa8", "[a-zA-Z]*"));
        assertFalse(action.computeNewValue(" a8 ", "[a-zA-Z]*"));
        assertFalse(action.computeNewValue("aa:", "[a-zA-Z]*"));
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void shouldMatchOrNotoEmptyString() {
        assertTrue(action.computeNewValue("", ".*"));
        assertTrue(action.computeNewValue("", "[a-zA-Z]*"));
        assertFalse(action.computeNewValue(" ", "[a-zA-Z]*"));
        assertTrue(action.computeNewValue(" ", "[a-zA-Z ]*"));
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void shouldMatchEmptyStringEmptyPattern() {
        assertFalse(action.computeNewValue("", ""));
        assertFalse(action.computeNewValue("  ", ""));
        assertFalse(action.computeNewValue("un petit texte", ""));
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void shouldNotMatchBadPattern() {
        assertFalse(action.computeNewValue("", "*"));
        assertFalse(action.computeNewValue("  ", "*"));
        assertFalse(action.computeNewValue("un petit texte", "*"));
    }

    /**
     * @see Split#create(Map)
     */
    @Test
    public void shouldMatchPatternTwice() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon");
        expectedValues.put("0004", "true");
        expectedValues.put("0003", "true");
        expectedValues.put("0002", "01/01/2015");

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see ComputeLength#create(Map)
     */
    @Test
    public void shouldUpdateMetadata() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0003", "steps_matching", Type.BOOLEAN));
        expected.add(createMetadata("0002", "last update"));

        // when
        action.applyOnColumn(new DataSetRow(rowMetadata), new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    /**
     * @see ComputeLength#create(Map)
     */
    @Test
    public void shouldUpdateMetadataTwice() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0004", "steps_matching", Type.BOOLEAN));
        expected.add(createMetadata("0003", "steps_matching", Type.BOOLEAN));
        expected.add(createMetadata("0002", "last update"));

        // when
        action.applyOnColumn(new DataSetRow(rowMetadata), new TransformationContext(), parameters, "0001");
        action.applyOnColumn(new DataSetRow(rowMetadata), new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    @Test
    public void shouldAcceptColumn() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void shouldNotAcceptColumn() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

    private ColumnMetadata createMetadata(String id, String name) {
        return createMetadata(id, name, Type.BOOLEAN);
    }

    private ColumnMetadata createMetadata(String id, String name, Type type) {
        return ColumnMetadata.Builder.column().computedId(id).name(name).type(type).headerSize(12).empty(0).invalid(2).valid(5)
                .build();
    }

}
