// ============================================================================
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
package org.talend.dataprep.transformation.actions.date;

import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.chrono.AbstractChronology;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.COLUMN_ID;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.ROW_ID;
import static org.talend.dataprep.transformation.actions.date.DateCalendarConverter.*;

/**
 * Test class for DateCalendarConverter action. Creates one consumer, and test it.
 *
 * @see DateCalendarConverter
 */
public class DateCalendarConverterTest extends BaseDateTest<DateCalendarConverter> {

    private Map<String, String> parameters;

    public DateCalendarConverterTest() {
        super(new DateCalendarConverter());
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
    public void testName() {
        // when
        final String name = action.getName();

        // then
        assertThat(name, is("date_calendar_converter"));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void shouldGetParameters() throws Exception {
        // given
        List<String> parameterNames = Arrays.asList("create_new_column", TO_CALENDAR_TYPE_PARAMETER, FROM_CALENDAR_TYPE_PARAMETER, FROM_MODE, "new_pattern",
                COLUMN_ID.getKey(), ROW_ID.getKey(), "scope", "filter");

        // when
        final List<Parameter> parameters = action.getParameters(Locale.US);

        // then
        assertNotNull(parameters);
        assertEquals(6, parameters.size());
        final List<String> expectedParametersNotFound = parameters.stream().map(Parameter::getName)
                .filter(n -> !parameterNames.contains(n)).collect(Collectors.toList());
        assertTrue(expectedParametersNotFound.toString() + " not found", expectedParametersNotFound.isEmpty());
    }

    private static final String pattern = "yyyy-MM-dd";

    private static final String IsoStr = "1996-10-29";

    private static final String HijrahStr = "1417-06-16";

    private static final String HijrahStr2 = "1417/06/16";

    private static final String JapaneseStr = "0008-10-29";

    private static final String MinguoStr = "0085-10-29";

    private static final String ThaiBuddhistStr = "2539-10-29";

    private static final String pattern1 = "yyyy/MM/dd";

    private static final String IsoStr1 = "1996/10/29";

    private static final String IsoStr2 = "1996-10-29";

    private static final String HijrahStr3 = "06/16/1417";

    private static final String MinguoStr1 = "0085/10/29";

    private static final String ThaiBuddhistStr1 = "2539/10/29";

    private static final String ThaiBuddhistStr2 = "2539-10-29";

    private static final String JulianDay = "2450386";

    private static final String ModifiedJulianDay = "50385";

    private static final String RataDie = "728961";

    private static final String EpochDay = "9798";

    @Test
    public void testBlank_values() {
        testConversion(null, DateCalendarConverter.CalendarUnit.ISO, pattern, null,
                DateCalendarConverter.CalendarUnit.HIJRI);
        testConversion("", DateCalendarConverter.CalendarUnit.ISO, pattern, "",
                DateCalendarConverter.CalendarUnit.HIJRI);
    }

    @Test
    public void testConversion_all_custom_patterns() {
        testConversion(IsoStr, DateCalendarConverter.CalendarUnit.ISO, pattern, HijrahStr,
                DateCalendarConverter.CalendarUnit.HIJRI);
        testConversion(IsoStr, DateCalendarConverter.CalendarUnit.ISO, pattern, JapaneseStr,
                DateCalendarConverter.CalendarUnit.JAPANESE);
        testConversion(IsoStr1, DateCalendarConverter.CalendarUnit.ISO, pattern1, MinguoStr1,
                DateCalendarConverter.CalendarUnit.MINGUO);
        testConversion(IsoStr, DateCalendarConverter.CalendarUnit.ISO, pattern, ThaiBuddhistStr2,
                DateCalendarConverter.CalendarUnit.THAI_BUDDHIST);
        testConversion(IsoStr, DateCalendarConverter.CalendarUnit.ISO, pattern, IsoStr2,
                DateCalendarConverter.CalendarUnit.ISO);
    }

    @Test
    public void testConversion_month() {
        testConversion("01/09/2015", DateCalendarConverter.CalendarUnit.ISO, "dd/MM/yyyy", "17/11/1436",
                DateCalendarConverter.CalendarUnit.HIJRI);
        testConversion("01/Sep/2015", DateCalendarConverter.CalendarUnit.ISO, "dd/MMM/yyyy", "17/ذو القعدة/1436",
                DateCalendarConverter.CalendarUnit.HIJRI);
    }

    @Test
    public void testConversion_out_of_range_calendar() {
        testConversion("12/5/15", DateCalendarConverter.CalendarUnit.ISO, "MM/dd/yyyy", "12/5/15",
                DateCalendarConverter.CalendarUnit.HIJRI);
    }

    @Test
    public void testConversion_only_input_custom() {
        testConversion(IsoStr, DateCalendarConverter.CalendarUnit.ISO, pattern, HijrahStr,
                DateCalendarConverter.CalendarUnit.HIJRI);
        testConversion(MinguoStr1, DateCalendarConverter.CalendarUnit.MINGUO, pattern1, HijrahStr2,
                DateCalendarConverter.CalendarUnit.HIJRI);
        testConversion(ThaiBuddhistStr1, DateCalendarConverter.CalendarUnit.THAI_BUDDHIST, pattern1, HijrahStr2,
                DateCalendarConverter.CalendarUnit.HIJRI);
        testConversion(MinguoStr, DateCalendarConverter.CalendarUnit.MINGUO, pattern, HijrahStr,
                DateCalendarConverter.CalendarUnit.HIJRI);
        testConversion(ThaiBuddhistStr, DateCalendarConverter.CalendarUnit.THAI_BUDDHIST, pattern, HijrahStr,
                DateCalendarConverter.CalendarUnit.HIJRI);
    }

    @Test
    public void testConversion_only_output_custom() throws IOException {
        // given
        final DataSetRow row = builder().with(value("toto").type(Type.STRING).name("recipe"))
                .with(value("10/29/1996").type(Type.DATE).name("last update")
                        .statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json")))
                .with(value("tata").type(Type.STRING).name("who")) //
                .build();

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(COLUMN_ID.getKey().toLowerCase(), "0001");
        parameters.put(FROM_CALENDAR_TYPE_PARAMETER, DateCalendarConverter.CalendarUnit.ISO.name());
        parameters.put(TO_CALENDAR_TYPE_PARAMETER, DateCalendarConverter.CalendarUnit.HIJRI.name());
        parameters.put(FROM_MODE, DateCalendarConverter.FROM_MODE_BEST_GUESS);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", HijrahStr3, "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.DATE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(2, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.NEED_STATISTICS_PATTERN));
    }

    @Test
    public void testLitteralMonthsParseDateFromPatterns() {

        List<DatePattern> patterns = Arrays.asList(new DatePattern("M/d/yyyy"), new DatePattern("dd-MMM-yyyy"),
                new DatePattern("MMMM d, yyyy"), new DatePattern("d MMMM yyyy"));
        AbstractChronology chronology = DateCalendarConverter.CalendarUnit.ISO.getCalendarType();

        // given
        String valueWithMonthInCapital = "02-FEB-1978";

        Locale localeUS = Locale.US;

        // then
        assertEquals(patterns.get(1).getPattern(),
                parseDateFromPatterns(valueWithMonthInCapital, patterns, chronology, localeUS));

        // given
        String valueMonthUSShortLitteral = "02-Dec-1997";

        // then
        assertEquals(patterns.get(1).getPattern(),
                parseDateFromPatterns(valueMonthUSShortLitteral, patterns, chronology, localeUS));

        // given
        String valueMonthUSLongLitteral = "2 February 1981";

        // then
        assertEquals(patterns.get(3).getPattern(),
                parseDateFromPatterns(valueMonthUSLongLitteral, patterns, chronology, localeUS));

        // given
        String valueMonthUSLongLitteral2 = "December 3, 2004";

        // then
        assertEquals(patterns.get(2).getPattern(),
                parseDateFromPatterns(valueMonthUSLongLitteral2, patterns, chronology, localeUS));

        // given
        Locale localeFr = Locale.FRANCE;
        String valueMonthFrLongLitteral = "4 Février 2030";

        // then
        assertEquals(patterns.get(3).getPattern(),
                parseDateFromPatterns(valueMonthFrLongLitteral, patterns, chronology, localeFr));

    }

    @Test(expected = DateTimeException.class)
    public void shouldNotParseNull() {

        // given
        List<DatePattern> patterns = Arrays.asList(new DatePattern("M/d/yyyy"), new DatePattern("dd-MMM-yyyy"));
        AbstractChronology chronology = DateCalendarConverter.CalendarUnit.ISO.getCalendarType();
        Locale locale = Locale.US;

        // then
        parseDateFromPatterns(null, patterns, chronology, locale);

    }

    @Test(expected = DateTimeException.class)
    public void testEmptyListPatterns() {

        // given
        String value = "02-Dec-197";
        List<DatePattern> patterns = new ArrayList<>();
        AbstractChronology chronology = DateCalendarConverter.CalendarUnit.ISO.getCalendarType();
        Locale locale = Locale.US;

        // then
        parseDateFromPatterns(value, patterns, chronology, locale);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongPatterns() {

        // given
        String value = "02-Dec-197";
        List<DatePattern> patterns = Arrays.asList(new DatePattern("a/bb/cccc"), new DatePattern("tt/zzz/fffff"));
        AbstractChronology chronology = DateCalendarConverter.CalendarUnit.ISO.getCalendarType();
        Locale locale = Locale.US;

        // then
        parseDateFromPatterns(value, patterns, chronology, locale);

    }

    @Test
    public void testConversionJapaneseToISO_ValidDateWithEra() {
        // given
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "0008/10/29 平成");
        final DataSetRow row = new DataSetRow(rowContent);
        row.getRowMetadata().getColumns().get(1).getStatistics().getPatternFrequencies().add(
                new PatternFrequency("yyyy/MM/dd G", 1));

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(COLUMN_ID.getKey(), "0001");
        parameters.put(FROM_CALENDAR_TYPE_PARAMETER, DateCalendarConverter.CalendarUnit.JAPANESE.name());
        parameters.put(TO_CALENDAR_TYPE_PARAMETER, DateCalendarConverter.CalendarUnit.ISO.name());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        // While using ResolverStyle.STRICT, date pattern of input data on Japanese calendar must contain era 'G'
        assertEquals("1996/10/29 AD", row.get("0001"));
    }

    @Test
    public void testConversionJapaneseToISO_ValidDateWithoutEra() {
        // given
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "0008/10/29");
        final DataSetRow row = new DataSetRow(rowContent);
        row.getRowMetadata().getColumns().get(1).getStatistics().getPatternFrequencies().add(
                new PatternFrequency("yyyy/MM/dd", 1));

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(COLUMN_ID.getKey(), "0001");
        parameters.put(FROM_CALENDAR_TYPE_PARAMETER, DateCalendarConverter.CalendarUnit.JAPANESE.name());
        parameters.put(TO_CALENDAR_TYPE_PARAMETER, DateCalendarConverter.CalendarUnit.ISO.name());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        // The date pattern does not contain G, the input cannot be parsed
        assertEquals("0008/10/29", row.get("0001"));
    }

    @Test
    public void testConversionJapaneseToISO_InvalidDateWithEra() {
        // given
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "Lucy");
        rowContent.put("0001", "0008/02/30 平成");
        final DataSetRow row = new DataSetRow(rowContent);
        row.getRowMetadata().getColumns().get(1).getStatistics().getPatternFrequencies().add(
                new PatternFrequency("yyyy/MM/dd G", 1));

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(COLUMN_ID.getKey(), "0001");
        parameters.put(FROM_CALENDAR_TYPE_PARAMETER, DateCalendarConverter.CalendarUnit.JAPANESE.name());
        parameters.put(TO_CALENDAR_TYPE_PARAMETER, DateCalendarConverter.CalendarUnit.ISO.name());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        // February 30 does not exist, can not parse even the Era part exists.
        assertEquals("0008/02/30 平成", row.get("0001"));// invalid date
    }

