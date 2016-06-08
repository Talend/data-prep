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

package org.talend.dataprep.transformation.api.action.metadata.date;

import static org.junit.Assert.assertEquals;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.*;

import java.io.IOException;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.datepattern.DatePattern;

/**
 * Unit test for the DateParser class.
 * 
 * @see DateParser
 */
public class DateParserTest extends BaseDateTests {

    /** The action to test. */
    @Autowired
    private DateParser action;

    @Test(expected = DateTimeException.class)
    public void shouldNotParseNull() {
        action.parseDateFromPatterns(null, Collections.emptyList());
    }

    @Test
    public void getPatterns_should_remove_invalid_or_empty_then_sort_patterns() throws IOException {
        // given
        final DataSetRow row = getRow("toto", "04/25/1999", "tata");
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_with_different_test_cases.json")); // contains
                                                                                                                                  // valid,
                                                                                                                                  // invalid,
                                                                                                                                  // empty
                                                                                                                                  // patterns
        final List<PatternFrequency> patternFrequencies = row.getRowMetadata().getById("0001").getStatistics()
                .getPatternFrequencies();

        // when
        final List<DatePattern> actual = action.getPatterns(patternFrequencies);

        // then
        final List<DatePattern> expected = new ArrayList<>();
        expected.add(new DatePattern("MM/dd/yyyy", 47));
        expected.add(new DatePattern("MM-dd-yy", 27));
        expected.add(new DatePattern("yyyy", 0));
        assertEquals(expected, actual);
    }

    @Test
    public void parseDateFromPatterns_should_parse_from_multiple_patterns() throws ParseException {
        // given
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        final TemporalAccessor date = LocalDate.of(2015, 8, 17);
        final String expected = dtf.format(date);

        final List<DatePattern> patterns = new ArrayList<>();
        patterns.add(new DatePattern("yyyy/MM/dd", 1));
        patterns.add(new DatePattern("MM-dd-yy", 1));
        patterns.add(new DatePattern("yy/dd/MM", 1));

        // when/then
        assertEquals(expected, dtf.format(action.parseDateFromPatterns("2015/08/17", patterns)));
        assertEquals(expected, dtf.format(action.parseDateFromPatterns("08-17-15", patterns)));
        assertEquals(expected, dtf.format(action.parseDateFromPatterns("15/17/08", patterns)));
    }

    @Test
    public void parseDateFromPatterns_should_parse_independently_of_empty_patterns() throws ParseException {
        // given
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        final TemporalAccessor date = LocalDate.of(2015, 8, 17);
        final String expected = dtf.format(date);

        final List<DatePattern> patterns = new ArrayList<>();
        patterns.add(new DatePattern("", 1));
        patterns.add(new DatePattern("yyyy/MM/dd", 2));

        // when
        final String actual = dtf.format(action.parseDateFromPatterns("2015/08/17", patterns));

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void shouldComputePatternFromDQ() {
        final ColumnMetadata column = getColumn(Type.DATE);
        assertEquals(new DatePattern("d/M/yyyy", 1), action.guessPattern("01/02/2015", column));
        assertEquals(new DatePattern("yyyy-MM-dd", 1), action.guessPattern("2015-01-02", column));
        assertEquals(new DatePattern("9999", 1), action.guessPattern("2015", column));
        assertEquals(new DatePattern("MMMM d yyyy", 1), action.guessPattern("July 14 2015", column));
    }

    @Test(expected = DateTimeException.class)
    public void shouldNotComputePatternFromDQBecauseEmptyValue() {
        final ColumnMetadata column = getColumn(Type.DATE);
        action.guessPattern("", column);
    }

    @Test(expected = DateTimeException.class)
    public void shouldNotComputePatternFromDQBecauseNullValue() {
        final ColumnMetadata column = getColumn(Type.DATE);
        action.guessPattern(null, column);
    }

    @Test(expected = DateTimeException.class)
    public void shouldNotComputePatternFromDQBecauseInvalidValue() {
        final ColumnMetadata column = getColumn(Type.DATE);
        action.guessPattern("not a date", column);
    }

    @Test
    public void shouldUpdateColumnStatisticsWithNewDatePattern() {
        // given
        ColumnMetadata column = getColumn(Type.DATE);
        column.getStatistics().getPatternFrequencies().add(new PatternFrequency("yyyy", 19));

        // when
        action.guessAndParse("01/02/2015", column);

        // then
        final List<PatternFrequency> actual = column.getStatistics().getPatternFrequencies();
        assertEquals(2, actual.size());
        assertEquals(new PatternFrequency("d/M/yyyy", 1), actual.get(1));
    }
}
