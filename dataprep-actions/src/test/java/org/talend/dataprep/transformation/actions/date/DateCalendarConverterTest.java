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
package org.talend.dataprep.transformation.actions.date;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.*;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.*;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.*;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.*;

import java.io.IOException;
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
        assertEquals(8, parameters.size()); // 4 implicit parameters + 4 specific
        final List<String> expectedParametersNotFound = parameters.stream().map(Parameter::getName) //
                .filter(n -> !parameterNames.contains(n)).collect(Collectors.toList());
        assertTrue(expectedParametersNotFound.toString() + " not found", expectedParametersNotFound.isEmpty());
    }

    private static final String pattern = "yyyy-MM-dd";

    private static final String IsoStr = "1996-10-29";

    private static final String HijrahStr = "1417-06-16";

    private static final String JapaneseStr = "0008-10-29";

    private static final String MinguoStr = "0085-10-29";

    private static final String ThaiBuddhistStr = "2539-10-29";

    private static final String pattern1 = "yyyy/MM/dd";

    private static final String IsoStr1 = "1996/10/29";

    private static final String HijrahStr1 = "1417/06/16";

    private static final String JapaneseStr1 = "0008/10/29";

    private static final String MinguoStr1 = "0085/10/29";

    private static final String ThaiBuddhistStr1 = "2539/10/29";

    @Test
    public void testConversion_all_custom_patterns() {
        testConversion(IsoStr, DateCalendarConverter.ChronologyUnit.ISO, pattern, true, HijrahStr,
                DateCalendarConverter.ChronologyUnit.Hijrah, pattern, true);
        testConversion(IsoStr, DateCalendarConverter.ChronologyUnit.ISO, pattern, true, JapaneseStr,
                DateCalendarConverter.ChronologyUnit.Japanese, pattern, true);
        testConversion(IsoStr1, DateCalendarConverter.ChronologyUnit.ISO, pattern1, true, MinguoStr1,
                DateCalendarConverter.ChronologyUnit.Minguo, pattern1, true);
        testConversion(IsoStr, DateCalendarConverter.ChronologyUnit.ISO, pattern, true, ThaiBuddhistStr1,
                DateCalendarConverter.ChronologyUnit.ThaiBuddhist, pattern1, true);
        testConversion(IsoStr, DateCalendarConverter.ChronologyUnit.ISO, pattern, true, IsoStr1,
                DateCalendarConverter.ChronologyUnit.ISO, pattern1, true);
    }

    @Test
    public void testConversion_only_input_custom() {
        testConversion(IsoStr, DateCalendarConverter.ChronologyUnit.ISO, pattern, true, HijrahStr,
                DateCalendarConverter.ChronologyUnit.Hijrah, pattern, false);
        testConversion(JapaneseStr1, DateCalendarConverter.ChronologyUnit.Japanese, pattern1, true, HijrahStr,
                DateCalendarConverter.ChronologyUnit.Hijrah, pattern, false);
        testConversion(MinguoStr1, DateCalendarConverter.ChronologyUnit.Minguo, pattern1, true, HijrahStr,
                DateCalendarConverter.ChronologyUnit.Hijrah, pattern, false);
        testConversion(ThaiBuddhistStr1, DateCalendarConverter.ChronologyUnit.ThaiBuddhist, pattern1, true, HijrahStr,
                DateCalendarConverter.ChronologyUnit.Hijrah, pattern, false);

        testConversion(MinguoStr, DateCalendarConverter.ChronologyUnit.Minguo, pattern, true, HijrahStr,
                DateCalendarConverter.ChronologyUnit.Hijrah, pattern, false);
        testConversion(ThaiBuddhistStr, DateCalendarConverter.ChronologyUnit.ThaiBuddhist, pattern, true, HijrahStr,
                DateCalendarConverter.ChronologyUnit.Hijrah, pattern, false);
    }

    @Test
    public void testConversion_only_output_custom() throws IOException {
        // given
        final DataSetRow row = builder()
                .with(value("toto").type(Type.STRING).name("recipe"))
                .with(value("10/29/1996").type(Type.DATE).name("last update")
                        .statistics(getDateTestJsonAsStream("statistics_MM_dd_yyyy.json")))
                .with(value("tata").type(Type.STRING).name("who")) //
                .build();

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0001");
        parameters.put(DateCalendarConverter.FROM_CALENDER_TYPE_PARAMETER, DateCalendarConverter.ChronologyUnit.ISO.name());
        parameters.put(DateCalendarConverter.TO_CALENDER_TYPE_PARAMETER, DateCalendarConverter.ChronologyUnit.Hijrah.name());
        parameters.put(DateCalendarConverter.FROM_MODE, DateCalendarConverter.FROM_MODE_BEST_GUESS);
        parameters.put(DateCalendarConverter.NEW_PATTERN, pattern1);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", HijrahStr1, "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    private void testConversion(String from, DateCalendarConverter.ChronologyUnit fromUnit, String fromPattern,
            boolean isFromCustomPattern, String expected, DateCalendarConverter.ChronologyUnit toUnit, String toPattern,
            boolean isToCustomPattern) {
        // given
        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", from);
        final DataSetRow row1 = new DataSetRow(rowContent);

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

        if (isFromCustomPattern) {
            parameters.put("from_pattern_mode", "from_custom_mode");
            parameters.put("from_custom_pattern", fromPattern);
        } else {
            parameters.put("from_pattern_mode", "unknown_separators");
        }

        if (isToCustomPattern) {
            parameters.put("new_pattern", "custom");
            parameters.put("custom_date_pattern", toPattern);
        } else {
            parameters.put("new_pattern", toPattern);
        }

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row1.get("0001"));
        assertEquals("foo", row2.get("0001"));
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

}
