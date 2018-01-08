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
package org.talend.dataprep.transformation.actions.conversions;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.date.BaseDateTest;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for DurationConverter action. Creates one consumer, and test it.
 *
 * @see DurationConverter
 */
public class DurationConverterTest extends BaseDateTest<DurationConverter> {

    public DurationConverterTest() {
        super(new DurationConverter());
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.CONVERSIONS.getDisplayName(Locale.US)));
    }

    @Test
    public void testName() {
        // when
        final String name = action.getName();

        // then
        assertThat(name, is("duration_converter"));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void shouldGetParameters() throws Exception {
        // given
        List<String> parameterNames = Arrays.asList("create_new_column", "from_unit", "to_unit", "precision","column_id", "row_id", "scope", "filter");

        // when
        final List<Parameter> parameters = action.getParameters(Locale.US);

        // then
        assertNotNull(parameters);
        assertEquals(8, parameters.size());
        final List<String> expectedParametersNotFound = parameters.stream().map(Parameter::getName) //
                .filter(n -> !parameterNames.contains(n)).collect(Collectors.toList());
        assertTrue(expectedParametersNotFound.toString() + " not found", expectedParametersNotFound.isEmpty());
    }

    private static final double year = 1.0;

    private static final double month = 12.2;

    private static final double week = 52.1;

    private static final double day = 365;

    private static final double hour = 8760;// 365 * 24;

    private static final double minute = 525600;// 365 * 24 * 60;

    private static final double second = 31536000;// 3.1536E7; //31536000;// 365 * 24 * 60 * 60;

    private static final double millisecond = 31536000000L;// (365 * 24 * 60 * 60 * 1000);

    @Test
    public void test_apply_inplace() {
        // given
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "365");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(123L);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");
        parameters.put("from_unit", ChronoUnit.DAYS.name());
        parameters.put("to_unit", ChronoUnit.YEARS.name());
        parameters.put("precision", "0");

        // when
        ActionTestWorkbench.test(Collections.singletonList(row1), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("1", row1.get("0001"));

        ColumnMetadata expected = ColumnMetadata.Builder.column().id(1).name("0001").type(Type.STRING).build();
        ColumnMetadata actual = row1.getRowMetadata().getById("0001");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "365");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(123L);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");
        parameters.put("from_unit", ChronoUnit.DAYS.name());
        parameters.put("to_unit", ChronoUnit.YEARS.name());
        parameters.put("precision", "0");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(Collections.singletonList(row1), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("365", row1.get("0001"));
        assertEquals("1", row1.get("0002"));

        ColumnMetadata expected = ColumnMetadata.Builder.column().id(2).name("0001_in_YEARS").type(Type.DOUBLE).build();
        ColumnMetadata actual = row1.getRowMetadata().getById("0002");
        assertEquals(expected, actual);
    }

    @Test
    public void testConversionDaysTo() {
        testConversion(day, ChronoUnit.DAYS, year, ChronoUnit.YEARS,"1");
        testConversion(day, ChronoUnit.DAYS, month, ChronoUnit.MONTHS,"1");
        testConversion(day, ChronoUnit.DAYS, week, ChronoUnit.WEEKS,"1");
        testConversion(day, ChronoUnit.DAYS, day, ChronoUnit.DAYS,"1");
        testConversion(day, ChronoUnit.DAYS, hour, ChronoUnit.HOURS,"1");
        testConversion(day, ChronoUnit.DAYS, minute, ChronoUnit.MINUTES,"1");
        testConversion(day, ChronoUnit.DAYS, second, ChronoUnit.SECONDS,"1");
        testConversion(day, ChronoUnit.DAYS, millisecond, ChronoUnit.MILLIS,"1");

        // TDP-3675: add precision parameter
        testConversion(1440, ChronoUnit.DAYS, 3.9, ChronoUnit.YEARS,"1");
        testConversion(1440, ChronoUnit.DAYS, 3.95, ChronoUnit.YEARS,"2");
        testConversion(1440, ChronoUnit.DAYS, 3.945, ChronoUnit.YEARS,"3");
        testConversion(1440, ChronoUnit.DAYS, 3.9452, ChronoUnit.YEARS,"4");
        testConversion(1440, ChronoUnit.DAYS, 3.94521, ChronoUnit.YEARS,"5");
        testConversion(1440, ChronoUnit.DAYS, 3.945205, ChronoUnit.YEARS,"6");
        testConversion(1440, ChronoUnit.DAYS, 3.9452055, ChronoUnit.YEARS,"7");
        testConversion(1440, ChronoUnit.DAYS, 3.94520548, ChronoUnit.YEARS,"8");
        testConversion(1440, ChronoUnit.DAYS, 3.945205479, ChronoUnit.YEARS,"9");
        testConversion(1440, ChronoUnit.DAYS, 3.9452054795, ChronoUnit.YEARS,"10");
    }

    @Test
    public void testConversionYearsTo() {
        testConversion(year, ChronoUnit.YEARS, year, ChronoUnit.YEARS,"1");
        testConversion(year, ChronoUnit.YEARS, month, ChronoUnit.MONTHS,"1");
        testConversion(year, ChronoUnit.YEARS, week, ChronoUnit.WEEKS,"1");
        testConversion(year, ChronoUnit.YEARS, day, ChronoUnit.DAYS,"1");
        testConversion(year, ChronoUnit.YEARS, hour, ChronoUnit.HOURS,"1");
        testConversion(year, ChronoUnit.YEARS, minute, ChronoUnit.MINUTES,"1");
        testConversion(year, ChronoUnit.YEARS, second, ChronoUnit.SECONDS,"1");
        testConversion(year, ChronoUnit.YEARS, millisecond, ChronoUnit.MILLIS,"1");
    }

    @Test
    public void testConversionMonthsTo() {
        testConversion(month, ChronoUnit.MONTHS, year, ChronoUnit.YEARS,"1");
        testConversion(month, ChronoUnit.MONTHS, month, ChronoUnit.MONTHS,"1");
        testConversion(month, ChronoUnit.MONTHS, week+0.2 , ChronoUnit.WEEKS,"1");
        testConversion(month, ChronoUnit.MONTHS, day, ChronoUnit.DAYS,"1");
        testConversion(month, ChronoUnit.MONTHS, hour, ChronoUnit.HOURS,"1");
        testConversion(month, ChronoUnit.MONTHS, minute, ChronoUnit.MINUTES,"1");
        testConversion(month, ChronoUnit.MONTHS, second, ChronoUnit.SECONDS,"1");
        testConversion(month, ChronoUnit.MONTHS, millisecond, ChronoUnit.MILLIS,"1");
    }

    @Test
    public void testConversionWeeksTo() {
        testConversion(week, ChronoUnit.WEEKS, year, ChronoUnit.YEARS,"1");
        testConversion(week, ChronoUnit.WEEKS, month, ChronoUnit.MONTHS,"1");
        testConversion(week, ChronoUnit.WEEKS, week, ChronoUnit.WEEKS,"1");
        testConversion(week, ChronoUnit.WEEKS, day, ChronoUnit.DAYS,"1");
        testConversion(week, ChronoUnit.WEEKS, hour, ChronoUnit.HOURS,"1");
        testConversion(week, ChronoUnit.WEEKS, minute, ChronoUnit.MINUTES,"1");
        testConversion(week, ChronoUnit.WEEKS, second, ChronoUnit.SECONDS,"1");
        testConversion(week, ChronoUnit.WEEKS, millisecond, ChronoUnit.MILLIS,"1");
    }

    @Test
    public void testConversionHoursTo() {
        testConversion(hour, ChronoUnit.HOURS, year, ChronoUnit.YEARS,"1");
        testConversion(hour, ChronoUnit.HOURS, month, ChronoUnit.MONTHS,"1");
        testConversion(hour, ChronoUnit.HOURS, week, ChronoUnit.WEEKS,"1");
        testConversion(hour, ChronoUnit.HOURS, day, ChronoUnit.DAYS,"1");
        testConversion(hour, ChronoUnit.HOURS, hour, ChronoUnit.HOURS,"1");
        testConversion(hour, ChronoUnit.HOURS, minute, ChronoUnit.MINUTES,"1");
        testConversion(hour, ChronoUnit.HOURS, second, ChronoUnit.SECONDS,"1");
        testConversion(hour, ChronoUnit.HOURS, millisecond, ChronoUnit.MILLIS,"1");
    }

    @Test
    public void testConversionMinutesTo() {
        testConversion(minute, ChronoUnit.MINUTES, year, ChronoUnit.YEARS,"1");
        testConversion(minute, ChronoUnit.MINUTES, month, ChronoUnit.MONTHS,"1");
        testConversion(minute, ChronoUnit.MINUTES, week, ChronoUnit.WEEKS,"1");
        testConversion(minute, ChronoUnit.MINUTES, day, ChronoUnit.DAYS,"1");
        testConversion(minute, ChronoUnit.MINUTES, hour, ChronoUnit.HOURS,"1");
        testConversion(minute, ChronoUnit.MINUTES, minute, ChronoUnit.MINUTES,"1");
        testConversion(minute, ChronoUnit.MINUTES, second, ChronoUnit.SECONDS,"1");
        testConversion(minute, ChronoUnit.MINUTES, millisecond, ChronoUnit.MILLIS,"1");
    }

    @Test
    public void testConversionSecondsTo() {
        testConversion(second, ChronoUnit.SECONDS, year, ChronoUnit.YEARS,"1");
        testConversion(second, ChronoUnit.SECONDS, month, ChronoUnit.MONTHS,"1");
        testConversion(second, ChronoUnit.SECONDS, week, ChronoUnit.WEEKS,"1");
        testConversion(second, ChronoUnit.SECONDS, day, ChronoUnit.DAYS,"1");
        testConversion(second, ChronoUnit.SECONDS, hour, ChronoUnit.HOURS,"1");
        testConversion(second, ChronoUnit.SECONDS, minute, ChronoUnit.MINUTES,"1");
        testConversion(second, ChronoUnit.SECONDS, second, ChronoUnit.SECONDS,"1");
        testConversion(second, ChronoUnit.SECONDS, millisecond, ChronoUnit.MILLIS,"1");
    }

    @Test
    public void testConversionMillisecondsTo() {
        testConversion(millisecond, ChronoUnit.MILLIS, year, ChronoUnit.YEARS,"1");
        testConversion(millisecond, ChronoUnit.MILLIS, month, ChronoUnit.MONTHS,"1");
        testConversion(millisecond, ChronoUnit.MILLIS, week, ChronoUnit.WEEKS,"1");
        testConversion(millisecond, ChronoUnit.MILLIS, day, ChronoUnit.DAYS,"1");
        testConversion(millisecond, ChronoUnit.MILLIS, hour, ChronoUnit.HOURS,"1");
        testConversion(millisecond, ChronoUnit.MILLIS, minute, ChronoUnit.MINUTES,"1");
        testConversion(millisecond, ChronoUnit.MILLIS, second, ChronoUnit.SECONDS,"1");
        testConversion(millisecond, ChronoUnit.MILLIS, millisecond, ChronoUnit.MILLIS,"1");
    }

    private void testConversion(double from, ChronoUnit fromUnit, double expected, ChronoUnit toUnit, String precision) {
        // given
        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", String.valueOf(from));
        final DataSetRow row1 = new DataSetRow(rowContent);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "foo");
        final DataSetRow row2 = new DataSetRow(rowContent);

        // row 3
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", null);
        final DataSetRow row3 = new DataSetRow(rowContent);

        // row 4
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "");
        final DataSetRow row4 = new DataSetRow(rowContent);

        // row 5
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", Boolean.toString(true));
        final DataSetRow row5 = new DataSetRow(rowContent);

        // row 6
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "1985-08-27");
        final DataSetRow row6 = new DataSetRow(rowContent);

        // row 7
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "0");
        final DataSetRow row7 = new DataSetRow(rowContent);

        // row 8
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "-1");
        final DataSetRow row8 = new DataSetRow(rowContent);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");
        parameters.put("from_unit", fromUnit.name());
        parameters.put("to_unit", toUnit.name());
        parameters.put("precision", precision);

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        // TDP-3675: add precision parameter
        assertEquals(expected , Double.valueOf(row1.get("0001")), Math.pow(0.1, Integer.valueOf(precision)));
        assertEquals("foo", row2.get("0001"));
        assertEquals(null, row3.get("0001"));
        assertEquals("", row4.get("0001"));
        assertEquals("true", row5.get("0001"));
        assertEquals("1985-08-27", row6.get("0001"));
        assertEquals("0", row7.get("0001"));
        assertEquals("-1", row8.get("0001"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
        assertTrue(action.acceptField(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

}
