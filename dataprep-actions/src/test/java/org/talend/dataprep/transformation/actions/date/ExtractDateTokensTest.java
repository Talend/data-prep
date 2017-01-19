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

package org.talend.dataprep.transformation.actions.date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.setStatistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ExtractDateTokensTest extends BaseDateTest {

    /** The action to test. */
    private ExtractDateTokens action = new ExtractDateTokens();

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(getDateTestJsonAsStream("extractDateTokensAction.json"));
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

    @Test
    public void should_process_row() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING)) //
                .with(value("04/25/1999").type(Type.DATE).statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .with(value("tata").type(Type.STRING)) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "04/25/1999");
        expectedValues.put("0003", "1999");
        expectedValues.put("0004", "4");
        expectedValues.put("0005", "0");
        expectedValues.put("0006", "0");
        expectedValues.put("0002", "tata");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_TDP_2480() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING)) //
                .with(value("Apr-25-1999").type(Type.DATE).statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .with(value("tata").type(Type.STRING)) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "Apr-25-1999");
        expectedValues.put("0003", "1999");
        expectedValues.put("0004", "4");
        expectedValues.put("0005", "0");
        expectedValues.put("0006", "0");
        expectedValues.put("0002", "tata");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_process_row_with_time() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING)) //
                .with(value("04/25/1999 15:45").type(Type.DATE).statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy_HH_mm.json"))) //
                .with(value("tata").type(Type.STRING)) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "04/25/1999 15:45");
        expectedValues.put("0003", "1999");
        expectedValues.put("0004", "4");
        expectedValues.put("0005", "15");
        expectedValues.put("0006", "45");
        expectedValues.put("0002", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * To test with a date that does not match the most frequent pattern, but match another one present in the stats
     */
    @Test
    public void should_process_row_wrong_pattern() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING)) //
                .with(value("04-25-09").type(Type.DATE).statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"))) //
                .with(value("tata").type(Type.STRING)) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "04-25-09");
        expectedValues.put("0003", "2009");
        expectedValues.put("0004", "4");
        expectedValues.put("0005", "0");
        expectedValues.put("0006", "0");
        expectedValues.put("0002", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        setStatistics(row, "0001", getDateTestJsonAsStream("statistics_MM_dd_yyyy.json"));

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "NA");
        expectedValues.put("0003", "");
        expectedValues.put("0004", "");
        expectedValues.put("0005", "");
        expectedValues.put("0006", "");
        expectedValues.put("0002", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        final Statistics statistics = mapper.reader(Statistics.class).readValue(getDateTestJsonAsStream("statistics_yyyy-MM-dd.json"));
        input.get(1).setStatistics(statistics);

        // when
        ActionTestWorkbench.test(rowMetadata, actionRegistry, factory.create(action, parameters));

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
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

}
