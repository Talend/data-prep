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

package org.talend.dataprep.schema.csv;

import static org.junit.Assert.*;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.junit.Test;
import org.talend.dataprep.api.type.Type;

/**
 * Unit test for Separator analyzer.
 */
public class SeparatorAnalyzerTest {

    @Test(expected = IllegalArgumentException.class)
    public void should_check_line_number() throws Exception {
        new SeparatorAnalyzer(-1, null);
    }

    @Test
    public void should_set_default_score() {
        SeparatorAnalyzer analyzer = new SeparatorAnalyzer(100, Arrays.asList("first", "last"));
        Separator sep = new Separator('|');
        analyzer.accept(sep);
        assertEquals(Double.MAX_VALUE, sep.getScore(), 0.000000001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_illegal_argument_exception_when_first_separator_is_null() {
        // given
        final Separator semiColonSeparator = new Separator(';');

        // when
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(1, Collections.emptyList());

        // then
        separatorAnalyzer.compare(null, semiColonSeparator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_illegal_argument_exception_when_second_separator_is_null() {
        // given
        final Separator semiColonSeparator = new Separator(';');

        // when
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(1, Collections.emptyList());

        // then
        separatorAnalyzer.compare(semiColonSeparator, null);
    }

    @Test
    public void should_return_char_with_more_priority() {
        // given
        final Separator semiColonSeparator = new Separator(';');
        final Separator commaSeparator = new Separator(',');
        final Separator spaceSeparator = new Separator(' ');
        final Separator tabulationSeparator = new Separator('\t');
        final Separator invalidSeparator = new Separator('A');

        // when
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(1, Collections.emptyList());

        // then
        assertTrue(separatorAnalyzer.compare(semiColonSeparator, commaSeparator) < 0);
        assertTrue(separatorAnalyzer.compare(commaSeparator, tabulationSeparator) < 0);
        assertTrue(separatorAnalyzer.compare(tabulationSeparator, spaceSeparator) < 0);
        assertTrue(separatorAnalyzer.compare(spaceSeparator, invalidSeparator) < 0);
    }

    @Test
    public void should_perform_right_analysis() {
        // given
        final Separator semiColonSeparator = new Separator(';');
        semiColonSeparator.incrementCount(1);
        semiColonSeparator.incrementCount(2);
        final Separator commaSeparator = new Separator(';');
        commaSeparator.incrementCount(2);

        // when
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(2, Arrays.asList("ID;Name", "1;Toto"));
        separatorAnalyzer.accept(semiColonSeparator);
        separatorAnalyzer.accept(commaSeparator);

        // then
        assertTrue(Double.compare(0.0, semiColonSeparator.getScore()) == 0);
        assertTrue(semiColonSeparator.isFirstLineAHeader());
        assertTrue(semiColonSeparator.isHeaderInfoReliable());
        String[] expectedHeaders = new String[] { "ID", "Name" };
        Object[] header = semiColonSeparator.getHeaders().stream().map( p -> p.getKey()).toArray();
        assertArrayEquals(expectedHeaders, header);

    }

    @Test
    public void should_return_char_with_more_consistency_level() {
        // given
        final Separator semiColonSeparator = new Separator(';');
        final Separator commaSeparator = new Separator(',');
        commaSeparator.incrementCount(1);
        final Separator tabulationSeparator = new Separator('\t');
        tabulationSeparator.incrementCount(2);
        tabulationSeparator.incrementCount(3);
        final Separator spaceSeparator = new Separator(' ');
        spaceSeparator.incrementCount(1);
        spaceSeparator.incrementCount(2);

        // when
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(3, Collections.emptyList());

        // then
        assertTrue(separatorAnalyzer.compare(spaceSeparator, tabulationSeparator) < 0);
        assertTrue(separatorAnalyzer.compare(tabulationSeparator, commaSeparator) < 0);
        assertTrue(separatorAnalyzer.compare(commaSeparator, semiColonSeparator) < 0);

    }

    @Test
    public void should_return_separator_with_more_reliable_header_info() {
        // given
        final Separator semiColonSeparator = new Separator(';');
        final Separator commaSeparator = new Separator(',');
        commaSeparator.setHeaderInfoReliable(true);
        semiColonSeparator.setHeaderInfoReliable(false);

        // when
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(3, Collections.emptyList());

        // then
        assertTrue(separatorAnalyzer.compare(commaSeparator, semiColonSeparator) < 0);

    }

    @Test
    public void should_return_separator_with_first_line_as_header() {
        // given
        final Separator semiColonSeparator = new Separator(';');
        final Separator commaSeparator = new Separator(',');
        commaSeparator.setFirstLineAHeader(true);
        semiColonSeparator.setFirstLineAHeader(false);

        // when
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(3, Collections.emptyList());

        // then
        assertTrue(separatorAnalyzer.compare(commaSeparator, semiColonSeparator) < 0);

    }

    @Test
    public void should_return_separator_with_first_more_columns() {
        // given
        final Separator semiColonSeparator = new Separator(';');
        List<Pair<String, Type>> semiColonHeader = new ArrayList<>();
        semiColonHeader.add(new Pair<>("First", Type.STRING));
        semiColonSeparator.setHeaders(semiColonHeader);
        final Separator commaSeparator = new Separator(',');
        List<Pair<String, Type>> commaHeader = new ArrayList<>();
        commaHeader.add(new Pair<>("First", Type.STRING));
        commaHeader.add(new Pair<>("Last", Type.STRING));
        commaSeparator.setHeaders(commaHeader);

        // when
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(3, Collections.emptyList());

        // then
        assertTrue(separatorAnalyzer.compare(commaSeparator, semiColonSeparator) < 0);

    }

    @Test
    public void should_return_arbitrarily_first_separator() {
        // given
        final Separator invalidSeparator1 = new Separator('A');
        final Separator invalidSeparator2 = new Separator('B');

        // when
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(1, Collections.emptyList());

        // then
        assertTrue(separatorAnalyzer.compare(invalidSeparator1, invalidSeparator2) < 0);
    }

    private void incrementCount(int count, int lineNumber, Separator separator) {
        for (int i = 0; i < count; i++) {
            separator.incrementCount(lineNumber);
        }
    }

}