//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================
package org.talend.dataprep.transformation.actions.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;

/**
 * Test class for Match Pattern action. Creates one consumer, and test it.
 *
 * @see Split
 */
public class MatchesPatternTest extends AbstractMetadataBaseTest<MatchesPattern> {

    private Map<String, String> parameters;

    public MatchesPatternTest() {
        super(new MatchesPattern());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(MatchesPatternTest.class.getResourceAsStream("matchesPattern.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.STRINGS.getDisplayName(Locale.US)));
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.INVISIBLE_ENABLED;
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // Nothing to test, this action is never applied in place
    }

    @Test
    public void test_apply_in_newcolumn() {
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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void shouldMatchPattern_starts_with() {
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

        parameters.put(MatchesPattern.PATTERN_PARAMETER, "custom");
        parameters.put(MatchesPattern.MANUAL_PATTERN_PARAMETER, generateJson("Bac", "starts_with"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * Test with an invalid regex pattern as token and mode is not REGEX.
     */
    @Test
    public void shouldMatchPattern_contains_invalid_regex() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Ba(con");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Ba(con");
        expectedValues.put("0003", "true");
        expectedValues.put("0002", "01/01/2015");

        parameters.put(MatchesPattern.PATTERN_PARAMETER, "custom");
        parameters.put(MatchesPattern.MANUAL_PATTERN_PARAMETER, generateJson("(", "contains"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void shouldNotMatchPattern_starts_with() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon");
        expectedValues.put("0003", "false");
        expectedValues.put("0002", "01/01/2015");

        parameters.put(MatchesPattern.PATTERN_PARAMETER, "custom");
        parameters.put(MatchesPattern.MANUAL_PATTERN_PARAMETER, generateJson("Bak", "starts_with"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void shouldOrNotMatchPattern() {
        assertFalse(action.computeNewValue(" ", buildPatternActionContext("[a-zA-Z]+")));
        assertTrue(action.computeNewValue("aA", buildPatternActionContext("[a-zA-Z]+")));

        assertFalse(action.computeNewValue("Ouch !", buildPatternActionContext("[a-zA-Z0-9]*")));
        assertTrue(action.computeNewValue("Houba 2 fois", buildPatternActionContext("[a-zA-Z0-9 ]*")));
    }

    @Test
    public void shouldNotMatchPattern() {
        assertFalse(action.computeNewValue(" ", buildPatternActionContext("[a-zA-Z]+")));
        assertFalse(action.computeNewValue("aaaa8", buildPatternActionContext("[a-zA-Z]*")));
        assertFalse(action.computeNewValue(" a8 ", buildPatternActionContext("[a-zA-Z]*")));
        assertFalse(action.computeNewValue("aa:", buildPatternActionContext("[a-zA-Z]*")));
    }

    @Test
    public void shouldMatchOrNotoEmptyString() {
        assertTrue(action.computeNewValue("", buildPatternActionContext(".*")));
        assertTrue(action.computeNewValue("", buildPatternActionContext("[a-zA-Z]*")));
        assertFalse(action.computeNewValue(" ", buildPatternActionContext("[a-zA-Z]+")));
        assertTrue(action.computeNewValue(" ", buildPatternActionContext("[a-zA-Z ]+")));
    }

    private ActionContext buildPatternActionContext(String regex) {
        ActionContext context = new ActionContext(new TransformationContext());
        // create and add a single column
        RowMetadata rowMetadata = new RowMetadata();
        ColumnMetadata column = new ColumnMetadata();
        column.setId("0000");
        column.setName("toto");
        column.setType(Type.STRING.name());
        rowMetadata.setColumns(Collections.singletonList(column));
        context.setRowMetadata(rowMetadata);

        // create and add minimalist parameters
        Map<String, String> parameters = new HashMap<>();
        parameters.put(MatchesPattern.PATTERN_PARAMETER, regex);
        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), "0000");
        context.setParameters(parameters);

        action.compile(context);
        return context;
    }

    @Test
    public void shouldMatchEmptyStringEmptyPattern() {
        assertFalse(action.computeNewValue("", buildPatternActionContext("")));
        assertFalse(action.computeNewValue("  ", buildPatternActionContext("")));
        assertFalse(action.computeNewValue("un petit texte", buildPatternActionContext("")));
    }

    @Test
    public void shouldNotMatchBadPattern() {
        assertFalse(action.computeNewValue("", buildPatternActionContext("*")));
        assertFalse(action.computeNewValue("  ", buildPatternActionContext("*")));
        assertFalse(action.computeNewValue("un petit texte", buildPatternActionContext("*")));
    }

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters), factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

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
        ActionTestWorkbench.test(rowMetadata, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

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
        ActionTestWorkbench.test(rowMetadata, actionRegistry, factory.create(action, parameters), factory.create(action, parameters));

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    @Test
    public void shouldAcceptColumn() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void shouldNotAcceptColumn() {
        assertFalse(action.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

    protected ColumnMetadata createMetadata(String id, String name) {
        return createMetadata(id, name, Type.BOOLEAN);
    }

    protected ColumnMetadata createMetadata(String id, String name, Type type) {
        return ColumnMetadata.Builder.column().computedId(id).name(name).type(type).headerSize(12).empty(0).invalid(2).valid(5)
                .build();
    }



}
