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

import static java.time.temporal.ChronoUnit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.date.ComputeTimeSince.TIME_UNIT_PARAMETER;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class for Split action. Creates one consumer, and test it.
 *
 * @see ComputeTimeSince
 */
public class ComputeTimeSinceTest extends BaseDateTests {

    /** The action to test. */
    @Autowired
    private ComputeTimeSince action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        final ComputeTimeSince currentAction = new ComputeTimeSince();
        final InputStream json = ComputeTimeSince.class.getResourceAsStream("computeTimeSinceAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(json);
        parameters.put(TIME_UNIT_PARAMETER, YEARS.name());
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

    /**
     * @see ComputeTimeSince#create(Map)
     */
    @Test
    public void should_compute_years() throws IOException {
        //given
        final String date = "01/01/2010";
        final String result = computeTimeSince(date, "MM/dd/yyyy", YEARS);

        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy.json");
        row.set("0001", date);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", date);
        expectedValues.put("0003", result);
        expectedValues.put("0002", "Bacon");

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_compute_years_alternative_pattern() throws IOException {
        // given
        final String date = "01-01-10";
        final String result = computeTimeSince(date, "MM-dd-yy", YEARS);

        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy.json");
        row.set("0001", date);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", date);
        expectedValues.put("0003", result);
        expectedValues.put("0002", "Bacon");

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_compute_years_wrong_pattern() throws IOException {
        // given
        final String date = "NA";
        final String result = "";

        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy.json");
        row.set("0001", date);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", date);
        expectedValues.put("0003", result);
        expectedValues.put("0002", "Bacon");

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see ComputeTimeSince#create(Map)
     */
    @Test
    public void should_compute_days() throws IOException {
        //given
        final String date = "06/15/2015";
        final String result = computeTimeSince(date, "MM/dd/yyyy", ChronoUnit.DAYS);

        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy.json");
        row.set("0001", date);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", date);
        expectedValues.put("0003", result);
        expectedValues.put("0002", "Bacon");

        parameters.put(TIME_UNIT_PARAMETER, DAYS.name());

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see ComputeTimeSince#create(Map)
     */
    @Test
    public void should_compute_hours() throws IOException {
        //given
        final String date = "07/16/2015 13:00";
        final String result = computeTimeSince(date, "MM/dd/yyyy HH:mm", ChronoUnit.HOURS);

        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy_HH_mm.json");
        row.set("0001", date);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", date);
        expectedValues.put("0003", result);
        expectedValues.put("0002", "Bacon");

        parameters.put(TIME_UNIT_PARAMETER, HOURS.name());

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see ComputeTimeSince#create(Map)
     */
    @Test
    public void should_compute_hours_on_date_without_hours() throws IOException {
        // given
        final String date = "07/16/2015";
        final String result = computeTimeSince(date, "MM/dd/yyyy", ChronoUnit.HOURS);

        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy.json");
        row.set("0001", date);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", date);
        expectedValues.put("0003", result);
        expectedValues.put("0002", "Bacon");

        parameters.put(TIME_UNIT_PARAMETER, HOURS.name());

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_compute_hours_twice() throws IOException {
        //given
        final String date = "07/16/2015 13:00";
        final String result = computeTimeSince(date, "MM/dd/yyyy HH:mm", ChronoUnit.HOURS);

        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy_HH_mm.json");
        row.set("0001", date);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", date);
        expectedValues.put("0004", result);
        expectedValues.put("0003", result);
        expectedValues.put("0002", "Bacon");

        parameters.put(TIME_UNIT_PARAMETER, HOURS.name());

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_compute_twice_diff_units() throws IOException {
        //given
        final String date = "07/15/2014 02:00";
        final String resultInMonth = computeTimeSince(date, "M/d/yyyy HH:mm", ChronoUnit.MONTHS);
        final String resultInYears = computeTimeSince(date, "M/d/yyyy HH:mm", ChronoUnit.YEARS);

        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy_HH_mm.json");
        row.set("0001", date);

        final Map<String, String> expectedValues = new TreeMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", date);
        expectedValues.put("0004", resultInMonth);
        expectedValues.put("0003", resultInYears);
        expectedValues.put("0002", "Bacon");

        //when
        parameters.put(TIME_UNIT_PARAMETER, YEARS.name());
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        parameters.put(TIME_UNIT_PARAMETER, MONTHS.name());
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see ComputeTimeSince#create(Map)
     */
    @Test
    public void should_deal_with_null_value() throws IOException {
        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy.json");
        row.set("0001", null);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", null);
        expectedValues.put("0003", "");
        expectedValues.put("0002", "Bacon");

        parameters.put(TIME_UNIT_PARAMETER, DAYS.name());

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Action#getRowAction()
     */
    @Test
    public void should_update_metadata() throws IOException {
        //given
        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy.json");

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        expected.add(createMetadata("0003", "since_last update_in_years", Type.INTEGER));
        expected.add(createMetadata("0002", "steps"));

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expected, row.getRowMetadata().getColumns());
    }

    /**
     * @see Action#getRowAction()
     */
    @Test
    public void should_update_metadata_twice() throws IOException {
        //given
        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy.json");

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        expected.add(createMetadata("0004", "since_last update_in_years", Type.INTEGER));
        expected.add(createMetadata("0003", "since_last update_in_years", Type.INTEGER));
        expected.add(createMetadata("0002", "steps"));

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expected, row.getRowMetadata().getColumns());
    }

    /**
     * @see Action#getRowAction()
     */
    @Test
    public void should_update_metadata_twice_diff_units() throws IOException {
        //given
        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy.json");

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        expected.add(createMetadata("0004", "since_last update_in_days", Type.INTEGER));
        expected.add(createMetadata("0003", "since_last update_in_years", Type.INTEGER));
        expected.add(createMetadata("0002", "steps"));

        //when
        parameters.put(TIME_UNIT_PARAMETER, YEARS.name());
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        parameters.put(TIME_UNIT_PARAMETER, DAYS.name());
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

        //then
        assertEquals(expected, row.getRowMetadata().getColumns());
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

    /**
     * Compute time since now.
     *
     * @param date the date to compute from.
     * @param pattern the pattern to use.
     * @param unit the unit for the result.
     * @return time since now in the wanted unit.
     */
    String computeTimeSince(String date, String pattern, ChronoUnit unit) {

        Temporal now = LocalDateTime.now();

        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime start;
        try {
            start = LocalDateTime.parse(date, format);
        } catch (Exception e) {
            start = null;
        }

        if (start == null) {
            LocalDate temp = LocalDate.parse(date, format);
            start = temp.atStartOfDay();
        }

        Temporal result = (unit == ChronoUnit.HOURS ? LocalDateTime.from(start) : LocalDate.from(start));
        return String.valueOf(unit.between(result, now));
    }

    /**
     * @param statisticsFileName the statistics file name to use.
     * @return a row with default settings for the tests.
     */
    private DataSetRow getDefaultRow(String statisticsFileName) throws IOException {

        List<ColumnMetadata> columns = new ArrayList<>(3);
        columns.add(ColumnMetadata.Builder.column().name("recipe").type(Type.STRING).build());
        ObjectMapper mapper = new ObjectMapper();
        final Statistics statistics = mapper.reader(Statistics.class).readValue(ComputeTimeSinceTest.class.getResourceAsStream(statisticsFileName));
        columns.add(ColumnMetadata.Builder.column().name("last update").type(Type.DATE).statistics(statistics).build());
        columns.add(ColumnMetadata.Builder.column().name("steps").type(Type.STRING).build());

        RowMetadata metadata = new RowMetadata();
        metadata.setColumns(columns);

        Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "01/01/2010");
        values.put("0002", "Bacon");

        return new DataSetRow(metadata, values);
    }

    private ColumnMetadata createMetadata(String id, String name, Type type, String statisticsFileName) throws IOException {
        ColumnMetadata column = createMetadata(id, name, type);
        ObjectMapper mapper = new ObjectMapper();
        final Statistics statistics = mapper.reader(Statistics.class).readValue(ComputeTimeSinceTest.class.getResourceAsStream(statisticsFileName));
        column.setStatistics(statistics);
        return column;
    }

    private ColumnMetadata createMetadata(String id, String name) {
        return createMetadata(id, name, Type.STRING);
    }

    private ColumnMetadata createMetadata(String id, String name, Type type) {
        return ColumnMetadata.Builder.column().computedId(id).name(name).type(type).build();
    }

}
