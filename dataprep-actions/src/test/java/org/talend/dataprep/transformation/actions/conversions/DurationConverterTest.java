// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.*;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.*;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.conversions.DurationConverter;
import org.talend.dataprep.transformation.actions.date.BaseDateTest;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for DurationConverter action. Creates one consumer, and test it.
 *
 * @see DurationConverter
 */
public class DurationConverterTest extends BaseDateTest {

    /** The action to test. */
    private DurationConverter action = new DurationConverter();

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.CONVERSIONS.getDisplayName()));
    }

    @Test
    public void testName() {
        // when
        final String name = action.getName();

        // then
        assertThat(name, is("duration_converter"));
    }

    @Test
    public void shouldGetParameters() throws Exception {
        // given
        List<String> parameterNames = Arrays.asList("from_unit", "to_unit", "column_id", "row_id", "scope", "filter");

        // when
        final List<Parameter> parameters = action.getParameters();

        // then
        assertNotNull(parameters);
        assertEquals(6, parameters.size());
        final List<String> expectedParametersNotFound = parameters.stream().map(Parameter::getName) //
                .filter(n -> !parameterNames.contains(n)).collect(Collectors.toList());
        assertTrue(expectedParametersNotFound.toString() + " not found", expectedParametersNotFound.isEmpty());
    }

    private static double year = 1;

    private static double month = 12;

    private static double week = 52;

    private static double day = 365;

    private static double hour = 8760;// 365 * 24;

    private static double minute = 525600;// 365 * 24 * 60;

    private static double second = 31536000;// 365 * 24 * 60 * 60;

    private static double millisecond = 31536000000L;// (365 * 24 * 60 * 60 * 1000);

    @Test
    public void testConversionDaysTo() {
        testConversion(day, ChronoUnit.DAYS, year, ChronoUnit.YEARS);
        testConversion(day, ChronoUnit.DAYS, month, ChronoUnit.MONTHS);
        testConversion(day, ChronoUnit.DAYS, week, ChronoUnit.WEEKS);
        testConversion(day, ChronoUnit.DAYS, day, ChronoUnit.DAYS);
        testConversion(day, ChronoUnit.DAYS, hour, ChronoUnit.HOURS);
        testConversion(day, ChronoUnit.DAYS, minute, ChronoUnit.MINUTES);
        testConversion(day, ChronoUnit.DAYS, second, ChronoUnit.SECONDS);
        testConversion(day, ChronoUnit.DAYS, millisecond, ChronoUnit.MILLIS);
    }

    @Test
    public void testConversionYearsTo() {
        testConversion(year, ChronoUnit.YEARS, year, ChronoUnit.YEARS);
        testConversion(year, ChronoUnit.YEARS, month, ChronoUnit.MONTHS);
        testConversion(year, ChronoUnit.YEARS, week, ChronoUnit.WEEKS);
        testConversion(year, ChronoUnit.YEARS, day, ChronoUnit.DAYS);
        testConversion(year, ChronoUnit.YEARS, hour, ChronoUnit.HOURS);
        testConversion(year, ChronoUnit.YEARS, minute, ChronoUnit.MINUTES);
        testConversion(year, ChronoUnit.YEARS, second, ChronoUnit.SECONDS);
        testConversion(year, ChronoUnit.YEARS, millisecond, ChronoUnit.MILLIS);
    }

    @Test
    public void testConversionMonthsTo() {
        testConversion(month, ChronoUnit.MONTHS, year, ChronoUnit.YEARS);
        testConversion(month, ChronoUnit.MONTHS, month, ChronoUnit.MONTHS);
        testConversion(month, ChronoUnit.MONTHS, week-1, ChronoUnit.WEEKS);
        testConversion(month, ChronoUnit.MONTHS, day, ChronoUnit.DAYS);
        testConversion(month, ChronoUnit.MONTHS, hour, ChronoUnit.HOURS);
        testConversion(month, ChronoUnit.MONTHS, minute, ChronoUnit.MINUTES);
        testConversion(month, ChronoUnit.MONTHS, second, ChronoUnit.SECONDS);
        testConversion(month, ChronoUnit.MONTHS, millisecond, ChronoUnit.MILLIS);
    }

    @Test
    public void testConversionWeeksTo() {
        testConversion(week, ChronoUnit.WEEKS, year, ChronoUnit.YEARS);
        testConversion(week, ChronoUnit.WEEKS, month, ChronoUnit.MONTHS);
        testConversion(week, ChronoUnit.WEEKS, week, ChronoUnit.WEEKS);
        testConversion(week, ChronoUnit.WEEKS, day, ChronoUnit.DAYS);
        testConversion(week, ChronoUnit.WEEKS, hour, ChronoUnit.HOURS);
        testConversion(week, ChronoUnit.WEEKS, minute, ChronoUnit.MINUTES);
        testConversion(week, ChronoUnit.WEEKS, second, ChronoUnit.SECONDS);
        testConversion(week, ChronoUnit.WEEKS, millisecond, ChronoUnit.MILLIS);
    }

    @Test
    public void testConversionHoursTo() {
        testConversion(hour, ChronoUnit.HOURS, year, ChronoUnit.YEARS);
        testConversion(hour, ChronoUnit.HOURS, month, ChronoUnit.MONTHS);
        testConversion(hour, ChronoUnit.HOURS, week, ChronoUnit.WEEKS);
        testConversion(hour, ChronoUnit.HOURS, day, ChronoUnit.DAYS);
        testConversion(hour, ChronoUnit.HOURS, hour, ChronoUnit.HOURS);
        testConversion(hour, ChronoUnit.HOURS, minute, ChronoUnit.MINUTES);
        testConversion(hour, ChronoUnit.HOURS, second, ChronoUnit.SECONDS);
        testConversion(hour, ChronoUnit.HOURS, millisecond, ChronoUnit.MILLIS);
    }

    @Test
    public void testConversionMinutesTo() {
        testConversion(minute, ChronoUnit.MINUTES, year, ChronoUnit.YEARS);
        testConversion(minute, ChronoUnit.MINUTES, month, ChronoUnit.MONTHS);
        testConversion(minute, ChronoUnit.MINUTES, week, ChronoUnit.WEEKS);
        testConversion(minute, ChronoUnit.MINUTES, day, ChronoUnit.DAYS);
        testConversion(minute, ChronoUnit.MINUTES, hour, ChronoUnit.HOURS);
        testConversion(minute, ChronoUnit.MINUTES, minute, ChronoUnit.MINUTES);
        testConversion(minute, ChronoUnit.MINUTES, second, ChronoUnit.SECONDS);
        testConversion(minute, ChronoUnit.MINUTES, millisecond, ChronoUnit.MILLIS);
    }

    @Test
    public void testConversionSecondsTo() {
        testConversion(second, ChronoUnit.SECONDS, year, ChronoUnit.YEARS);
        testConversion(second, ChronoUnit.SECONDS, month, ChronoUnit.MONTHS);
        testConversion(second, ChronoUnit.SECONDS, week, ChronoUnit.WEEKS);
        testConversion(second, ChronoUnit.SECONDS, day, ChronoUnit.DAYS);
        testConversion(second, ChronoUnit.SECONDS, hour, ChronoUnit.HOURS);
        testConversion(second, ChronoUnit.SECONDS, minute, ChronoUnit.MINUTES);
        testConversion(second, ChronoUnit.SECONDS, second, ChronoUnit.SECONDS);
        testConversion(second, ChronoUnit.SECONDS, millisecond, ChronoUnit.MILLIS);
    }

    @Test
    public void testConversionMillisecondsTo() {
        testConversion(millisecond, ChronoUnit.MILLIS, year, ChronoUnit.YEARS);
        testConversion(millisecond, ChronoUnit.MILLIS, month, ChronoUnit.MONTHS);
        testConversion(millisecond, ChronoUnit.MILLIS, week, ChronoUnit.WEEKS);
        testConversion(millisecond, ChronoUnit.MILLIS, day, ChronoUnit.DAYS);
        testConversion(millisecond, ChronoUnit.MILLIS, hour, ChronoUnit.HOURS);
        testConversion(millisecond, ChronoUnit.MILLIS, minute, ChronoUnit.MINUTES);
        testConversion(millisecond, ChronoUnit.MILLIS, second, ChronoUnit.SECONDS);
        testConversion(millisecond, ChronoUnit.MILLIS, millisecond, ChronoUnit.MILLIS);
    }

    private void testConversion(double from, ChronoUnit fromUnit, double expected, ChronoUnit toUnit) {
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

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(String.valueOf(expected), row1.get("0001"));
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
