// ============================================================================
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
package org.talend.dataprep.transformation.actions.date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.chrono.AbstractChronology;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for DateCalendarConverter action. Creates one consumer, and test it.
 *
 * @see DateCalendarConverter
 */
public class DateCalendarConverterTest extends BaseDateTest {

    /** The action to test. */
    private DateCalendarConverter action = new DateCalendarConverter();

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
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
        assertThat(name, is("date_calendar_converter"));
    }

    @Test
    public void shouldGetParameters() throws Exception {
        // given
        List<String> parameterNames = Arrays.asList("to_calender_type", "from_calender_type", "from_pattern_mode", "new_pattern", //$NON-NLS-3$ //$NON-NLS-4$
                "column_id", "row_id", "scope", "filter");

        // when
        final List<Parameter> parameters = action.getParameters();

        // then
        assertNotNull(parameters);
        assertEquals(6, parameters.size()); // 4 implicit parameters + 4 specific
        final List<String> expectedParametersNotFound = parameters.stream().map(Parameter::getName) //
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

    private static final String HijrahStr1 = "1417/06/16";

    private static final String HijrahStr3 = "06/16/1417";

    private static final String JapaneseStr1 = "0008/10/29";

    private static final String MinguoStr1 = "0085/10/29";

    private static final String ThaiBuddhistStr1 = "2539/10/29";

    private static final String ThaiBuddhistStr2 = "2539-10-29";

    @Test
    public void testBlank_values() {
        testConversion(null, DateCalendarConverter.ChronologyUnit.ISO, pattern, null,
                DateCalendarConverter.ChronologyUnit.HIJRI);

        testConversion("", DateCalendarConverter.ChronologyUnit.ISO, pattern, "",
                DateCalendarConverter.ChronologyUnit.HIJRI);
    }

    @Test
    public void testConversion_all_custom_patterns() {
        testConversion(IsoStr, DateCalendarConverter.ChronologyUnit.ISO, pattern, HijrahStr,
                DateCalendarConverter.ChronologyUnit.HIJRI);
        testConversion(IsoStr, DateCalendarConverter.ChronologyUnit.ISO, pattern, JapaneseStr,
                DateCalendarConverter.ChronologyUnit.JAPANESE);
        testConversion(IsoStr1, DateCalendarConverter.ChronologyUnit.ISO, pattern1, MinguoStr1,
                DateCalendarConverter.ChronologyUnit.MINGUO);
        testConversion(IsoStr, DateCalendarConverter.ChronologyUnit.ISO, pattern, ThaiBuddhistStr2,
                DateCalendarConverter.ChronologyUnit.THAI_BUDDHIST);
        testConversion(IsoStr, DateCalendarConverter.ChronologyUnit.ISO, pattern, IsoStr2,
                DateCalendarConverter.ChronologyUnit.ISO);
    }

    @Test
    public void testConversion_month() {
        testConversion("01/09/2015", DateCalendarConverter.ChronologyUnit.ISO, "dd/MM/yyyy", "17/11/1436",
                DateCalendarConverter.ChronologyUnit.HIJRI);
        testConversion("01/Sep/2015", DateCalendarConverter.ChronologyUnit.ISO, "dd/MMM/yyyy", "17/ذو القعدة/1436",
                DateCalendarConverter.ChronologyUnit.HIJRI);
    }

    @Test
    public void testConversion_out_of_range_calendar() {
        testConversion("12/5/15", DateCalendarConverter.ChronologyUnit.ISO, "MM/dd/yyyy", "12/5/15",
                DateCalendarConverter.ChronologyUnit.HIJRI);
    }

    @Test
    public void testConversion_only_input_custom() {
        testConversion(IsoStr, DateCalendarConverter.ChronologyUnit.ISO, pattern, HijrahStr,
                DateCalendarConverter.ChronologyUnit.HIJRI);
        testConversion(JapaneseStr1, DateCalendarConverter.ChronologyUnit.JAPANESE, pattern1, HijrahStr2,
                DateCalendarConverter.ChronologyUnit.HIJRI);
        testConversion(MinguoStr1, DateCalendarConverter.ChronologyUnit.MINGUO, pattern1, HijrahStr2,
                DateCalendarConverter.ChronologyUnit.HIJRI);
        testConversion(ThaiBuddhistStr1, DateCalendarConverter.ChronologyUnit.THAI_BUDDHIST, pattern1, HijrahStr2,
                DateCalendarConverter.ChronologyUnit.HIJRI);

        testConversion(MinguoStr, DateCalendarConverter.ChronologyUnit.MINGUO, pattern, HijrahStr,
                DateCalendarConverter.ChronologyUnit.HIJRI);
        testConversion(ThaiBuddhistStr, DateCalendarConverter.ChronologyUnit.THAI_BUDDHIST, pattern, HijrahStr,
                DateCalendarConverter.ChronologyUnit.HIJRI);
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
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0001");
        parameters.put(DateCalendarConverter.FROM_CALENDER_TYPE_PARAMETER, DateCalendarConverter.ChronologyUnit.ISO.name());
        parameters.put(DateCalendarConverter.TO_CALENDER_TYPE_PARAMETER, DateCalendarConverter.ChronologyUnit.HIJRI.name());
        parameters.put(DateCalendarConverter.FROM_MODE, DateCalendarConverter.FROM_MODE_BEST_GUESS);

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
        assertFalse(action.acceptField(getColumn(Type.INTEGER)));
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
        AbstractChronology chronology = DateCalendarConverter.ChronologyUnit.ISO.getCalendarType();

        // given
        String valueWithMonthInCapital = "02-FEB-1978";

        Locale localeUS = Locale.US;

        // then
        assertEquals(patterns.get(1).getPattern(),
                action.parseDateFromPatterns(valueWithMonthInCapital, patterns, chronology, localeUS));

        // given
        String valueMonthUSShortLitteral = "02-Dec-1997";

        // then
        assertEquals(patterns.get(1).getPattern(),
                action.parseDateFromPatterns(valueMonthUSShortLitteral, patterns, chronology, localeUS));

        // given
        String valueMonthUSLongLitteral = "2 February 1981";

        // then
        assertEquals(patterns.get(3).getPattern(),
                action.parseDateFromPatterns(valueMonthUSLongLitteral, patterns, chronology, localeUS));

        // given
        String valueMonthUSLongLitteral2 = "December 3, 2004";

        // then
        assertEquals(patterns.get(2).getPattern(),
                action.parseDateFromPatterns(valueMonthUSLongLitteral2, patterns, chronology, localeUS));

        // given
        Locale localeFr = Locale.FRANCE;
        String valueMonthFrLongLitteral = "4 Février 2030";

        // then
        assertEquals(patterns.get(3).getPattern(),
                action.parseDateFromPatterns(valueMonthFrLongLitteral, patterns, chronology, localeFr));

    }

    @Test(expected = DateTimeException.class)
    public void shouldNotParseNull() {

        // given
        List<DatePattern> patterns = Arrays.asList(new DatePattern("M/d/yyyy"), new DatePattern("dd-MMM-yyyy"));
        AbstractChronology chronology = DateCalendarConverter.ChronologyUnit.ISO.getCalendarType();
        Locale locale = Locale.US;

        // then
        action.parseDateFromPatterns(null, patterns, chronology, locale);

    }

    @Test(expected = DateTimeException.class)
    public void testEmptyListPatterns() {

        // given
        String value = "02-Dec-197";
        List<DatePattern> patterns = new ArrayList<>();
        AbstractChronology chronology = DateCalendarConverter.ChronologyUnit.ISO.getCalendarType();
        Locale locale = Locale.US;

        // then
        action.parseDateFromPatterns(value, patterns, chronology, locale);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongPatterns() {

        // given
        String value = "02-Dec-197";
        List<DatePattern> patterns = Arrays.asList(new DatePattern("a/bb/cccc"), new DatePattern("tt/zzz/fffff"));
        AbstractChronology chronology = DateCalendarConverter.ChronologyUnit.ISO.getCalendarType();
        Locale locale = Locale.US;

        // then
        action.parseDateFromPatterns(value, patterns, chronology, locale);

    }


    private void testConversion(String from, DateCalendarConverter.ChronologyUnit fromUnit, String fromPattern, String expected,
                                DateCalendarConverter.ChronologyUnit toUnit) {
        // given
        // row 1
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
        parameters.put("column_id", "0001");
        parameters.put("from_calender_type", fromUnit.name());
        parameters.put("to_calender_type", toUnit.name());

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row1.get("0001"));
        assertEquals("foo", row2.get("0001"));
    }

}
