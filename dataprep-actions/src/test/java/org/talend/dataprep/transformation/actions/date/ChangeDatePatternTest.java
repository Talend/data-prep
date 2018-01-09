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

package org.talend.dataprep.transformation.actions.date;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.*;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.CREATE_NEW_COLUMN;

/**
 * Unit test for the ChangeDatePattern action.
 *
 * @see ChangeDatePattern
 */
public class ChangeDatePatternTest extends BaseDateTest<ChangeDatePattern> {

    private Map<String, String> parameters;

    public ChangeDatePatternTest() {
        super(new ChangeDatePattern());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(this.getClass().getResourceAsStream("changeDatePatternAction.json"));
    }

    @Test
    public void testName() throws Exception {
        assertEquals("change_date_pattern", action.getName());
    }

    @Test
    public void testParameters() throws Exception {
        // 4 predefined patterns + custom = 6
        assertThat(action.getParameters(Locale.US).size(), is(7));

        // Test on items label for TDP-2944:
        final SelectParameter newFormatParam = (SelectParameter) action.getParameters(Locale.US).get(6);
        assertEquals("American standard", newFormatParam.getItems().get(0).getLabel());
    }

    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
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


    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.DATE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(3, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CHANGE_TYPE));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.NEED_STATISTICS_PATTERN));
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_check_column_id_parameter_when_dealing_with_row_metadata() {
        //given
        Map<String, String> missingParameters = new HashMap<>();
        missingParameters.put("column_id", "");

        //when
        ActionTestWorkbench.test(new DataSetRow(Collections.emptyMap()), actionRegistry, factory.create(action, missingParameters));
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_check_new_pattern_parameter_when_dealing_with_row_metadata() {
        //given
        Map<String, String> missingParameters = new HashMap<>();
        missingParameters.put("column_id", "0000");
        missingParameters.put(ChangeDatePattern.NEW_PATTERN, "toto");

        //when
        ActionTestWorkbench.test(new DataSetRow(Collections.emptyMap()), actionRegistry, factory.create(action, missingParameters));
    }

    @Test(expected = TalendRuntimeException.class)
    public void should_check_new_pattern_parameter_when_dealing_with_row() {
        //given
        final Map<String, String> insufficientParams = new HashMap<>();
        insufficientParams.put("column_id", "0000");

        //when
        ActionTestWorkbench.test(new DataSetRow(Collections.emptyMap()), actionRegistry, factory.create(action, insufficientParams));
    }

    @Test
    public void test_apply_in_newcolumn() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING).name("recipe")) //
                .with(value("04/25/1999").type(Type.STRING).name("recipe").statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .with(value("tata").type(Type.DATE).name("last update")) //
                .build();
        parameters.put(CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "04/25/1999", "tata", "25 - Apr - 1999");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_apply_in_newcolumn_with_empty_values() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING).name("recipe")) //
                .with(value("").type(Type.STRING).name("recipe").statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .with(value("tata").type(Type.DATE).name("last update")) //
                .build();
        parameters.put(CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "", "tata", "");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING).name("recipe")) //
                .with(value("04/25/1999").type(Type.STRING).name("recipe").statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .with(value("tata").type(Type.DATE).name("last update")) //
                .build();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "25 - Apr - 1999", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_1108_invalid_pattern() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "04/25/1999", "tata");
        setStatistics(row, "0001", getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"));
        parameters.put(ChangeDatePattern.NEW_PATTERN, "custom");
        parameters.put(ChangeDatePattern.CUSTOM_PATTERN, "ff");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "04/25/1999", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_1108_invalid_pattern_newcolumn() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "04/25/1999", "tata");
        setStatistics(row, "0001", getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"));
        parameters.put(ChangeDatePattern.NEW_PATTERN, "custom");
        parameters.put(ChangeDatePattern.CUSTOM_PATTERN, "ff");
        parameters.put(CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "04/25/1999", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_1108_empty_pattern() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "04/25/1999", "tata");
        setStatistics(row, "0001", getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"));
        parameters.put(ChangeDatePattern.NEW_PATTERN, "custom");
        parameters.put(ChangeDatePattern.CUSTOM_PATTERN, "");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "04/25/1999", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_set_new_pattern_as_most_used_one() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING).name("recipe")) //
                .with(value("04/25/1999").type(Type.STRING).name("recipe").statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .with(value("tata").type(Type.DATE).name("last update")) //
                .build();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final List<PatternFrequency> patternFrequencies = row.getRowMetadata() //
                .getById("0001") //
                .getStatistics() //
                .getPatternFrequencies();

        String newPattern = parameters.get("new_pattern");
        final Optional<PatternFrequency> newPatternSet = patternFrequencies //
                .stream() //
                .filter(p -> StringUtils.equals(newPattern, p.getPattern())) //
                .findFirst();

        assertTrue(newPatternSet.isPresent());
        assertEquals(newPatternSet.get().getOccurrences(), 48);
    }

    @Test
    public void should_set_new_pattern_as_most_used_one_newcolumn() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING).name("recipe")) //
                .with(value("04/25/1999").type(Type.STRING).name("recipe").statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .with(value("tata").type(Type.DATE).name("last update")) //
                .build();
        parameters.put(CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final List<PatternFrequency> patternFrequencies = row.getRowMetadata() //
                .getById("0003") //
                .getStatistics() //
                .getPatternFrequencies();

        String newPattern = parameters.get("new_pattern");
        final Optional<PatternFrequency> newPatternSet = patternFrequencies //
                .stream() //
                .filter(p -> StringUtils.equals(newPattern, p.getPattern())) //
                .findFirst();

        assertTrue(newPatternSet.isPresent());
        assertEquals(newPatternSet.get().getOccurrences(), 48);
    }

    @Test
    public void should_process_row_when_value_does_not_match_most_frequent_pattern() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING).name("recipe")) //
                .with(value("04-25-09").type(Type.STRING).name("recipe").statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .with(value("tata").type(Type.DATE).name("last update")) //
                .build();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "25 - Apr - 2009", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_when_value_does_not_match_any_pattern() throws Exception {
        // given
        DataSetRow row = getRow("toto", "NA", "tata");
        setStatistics(row, "0001", getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "NA", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_with_no_pattern_mode() throws Exception {
        // given
        DataSetRow row = getRow("toto", "Apr-25-09", "tata");
        setStatistics(row, "0001", getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"));

        parameters.put(ChangeDatePattern.FROM_MODE, "test");
        parameters.put(ChangeDatePattern.FROM_CUSTOM_PATTERN, "MMM-dd-yy");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "Apr-25-09", "tata");
        assertEquals(expectedRow.values(), row.values());
    }


    /**
     * @see <a href="https://jira.talendforge.org/browse/TDP-1657">Jira TDP-1657</a>
     */
    @Test
    public void should_process_row_with_user_set_pattern_TDP_1657() throws Exception {
        // given
        DataSetRow row = getRow("toto", "Apr-25-09", "tata");
        setStatistics(row, "0001", getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"));

        parameters.put(ChangeDatePattern.FROM_MODE, ChangeDatePattern.FROM_MODE_CUSTOM);
        parameters.put(ChangeDatePattern.FROM_CUSTOM_PATTERN, "MMM-dd-yy");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "25 - Apr - 2009", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_2480() throws Exception {
        // given
        DataSetRow row = getRow("toto", "APR-25-09", "tata");
        setStatistics(row, "0001", getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"));

        parameters.put(ChangeDatePattern.FROM_MODE, ChangeDatePattern.FROM_MODE_CUSTOM);
        parameters.put(ChangeDatePattern.FROM_CUSTOM_PATTERN, "MMM-dd-yy");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "25 - Apr - 2009", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_2636() throws Exception {
        // given
        long rowId = 120;

        // row 1
        final DataSetRow row1 = builder() //
                .with(value("David").type(Type.STRING).name("recipe")) //
                .with(value("6/12/2015 0:01").type(Type.STRING).name("recipe").statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .build();

        // row 2
        final DataSetRow row2 = builder() //
                .with(value("David").type(Type.STRING).name("recipe")) //
                .with(value("12/14/2015 1:18 AM").type(Type.STRING).name("recipe").statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .build();

        // row 3
        final DataSetRow row3 = builder() //
                .with(value("James").type(Type.STRING).name("recipe")) //
                .with(value("9-oct-2016").type(Type.STRING).name("recipe").statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .build();

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ChangeDatePattern.FROM_MODE, ChangeDatePattern.FROM_MODE_BEST_GUESS);
        parameters.put(ChangeDatePattern.NEW_PATTERN, "yyyy-MM-dd");
        parameters.put("column_id", "0001");

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("2015-06-12", row1.get("0001"));
        assertEquals("2015-12-14", row2.get("0001"));
        assertEquals("2016-10-09", row3.get("0001"));
    }

    @Test
    public void test_TDP_2255_not_working_with_timezone_in_format() throws Exception {
        // given
        DataSetRow row = getRow("toto", "Apr-25-09", "tata");
        setStatistics(row, "0001", getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"));

        parameters.put(ChangeDatePattern.FROM_MODE, ChangeDatePattern.FROM_MODE_CUSTOM);
        parameters.put(ChangeDatePattern.FROM_CUSTOM_PATTERN, "MMM-dd-yy");

        parameters.put(ChangeDatePattern.NEW_PATTERN, "custom");
        parameters.put(ChangeDatePattern.CUSTOM_PATTERN, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "Apr-25-09", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_2255_should_work_without_timezone_in_format() throws Exception {
        // given
        DataSetRow row = getRow("toto", "Apr-25-09", "tata");
        setStatistics(row, "0001", getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"));

        parameters.put(ChangeDatePattern.FROM_MODE, ChangeDatePattern.FROM_MODE_CUSTOM);
        parameters.put(ChangeDatePattern.FROM_CUSTOM_PATTERN, "MMM-dd-yy");

        parameters.put(ChangeDatePattern.NEW_PATTERN, "custom");
        parameters.put(ChangeDatePattern.CUSTOM_PATTERN, "yyyy-MM-dd'T'HH:mm:ss.SSS");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "2009-04-25T00:00:00.000", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_when_value_is_empty() throws Exception {
        // given
        DataSetRow row = getRow("toto", "", "tata");
        setStatistics(row, "0001", getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then (values should be unchanged)
        final DataSetRow expectedRow = getRow("toto", "", "tata");
        assertEquals(expectedRow.values(), row.values());
    }


}
