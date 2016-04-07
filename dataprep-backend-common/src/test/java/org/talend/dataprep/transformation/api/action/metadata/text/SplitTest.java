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
package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getRow;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.parameters.Parameter;

/**
 * Test class for Split action. Creates one consumer, and test it.
 *
 * @see Split
 */
public class SplitTest extends AbstractMetadataBaseTest {

    /**
     * The action to test.
     */
    @Autowired
    private Split action;

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
        final List<Parameter> parameters = action.getParameters();
        assertEquals(6, parameters.size());
        assertEquals(1L, parameters.stream().filter(p -> StringUtils.equals(Split.LIMIT, p.getName())).count());
        final Optional<Parameter> separatorParameter = parameters.stream() //
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
        assertThat(action.getCategory(), is(ActionCategory.SPLIT.getDisplayName()));
    }

    @Test
    public void should_split_row() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon ipsum dolor amet swine leberkas pork belly", "01/01/2015");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_split_underscore() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon_ipsum", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, "other (string)");
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_STRING, "_");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon_ipsum");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "ipsum");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_split_tab() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon\tipsum", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, "other (string)");
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_STRING, "\t");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon\tipsum");
        expectedValues.put("0003", "Bacon");
        expectedValues.put("0004", "ipsum");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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

        parameters.put(Split.SEPARATOR_PARAMETER, "other (string)");
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_STRING, "");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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

        parameters.put(Split.SEPARATOR_PARAMETER, "other (regex)");
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_STRING, "(");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());
    }

    @Test
    public void test_string_that_looks_like_a_regex() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Je vais bien (tout va bien)", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, "other (string)");
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_STRING, "(");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Je vais bien (tout va bien)");
        expectedValues.put("0002", "01/01/2015");
        expectedValues.put("0003", "Je vais bien ");
        expectedValues.put("0004", "tout va bien)");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_split_on_regex() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Je vais bien (tout va bien)", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, "other (regex)");
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_REGEX, "bien");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Je vais bien (tout va bien)");
        expectedValues.put("0003", "Je vais ");
        expectedValues.put("0004", " (tout va bien)");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_split_on_regex2() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Je vais bien (tout va bien)", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, "other (regex)");
        parameters.put(Split.MANUAL_SEPARATOR_PARAMETER_REGEX, "bien|fff");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Je vais bien (tout va bien)");
        expectedValues.put("0003", "Je vais ");
        expectedValues.put("0004", " (tout va bien)");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    /**
     * @see SplitTest#should_split_row()
     */
    public void test_TDP_876() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);



        // when
        ActionTestWorkbench.test(Collections.singletonList(row), //
                c -> analyzerService.schemaAnalysis(c), // Test requires some analysis in asserts
                c -> analyzerService.full(c), // Test requires some analysis in asserts
                factory.create(action, parameters));

        // then
        Statistics originalStats = row.getRowMetadata().getById("0001").getStatistics();
        final List<PatternFrequency> originalPatterns = originalStats.getPatternFrequencies();

        assertFalse(originalPatterns.equals(row.getRowMetadata().getById("0003").getStatistics().getPatternFrequencies()));
        assertFalse(originalPatterns.equals(row.getRowMetadata().getById("0004").getStatistics().getPatternFrequencies()));
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
        ActionTestWorkbench.test(row, factory.create(action, parameters), factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Action#getRowAction()
     */
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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Action#getRowAction()
     */
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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Action#getRowAction()
     */
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
        ActionTestWorkbench.test(rowMetadata, factory.create(action, parameters));

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    /**
     * @see Action#getRowAction()
     */
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
        ActionTestWorkbench.test(rowMetadata, factory.create(action, parameters), factory.create(action, parameters));

        assertEquals(expected, rowMetadata.getColumns());
    }

    @Test
    public void should_not_split_separator_not_found() throws IOException {
        // given
        final DataSetRow row = getRow("lorem bacon", "Bacon ipsum dolor amet swine leberkas pork belly", "01/01/2015");

        parameters.put(Split.SEPARATOR_PARAMETER, "-");
        parameters.put(Split.LIMIT, "4");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());
    }

    public void should_not_update_metadata_because_null_separator() throws IOException {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        parameters.put(Split.SEPARATOR_PARAMETER, "");

        // when
        ActionTestWorkbench.test(rowMetadata, factory.create(action, parameters));

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

    @Test
    public void should_have_separator_that_could_be_blank() {
        Optional<Parameter> parameter = new Split().getParameters().stream()
                .filter(p -> StringUtils.equals(p.getName(), Split.SEPARATOR_PARAMETER)).findFirst();
        if (parameter.isPresent()) {
            assertTrue(parameter.get().isCanBeBlank());
        } else {
            fail();
        }
    }

    /**
     * @param name name of the column metadata to create.
     * @return a new column metadata
     */
    protected ColumnMetadata createMetadata(String id, String name) {
        return ColumnMetadata.Builder.column().computedId(id).name(name).type(Type.STRING).headerSize(12).empty(0).invalid(2)
                .valid(5).build();
    }

}
