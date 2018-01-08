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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;

/**
 * Unit test for the DateParser class.
 * @see DateParser
 */
public class DateParserTest {

    /** The action to test. */
    private final DateParser action = new DateParser(new AnalyzerService());

    protected InputStream getDateTestJsonAsStream(String testFileName) {
        return BaseDateTest.class.getResourceAsStream(testFileName);
    }

    @Test(expected = DateTimeException.class)
    public void shouldNotParseNull() {
        action.parseDateFromPatterns(null, Collections.emptyList());
    }

    @Test
    public void getPatterns_should_remove_invalid_or_empty_then_sort_patterns() throws IOException {
        // given
        final DataSetRow row = ActionMetadataTestUtils.getRow("toto", "04/25/1999", "tata");
        ActionMetadataTestUtils.setStatistics(row, "0001", getDateTestJsonAsStream("statistics_with_different_test_cases.json")); //contains valid, invalid, empty patterns
        final List<PatternFrequency> patternFrequencies = row.getRowMetadata().getById("0001").getStatistics().getPatternFrequencies();

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
        //given
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        final TemporalAccessor date = LocalDate.of(2015, 8, 17);
        final String expected = dtf.format(date);

        final List<DatePattern> patterns = new ArrayList<>();
        patterns.add(new DatePattern("yyyy/MM/dd", 1));
        patterns.add(new DatePattern("MM-dd-yy", 1));
        patterns.add(new DatePattern("yy/dd/MM", 1));

        //when/then
        assertEquals(expected, dtf.format(action.parseDateFromPatterns("2015/08/17", patterns)));
        assertEquals(expected, dtf.format(action.parseDateFromPatterns("08-17-15", patterns)));
        assertEquals(expected, dtf.format(action.parseDateFromPatterns("15/17/08", patterns)));
    }

    /**
     * TDQ-14421 an invalid date like as 2017-02-30 should not be parsed to 2017-02-28
     */
    @Test(expected = DateTimeException.class)
    public void shouldNotParseDateFromPatternsOnInvalidDate() throws ParseException {
        final List<DatePattern> patterns = new ArrayList<>();
        patterns.add(new DatePattern("yyyy-MM-dd", 1));
        action.parseDateFromPatterns("2017-02-30", patterns);
    }

    @Test(expected = DateTimeException.class)
    public void shouldNotParseDateFromPatternsEmptyPattern() throws ParseException {
        final List<DatePattern> patterns = new ArrayList<>();
        patterns.add(new DatePattern("", 1));
        action.parseDateFromPatterns("2017-02-28", patterns);
    }

    @Test
    public void parseDateFromPatterns_should_parse_independently_of_empty_patterns() throws ParseException {
        //given
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        final TemporalAccessor date = LocalDate.of(2015, 8, 17);
        final String expected = dtf.format(date);

        final List<DatePattern> patterns = new ArrayList<>();
        patterns.add(new DatePattern("", 1));
        patterns.add(new DatePattern("yyyy/MM/dd", 2));

        //when
        final String actual = dtf.format(action.parseDateFromPatterns("2015/08/17", patterns));

        //then
        assertEquals(expected, actual);
    }

    @Test
    public void shouldComputePatternFromDQ() {
        final ColumnMetadata column = ActionMetadataTestUtils.getColumn(Type.DATE);
        assertEquals(new DatePattern("dd/MM/yyyy", 1), action.guessPattern("01/02/2015", column));
        assertEquals(new DatePattern("d/M/yyyy", 1), action.guessPattern("1/2/2015", column));
        assertEquals(new DatePattern("yyyy-MM-dd", 1), action.guessPattern("2015-01-02", column));
        assertEquals(new DatePattern("9999", 1), action.guessPattern("2015", column));
        assertEquals(new DatePattern("MMMM d yyyy", 1), action.guessPattern("July 14 2015", column));
    }

    @Test(expected = DateTimeException.class)
    public void shouldNotComputePatternFromDQBecauseEmptyValue() {
        final ColumnMetadata column = ActionMetadataTestUtils.getColumn(Type.DATE);
        action.guessPattern("", column);
    }

    @Test(expected = DateTimeException.class)
    public void shouldNotComputePatternFromDQBecauseNullValue() {
        final ColumnMetadata column = ActionMetadataTestUtils.getColumn(Type.DATE);
        action.guessPattern(null, column);
    }

    @Test(expected = DateTimeException.class)
    public void shouldNotComputePatternFromDQBecauseInvalidValue() {
        final ColumnMetadata column = ActionMetadataTestUtils.getColumn(Type.DATE);
        action.guessPattern("not a date", column);
    }

    @Test
    public void shouldUpdateColumnStatisticsWithNewDatePattern() {
        // given
        ColumnMetadata column = ActionMetadataTestUtils.getColumn(Type.DATE);
        column.getStatistics().getPatternFrequencies().add(new PatternFrequency("yyyy", 19));

        // when
        action.guessAndParse("01/02/2015", column);

        // then
        final List<PatternFrequency> actual = column.getStatistics().getPatternFrequencies();
        assertEquals(2, actual.size());
        assertEquals(new PatternFrequency("dd/MM/yyyy", 1), actual.get(1));
    }
}