    private void testConversion(String from, DateCalendarConverter.CalendarUnit fromUnit, String fromPattern, String expected,
                                DateCalendarConverter.CalendarUnit toUnit) {
        // given
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", from);
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.getRowMetadata().getColumns().get(1).getStatistics().getPatternFrequencies()
                .add(new PatternFrequency(fromPattern, 1));

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "foo");
        final DataSetRow row2 = new DataSetRow(rowContent);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(COLUMN_ID.getKey(), "0001");
        parameters.put(FROM_CALENDAR_TYPE_PARAMETER, fromUnit.name());
        parameters.put(TO_CALENDAR_TYPE_PARAMETER, toUnit.name());

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row1.get("0001"));
        assertEquals("foo", row2.get("0001"));
    }

    @Test
    public void testJulianDayToChronology() {
        String ear = " AD";
        testConversion(ModifiedJulianDay, DateCalendarConverter.CalendarUnit.MODIFIED_JULIAN_DAY, null, IsoStr2 + ear,
                DateCalendarConverter.CalendarUnit.ISO);
    }

    @Test
    public void testChronologyToJulianDay() {
        testConversion(IsoStr2, DateCalendarConverter.CalendarUnit.ISO, pattern, JulianDay,
                DateCalendarConverter.CalendarUnit.JULIAN_DAY);
        testConversion(JapaneseStr, DateCalendarConverter.CalendarUnit.JAPANESE, pattern, ModifiedJulianDay,
                DateCalendarConverter.CalendarUnit.MODIFIED_JULIAN_DAY);

        testConversion(HijrahStr, DateCalendarConverter.CalendarUnit.HIJRI, pattern, EpochDay,
                DateCalendarConverter.CalendarUnit.EPOCH_DAY);
        testConversion(MinguoStr, DateCalendarConverter.CalendarUnit.MINGUO, pattern, RataDie,
                DateCalendarConverter.CalendarUnit.RATA_DIE);
        testConversion(ThaiBuddhistStr, DateCalendarConverter.CalendarUnit.THAI_BUDDHIST, pattern, JulianDay,
                DateCalendarConverter.CalendarUnit.JULIAN_DAY);
        testConversion("1858-11-18", DateCalendarConverter.CalendarUnit.ISO, pattern, "1",
                DateCalendarConverter.CalendarUnit.MODIFIED_JULIAN_DAY);
        testConversion("1858-11-18", DateCalendarConverter.CalendarUnit.ISO, pattern, "2400002",
                DateCalendarConverter.CalendarUnit.JULIAN_DAY);
        testConversion("1970-01-01", DateCalendarConverter.CalendarUnit.ISO, pattern, "0",
                DateCalendarConverter.CalendarUnit.EPOCH_DAY);
        testConversion("0001-01-01", DateCalendarConverter.CalendarUnit.ISO, pattern, "1",
                DateCalendarConverter.CalendarUnit.RATA_DIE);
        testConversion("1970-01-01 AD", DateCalendarConverter.CalendarUnit.ISO, "yyyy-MM-dd G", "0",
                DateCalendarConverter.CalendarUnit.EPOCH_DAY);
    }

    @Test
    public void test_apply_inplace() {
        testChronologyToJulianDaySameInstance();
    }

    /**
     * row1 and row2 should use one instance DateCalendarConverter.Just cover the test code and no assert the Mpa 'dateCalendarConverterMap'.
     * Because the Map is private.
     */
    @Test
    public void testChronologyToJulianDaySameInstance() {
        Map<String, String> rowContent = new HashMap<>();
        //row1
        rowContent.put("0000", "David");
        rowContent.put("0001", "1970-01-01");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.getRowMetadata().getColumns().get(1).getStatistics().getPatternFrequencies()
                .add(new PatternFrequency("yyyy-MM-dd", 1));

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "0001-01-01");
        final DataSetRow row2 = new DataSetRow(rowContent);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(COLUMN_ID.getKey(), "0001");
        parameters.put(FROM_CALENDAR_TYPE_PARAMETER, DateCalendarConverter.CalendarUnit.ISO.name());
        parameters.put(TO_CALENDAR_TYPE_PARAMETER, DateCalendarConverter.CalendarUnit.JULIAN_DAY.name());

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("2440588", row1.get("0001"));
        assertEquals("1721426", row2.get("0001"));
    }

    @Test
    public void test_apply_in_newcolumn() {
        Map<String, String> rowContent = new HashMap<>();
        //row1
        rowContent.put("0000", "David");
        rowContent.put("0001", "1970-01-01");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.getRowMetadata().getColumns().get(1).getStatistics().getPatternFrequencies()
                .add(new PatternFrequency("yyyy-MM-dd", 1));

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "0001-01-01");
        final DataSetRow row2 = new DataSetRow(rowContent);

        // row 3
        rowContent = new HashMap<>();
        rowContent.put("0000", "Michel");
        rowContent.put("0001", "");
        final DataSetRow row3 = new DataSetRow(rowContent);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");
        parameters.put("from_calendar_type", DateCalendarConverter.CalendarUnit.ISO.name());
        parameters.put("to_calendar_type", DateCalendarConverter.CalendarUnit.JULIAN_DAY.name());

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), actionRegistry, factory.create(action, parameters));

        // then
        // assert that original column is unchanged:
        assertEquals("1970-01-01", row1.get("0001"));
        assertEquals("0001-01-01", row2.get("0001"));
        assertEquals("", row3.get("0001"));

        // assert that new column is created:
        assertEquals("2440588", row1.get("0002"));
        assertEquals("1721426", row2.get("0002"));
        assertEquals("", row3.get("0002"));
    }

    @Test
    public void should_not_chronologyToJulianDay() {
        testConversion("1970-01-01", DateCalendarConverter.CalendarUnit.ISO, null, "1970-01-01",
                DateCalendarConverter.CalendarUnit.EPOCH_DAY);
    }

    @Test
    public void testJulianDayToEachother() {
        testConversion(JulianDay, DateCalendarConverter.CalendarUnit.JULIAN_DAY, null, RataDie,
                DateCalendarConverter.CalendarUnit.RATA_DIE);
        testConversion(JulianDay, DateCalendarConverter.CalendarUnit.JULIAN_DAY, null, EpochDay,
                DateCalendarConverter.CalendarUnit.EPOCH_DAY);
        testConversion(JulianDay, DateCalendarConverter.CalendarUnit.JULIAN_DAY, null, ModifiedJulianDay,
                DateCalendarConverter.CalendarUnit.MODIFIED_JULIAN_DAY);
        testConversion(ModifiedJulianDay, DateCalendarConverter.CalendarUnit.MODIFIED_JULIAN_DAY, null, JulianDay,
                DateCalendarConverter.CalendarUnit.JULIAN_DAY);
        testConversion(RataDie, DateCalendarConverter.CalendarUnit.RATA_DIE, null, EpochDay,
                DateCalendarConverter.CalendarUnit.EPOCH_DAY);
        testConversion(EpochDay, DateCalendarConverter.CalendarUnit.EPOCH_DAY, null, ModifiedJulianDay,
                DateCalendarConverter.CalendarUnit.MODIFIED_JULIAN_DAY);
        testConversion("1", DateCalendarConverter.CalendarUnit.JULIAN_DAY, null, "-2400000",
                DateCalendarConverter.CalendarUnit.MODIFIED_JULIAN_DAY);
        testConversion("1", DateCalendarConverter.CalendarUnit.MODIFIED_JULIAN_DAY, null, "678577",
                DateCalendarConverter.CalendarUnit.RATA_DIE);
        testConversion("1", DateCalendarConverter.CalendarUnit.EPOCH_DAY, null, "2440589",
                DateCalendarConverter.CalendarUnit.JULIAN_DAY);
        testConversion("1", DateCalendarConverter.CalendarUnit.RATA_DIE, null, "1721426",
                DateCalendarConverter.CalendarUnit.JULIAN_DAY);
    }
}
