package org.talend.dataprep.transformation.api.action.metadata.date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.setStatistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for the ChangeDatePattern action.
 *
 * @see ChangeDatePattern
 */
public class ExtractDateTokensTest extends BaseDateTests {

    /** The action to test. */
    @Autowired
    private ExtractDateTokens action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {

        parameters = ActionMetadataTestUtils.parseParameters( //
                //
                ExtractDateTokensTest.class.getResourceAsStream("extractDateTokensAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATE.getDisplayName()));
    }

    @Test
    public void should_process_row() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "toto");
        values.put("0001", "04/25/1999");
        values.put("0002", "tata");

        final DataSetRow row = new DataSetRow(values);
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_MM_dd_yyyy.json"));

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "04/25/1999");
        expectedValues.put("0003", "1999");
        expectedValues.put("0004", "4");
        expectedValues.put("0005", "0");
        expectedValues.put("0006", "0");
        expectedValues.put("0002", "tata");

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_process_row_with_time() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "toto");
        values.put("0001", "04/25/1999 15:45");
        values.put("0002", "tata");
        final DataSetRow row = new DataSetRow(values);
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_MM_dd_yyyy_HH_mm.json"));

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "04/25/1999 15:45");
        expectedValues.put("0003", "1999");
        expectedValues.put("0004", "4");
        expectedValues.put("0005", "15");
        expectedValues.put("0006", "45");
        expectedValues.put("0002", "tata");

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * To test with a date that does not match the most frequent pattern, but match another one present in the stats
     */
    @Test
    public void should_process_row_wrong_pattern() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "toto");
        values.put("0001", "04-25-09");
        values.put("0002", "tata");
        final DataSetRow row = new DataSetRow(values);
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_MM_dd_yyyy.json"));

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "04-25-09");
        expectedValues.put("0003", "2009");
        expectedValues.put("0004", "4");
        expectedValues.put("0005", "0");
        expectedValues.put("0006", "0");
        expectedValues.put("0002", "tata");

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * To test with a date that does not match any of the pattern present in the stats
     */
    @Test
    public void should_process_row_very_wrong_pattern() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "toto");
        values.put("0001", "NA");
        values.put("0002", "tata");
        final DataSetRow row = new DataSetRow(values);
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_MM_dd_yyyy.json"));

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "NA");
        expectedValues.put("0003", "");
        expectedValues.put("0004", "");
        expectedValues.put("0005", "");
        expectedValues.put("0006", "");
        expectedValues.put("0002", "tata");

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_update_metadata() throws IOException {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000"));
        input.add(createMetadata("0001"));
        input.add(createMetadata("0002"));
        final RowMetadata rowMetadata = new RowMetadata(input);
        ObjectMapper mapper = new ObjectMapper();
        final Statistics statistics = mapper.reader(Statistics.class).readValue(ChangeDatePatternTest.class.getResourceAsStream("statistics_yyyy-MM-dd.json"));
        input.get(1).setStatistics(statistics);

        // when
        action.applyOnColumn(new DataSetRow(rowMetadata), new TransformationContext(), parameters, "0001");

        // then
        assertNotNull(rowMetadata.getById("0003"));
        assertNotNull(rowMetadata.getById("0004"));
        assertNotNull(rowMetadata.getById("0005"));
        assertNotNull(rowMetadata.getById("0006"));
        assertNull(rowMetadata.getById("0007"));
    }

    private ColumnMetadata createMetadata(String id) {
        return createMetadata(id, Type.STRING);
    }

    private ColumnMetadata createMetadata(String id, Type type) {
        return ColumnMetadata.Builder.column().computedId(id).type(type).headerSize(12).empty(0).invalid(2)
                .valid(5).build();
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
