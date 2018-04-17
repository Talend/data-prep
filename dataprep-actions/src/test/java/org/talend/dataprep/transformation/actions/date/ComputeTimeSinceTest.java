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

import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.OTHER_COLUMN_MODE;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.SELECTED_COLUMN_PARAMETER;
import static org.talend.dataprep.transformation.actions.date.ComputeTimeSince.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.assertj.core.data.MapEntry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for ComputeTimeSince action. Creates one consumer, and test it.
 *
 * @see ComputeTimeSince
 */
public class ComputeTimeSinceTest extends BaseDateTest<ComputeTimeSince> {

    private Map<String, String> parameters;

    public ComputeTimeSinceTest() {
        super(new ComputeTimeSince());
    }

    @Before
    public void init() throws IOException {
        final InputStream json = ComputeTimeSince.class.getResourceAsStream("computeTimeSinceAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(json);
        parameters.put(TIME_UNIT_PARAMETER, YEARS.name());
    }

    @Test
    public void testAdapt() throws Exception {
        Assert.assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        Assert.assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        Assert.assertThat(action.getCategory(Locale.US), is(ActionCategory.DATE.getDisplayName(Locale.US)));
    }

    @Override
    protected CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.VISIBLE_ENABLED;
    }

    @Test
    public void testParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(Locale.US);
        Assert.assertThat(parameters.size(), is(7));

        // Test on items label for TDP-2943:
        final SelectParameter selectParameter = (SelectParameter) parameters.get(6);
        assertEquals("Now", selectParameter.getItems().get(0).getLabel());
        assertEquals("Specific date", selectParameter.getItems().get(1).getLabel());
        assertEquals("Other column", selectParameter.getItems().get(2).getLabel());
    }

    @Test
    public void test_apply_inplace() throws IOException {
        //given
        final String date = "01/01/2010";
        final String result = computeTimeSince(date, "MM/dd/yyyy", YEARS);

        final DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy.json");
        row.set("0001", date);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", result);
        expectedValues.put("0002", "Bacon");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_apply_in_newcolumn() throws IOException {
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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters), factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        parameters.put(TIME_UNIT_PARAMETER, MONTHS.name());
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters), factory.create(action, parameters));

        //then
        assertEquals(expected, row.getRowMetadata().getColumns());
    }

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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        parameters.put(TIME_UNIT_PARAMETER, DAYS.name());
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertEquals(expected, row.getRowMetadata().getColumns());
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
    public void should_compute_days_since_other_column() throws IOException {
        //given
        final String date = "07/16/2015 13:00";
        final String compare = "07/26/2015 13:00";
        final DataSetRow row = builder() //
                .with(value("lorem bacon").type(Type.STRING).name("recipe")) //
                .with(value(date).type(Type.STRING).name("recipe").statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy_HH_mm.json"))) //
                .with(value(compare).type(Type.DATE).name("last update").statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy_HH_mm.json"))) //
                .build();

        parameters.put(TIME_UNIT_PARAMETER, DAYS.name());
        parameters.put(SINCE_WHEN_PARAMETER, OTHER_COLUMN_MODE);
        parameters.put(SELECTED_COLUMN_PARAMETER, "0002");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertThat(row.values()).contains(MapEntry.entry("0000", "lorem bacon"), //
                MapEntry.entry("0001", date), //
                MapEntry.entry("0003", "10"), //
                MapEntry.entry("0002", compare));
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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertThat(row.values()).contains(MapEntry.entry("0000", "lorem bacon"), //
                MapEntry.entry("0001", date), //
                MapEntry.entry("0003", StringUtils.EMPTY), //
                MapEntry.entry("0002", compare));
    }

    @Test
    public void should_compute_days_since_value() throws IOException {
        //given
        String date = "16/07/2015 13:00:00";
        String compare = "2015-07-06 13:00";
        String result = "-10";

        DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy_HH_mm.json");
        row.set("0001", date);

        parameters.put(TIME_UNIT_PARAMETER, DAYS.name());
        parameters.put(SINCE_WHEN_PARAMETER, SPECIFIC_DATE_MODE);
        parameters.put(SPECIFIC_DATE_PARAMETER, compare);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertThat(row.values()).contains(MapEntry.entry("0000", "lorem bacon"), //
                MapEntry.entry("0001", date), //
                MapEntry.entry("0003", result), //
                MapEntry.entry("0002", "Bacon"));
    }

    @Test
    public void testApplyOnColumn_specificDate_inYears_TDP_2532() throws IOException {
        //given
        String date = "16/07/2015 13:00:00";
        String compare = "2016-07-18 13:00";
        String result = "1";

        DataSetRow row = getDefaultRow("statistics_MM_dd_yyyy_HH_mm.json");
        row.set("0001", date);

        parameters.put(TIME_UNIT_PARAMETER, YEARS.name());
        parameters.put(SINCE_WHEN_PARAMETER, SPECIFIC_DATE_MODE);
        parameters.put(SPECIFIC_DATE_PARAMETER, compare);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertThat(row.values()).contains(MapEntry.entry("0000", "lorem bacon"), //
                MapEntry.entry("0001", date), //
                MapEntry.entry("0003", result), //
                MapEntry.entry("0002", "Bacon"));
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
        parameters.put(SPECIFIC_DATE_PARAMETER, compare);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertThat(row.values()).contains(MapEntry.entry("0000", "lorem bacon"), //
                MapEntry.entry("0001", date), //
                MapEntry.entry("0003", StringUtils.EMPTY), //
                MapEntry.entry("0002", "Bacon"));
    }

    /**
     * Compute time since now.
     *
     * @param date    the date to compute from.
     * @param pattern the pattern to use.
     * @param unit    the unit for the result.
     * @return time since now in the wanted unit.
     */
    String computeTimeSince(String date, String pattern, ChronoUnit unit) {
        return computeTimeSince(date, pattern, unit, null);
    }

    /**
     * Compute time since .
     *
     * @param date      the date to compute from.
     * @param pattern   the pattern to use.
     * @param unit      the unit for the result.
     * @param sinceWhen the date to calculate since when
     * @return time since now in the wanted unit.
     */
    String computeTimeSince(String date, String pattern, ChronoUnit unit, String sinceWhen) {

        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        Temporal since;
        if (sinceWhen == null) {
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

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

}
