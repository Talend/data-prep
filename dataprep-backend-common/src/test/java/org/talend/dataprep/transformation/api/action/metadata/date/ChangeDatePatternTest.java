package org.talend.dataprep.transformation.api.action.metadata.date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.*;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Unit test for the ChangeDatePattern action.
 *
 * @see ChangeDatePattern
 */
public class ChangeDatePatternTest extends BaseDateTests {

    /** The action to test. */
    @Autowired
    private ChangeDatePattern action;

    private Map<String, String> parameters;

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
        // 4 predefined patterns + custom = 5
        assertThat(action.getParameters().size(), is(5));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATE.getDisplayName()));
    }

    @Test(expected = TDPException.class)
    public void should_check_column_id_parameter_when_dealing_with_row_metadata() {
        //given
        Map<String, String> missingParameters = new HashMap<>();
        missingParameters.put("column_id", "");

        //when
        ActionTestWorkbench.test(new DataSetRow(Collections.emptyMap()), action.create(missingParameters).getRowAction());
    }

    @Test(expected = TDPException.class)
    public void should_check_new_pattern_parameter_when_dealing_with_row_metadata() {
        //given
        Map<String, String> missingParameters = new HashMap<>();
        missingParameters.put("column_id", "0000");
        missingParameters.put(ChangeDatePattern.NEW_PATTERN, "toto");

        //when
        ActionTestWorkbench.test(new DataSetRow(Collections.emptyMap()), action.create(missingParameters).getRowAction());
    }

    @Test(expected = TDPException.class)
    public void should_check_new_pattern_parameter_when_dealing_with_row() {
        //given
        final Map<String, String> insufficientParams = new HashMap<>();
        insufficientParams.put("column_id", "0000");

        //when
        ActionTestWorkbench.test(new DataSetRow(Collections.emptyMap()), action.create(insufficientParams).getRowAction());
    }

    @Test
    public void should_process_row() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "04/25/1999", "tata");
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_MM_dd_yyyy.json"));

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "25 - Apr - 1999", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_1108_invalid_pattern() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "04/25/1999", "tata");
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_MM_dd_yyyy.json"));
        parameters.put(ChangeDatePattern.NEW_PATTERN, "custom");
        parameters.put(ChangeDatePattern.CUSTOM_PATTERN, "ff");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "04/25/1999", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_1108_empty_pattern() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "04/25/1999", "tata");
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_MM_dd_yyyy.json"));
        parameters.put(ChangeDatePattern.NEW_PATTERN, "custom");
        parameters.put(ChangeDatePattern.CUSTOM_PATTERN, "");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "04/25/1999", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_set_new_pattern_as_most_used_one() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "04/25/1999", "tata");
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_MM_dd_yyyy.json"));

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

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
    public void should_process_row_when_value_does_not_match_most_frequent_pattern() throws Exception {
        // given
        DataSetRow row = getRow("toto", "04-25-09", "tata");
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_MM_dd_yyyy.json"));

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "25 - Apr - 2009", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_when_value_does_not_match_any_pattern() throws Exception {
        // given
        DataSetRow row = getRow("toto", "NA", "tata");
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_MM_dd_yyyy.json"));

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        final DataSetRow expectedRow = getRow("toto", "NA", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_when_value_is_empty() throws Exception {
        // given
        DataSetRow row = getRow("toto", "", "tata");
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_MM_dd_yyyy.json"));

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then (values should be unchanged)
        final DataSetRow expectedRow = getRow("toto", "", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.DATE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.STRING)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }
}
