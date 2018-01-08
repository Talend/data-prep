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
package org.talend.dataprep.transformation.actions.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for Split action. Creates one consumer, and test it.
 *
 * @see Split
 */
public class SplitTest extends AbstractMetadataBaseTest<Split> {

    /**
     * The action to test.
     */
    private final Split action = new Split();

    public SplitTest() {
        super(new Split());
    }

    /** The action parameters. */
    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(SplitTest.class.getResourceAsStream("splitAction.json"));
    }

    @Test
    public void testName() throws Exception {
        assertEquals("split", action.getName());
    }

    @Test
    public void testParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(Locale.US);
        assertEquals(6, parameters.size());
        assertEquals(1L, parameters.stream().filter(p -> StringUtils.equals(Split.LIMIT, p.getName())).count());
        final Optional<Parameter> separatorParameter = parameters
                .stream() //
                .filter(p -> StringUtils.equals(Split.SEPARATOR_PARAMETER, p.getName())) //
                .findFirst();
        assertTrue(separatorParameter.isPresent());
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.SPLIT.getDisplayName(Locale.US)));
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon ipsum dolor amet swine leberkas pork belly", "01/01/2015");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_split_semicolon() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon;ipsum", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, ";");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon;ipsum");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "ipsum");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_split_underscore() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon_ipsum", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, Split.OTHER_STRING);
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_STRING, "_");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon_ipsum");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "ipsum");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_split_tab() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon\tipsum", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, Split.OTHER_STRING);
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_STRING, "\t");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon\tipsum");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "ipsum");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_TDP_786_empty_pattern() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Je vais bien (tout va bien)");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(Split.SEPARATOR_PARAMETER, Split.OTHER_STRING);
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_STRING, "");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());
    }

    @Test
    public void test_TDP_831_invalid_pattern() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Je vais bien (tout va bien)");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(Split.SEPARATOR_PARAMETER, Split.OTHER_REGEX);
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_STRING, "(");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());
    }

    @Test
    public void test_string_that_looks_like_a_regex() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Je vais bien (tout va bien)", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, Split.OTHER_STRING);
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_STRING, "(");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Je vais bien (tout va bien)");
        expectedValues.put("0002", "01/01/2015");
        expectedValues.put("0003", "Je vais bien ");
        expectedValues.put("0004", "tout va bien)");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_split_on_regex() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Je vais bien (tout va bien)", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, Split.OTHER_REGEX);
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_REGEX, "bien");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Je vais bien (tout va bien)");
        expectedValues.put("0003", "Je vais ");
        expectedValues.put("0004", " (tout va bien)");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_split_on_regex2() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Je vais bien (tout va bien)", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, Split.OTHER_REGEX);
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_REGEX, "bien|fff");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Je vais bien (tout va bien)");
        expectedValues.put("0003", "Je vais ");
        expectedValues.put("0004", " (tout va bien)");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_TDP_876() {
        // given
        final DataSetRow row = builder() //
                .with(value("lorem bacon").type(Type.STRING)) //
                .with(value("Bacon ipsum dolor amet swine leberkas pork belly").type(Type.STRING)) //
                .with(value("01/01/2015").type(Type.STRING)) //
                .build();

        // when
        ActionTestWorkbench.test(Collections.singletonList(row), //
                analyzerService, // Test requires some analysis in asserts
                actionRegistry, factory.create(action, parameters));

        // then
        final RowMetadata actual = row.getRowMetadata();
        Statistics originalStats = actual.getById("0001").getStatistics();
        final List<PatternFrequency> originalPatterns = originalStats.getPatternFrequencies();

        assertFalse(originalPatterns.equals(actual.getById("0003").getStatistics().getPatternFrequencies()));
        assertFalse(originalPatterns.equals(actual.getById("0004").getStatistics().getPatternFrequencies()));
    }

    @Test
    public void should_split_row_twice() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon ipsum dolor amet swine leberkas pork belly", "01/01/2015");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0005", "Bacon");
        expectedValues.put("0006", "ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters), factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_split_row_with_separator_at_the_end() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon ", "01/01/2015");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_split_row_no_separator() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon", "01/01/2015");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_update_metadata() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0003", "steps_split_1"));
        expected.add(createMetadata("0004", "steps_split_2"));
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
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0005", "steps_split_1"));
        expected.add(createMetadata("0006", "steps_split_2"));
        expected.add(createMetadata("0003", "steps_split_1"));
        expected.add(createMetadata("0004", "steps_split_2"));
        expected.add(createMetadata("0002", "last update"));

        // when
        ActionTestWorkbench.test(rowMetadata, actionRegistry, factory.create(action, parameters),
                factory.create(action, parameters));

        assertEquals(expected, rowMetadata.getColumns());
    }

    @Test
    public void should_not_split_separator_not_found() throws IOException {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon ipsum dolor amet swine leberkas pork belly", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, "-");
        parameters.put(Split.LIMIT, "4");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0004", "");
        expectedValues.put("0005", "");
        expectedValues.put("0006", "");
        expectedValues.put("0002", "01/01/2015");

        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_split_because_null_separator() throws IOException {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(Split.SEPARATOR_PARAMETER, "");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());
    }

    @Test
    public void should_not_update_metadata_because_null_separator() throws IOException {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        parameters.put(Split.SEPARATOR_PARAMETER, "");

        // when
        ActionTestWorkbench.test(rowMetadata, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(rowMetadata, rowMetadata);
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_separator_that_could_be_blank() {
        Optional<Parameter> parameter = new Split()
                .getParameters(Locale.US)
                .stream()
                .filter(p -> StringUtils.equals(p.getName(), Split.SEPARATOR_PARAMETER))
                .findFirst();
        if (parameter.isPresent()) {
            assertTrue(parameter.get().isCanBeBlank());
        } else {
            fail();
        }
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

    /**
     * @param name name of the column metadata to create.
     * @return a new column metadata
     */
    protected ColumnMetadata createMetadata(String id, String name) {
        return ColumnMetadata.Builder
                .column()
                .computedId(id)
                .name(name)
                .type(Type.STRING)
                .headerSize(12)
                .empty(0)
                .invalid(2)
                .valid(5)
                .build();
    }

    @Test
    public void test_apply_inplace() {
        // Nothing to test, this action is never applied in place
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.INVISIBLE_ENABLED;
    }

}
