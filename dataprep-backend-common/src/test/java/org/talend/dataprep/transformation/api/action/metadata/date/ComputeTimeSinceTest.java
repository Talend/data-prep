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
package org.talend.dataprep.transformation.api.action.metadata.date;

import static java.time.temporal.ChronoUnit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters.MODE_PARAMETER;
import static org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters.OTHER_COLUMN_MODE;
import static org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters.SELECTED_COLUMN_PARAMETER;
import static org.talend.dataprep.transformation.api.action.metadata.date.ComputeTimeSince.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for ComputeTimeSince action. Creates one consumer, and test it.
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
        final InputStream json = ComputeTimeSince.class.getResourceAsStream("computeTimeSinceAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(json);
        parameters.put(TIME_UNIT_PARAMETER, YEARS.name());
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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        //then
        assertEquals(expectedValues, row.values());
    }

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        //then
        assertEquals(expectedValues, row.values());
    }

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters), factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        parameters.put(TIME_UNIT_PARAMETER, MONTHS.name());
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        //then
        assertEquals(expectedValues, row.values());
    }

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        //then
        assertEquals(expected, row.getRowMetadata().getColumns());
    }

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
        ActionTestWorkbench.test(row, factory.create(action, parameters), factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        parameters.put(TIME_UNIT_PARAMETER, DAYS.name());
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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

    @Test
    public void should_compute_days_since_other_column() throws IOException {
        //given
        String date = "07/16/2015 13:00";
        String compare = "07/26/2015 13:00";

        DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy_HH_mm.json");
        row.set("0001", date);
        row.set("0002", compare);

        parameters.put(TIME_UNIT_PARAMETER, DAYS.name());
        parameters.put(SINCE_WHEN_PARAMETER, OTHER_COLUMN_MODE);
        parameters.put(SELECTED_COLUMN_PARAMETER, "0002");

        //when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        //then
        Assertions.assertThat(row.values()).contains(
            MapEntry.entry( "0000", "lorem bacon" ), //
            MapEntry.entry( "0001", date ), //
            MapEntry.entry( "0003", "10" ), //
            MapEntry.entry( "0002", compare )
        );
    }

    @Test
    public void should_not_fail_computing_days_since_other_column_not_date() throws IOException {
        //given
        String date = "07/16/2015 13:00";
        String compare = "beer";

        DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy_HH_mm.json");
        row.set("0001", date);
        row.set("0002", compare);

        parameters.put(TIME_UNIT_PARAMETER, DAYS.name());
        parameters.put(SINCE_WHEN_PARAMETER, OTHER_COLUMN_MODE);
        parameters.put(SELECTED_COLUMN_PARAMETER, "0002");

        //when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        //then
        Assertions.assertThat(row.values()).contains(
            MapEntry.entry( "0000", "lorem bacon" ), //
            MapEntry.entry( "0001", date ), //
            MapEntry.entry( "0003", StringUtils.EMPTY ), //
            MapEntry.entry( "0002", compare )
        );
    }


    @Test
    public void should_compute_days_since_value() throws IOException {
        //given
        String date = "16/07/2015 13:00:00";
        String compare = "06/07/2015 13:00:00";
        String result = computeTimeSince(date, "dd/MM/yyyy HH:mm:ss", ChronoUnit.DAYS, compare);

        DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy_HH_mm.json");
        row.set("0001", date);

        parameters.put(TIME_UNIT_PARAMETER, DAYS.name());
        parameters.put(SINCE_WHEN_PARAMETER, SPECIFIC_DATE_MODE);
        parameters.put(SPECIFIC_DATE_PARAMETER, compare );

        //when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        //then
        Assertions.assertThat(row.values()).contains(
            MapEntry.entry( "0000", "lorem bacon" ), //
            MapEntry.entry( "0001", date ), //
            MapEntry.entry( "0003", result ), //
            MapEntry.entry( "0002", "Bacon" )
        );
    }

    @Test
    public void should_not_fail_computing_days_since_value_not_date() throws IOException {
        //given
        String date = "16/07/2015 13:00:00";
        String compare = "foo";

        DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy_HH_mm.json");
        row.set("0001", date);

        parameters.put(TIME_UNIT_PARAMETER, DAYS.name());
        parameters.put(SINCE_WHEN_PARAMETER, SPECIFIC_DATE_MODE);
        parameters.put(SPECIFIC_DATE_PARAMETER, compare );

        //when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        //then
        Assertions.assertThat(row.values()).contains(
            MapEntry.entry( "0000", "lorem bacon" ), //
            MapEntry.entry( "0001", date ), //
            MapEntry.entry( "0003", StringUtils.EMPTY ), //
            MapEntry.entry( "0002", "Bacon" )
        );
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
        return computeTimeSince( date, pattern, unit, null );
    }

    /**
     * Compute time since .
     *
     * @param date the date to compute from.
     * @param pattern the pattern to use.
     * @param unit the unit for the result.
     * @param sinceWhen the date to calculate since when
     * @return time since now in the wanted unit.
     */
    String computeTimeSince(String date, String pattern, ChronoUnit unit, String sinceWhen) {

        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        Temporal since;
        if (sinceWhen == null){
            since = LocalDateTime.now();
        } else {
            since = LocalDateTime.parse(sinceWhen, format);
        }

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

        Temporal result = LocalDateTime.from(start);
        return String.valueOf(unit.between(result, since));
    }

}
