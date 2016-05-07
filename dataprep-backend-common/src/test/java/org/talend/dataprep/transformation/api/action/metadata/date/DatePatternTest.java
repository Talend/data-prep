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

import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import org.talend.dataprep.datepattern.DatePattern;

public class DatePatternTest {

    @Test
    public void datePattern_should_store_occurrences() {
        //given
        final String pattern = "d/MM/yyyy";
        final DatePattern dp = new DatePattern( pattern, 5);

        //when
        final long occurrences = dp.getOccurrences();

        //then
        assertThat(occurrences, is(5L));
    }

    @Test
    public void datePattern_should_store_pattern() {
        //given
        final String pattern = "d/MM/yyyy";
        final DatePattern dp = new DatePattern(pattern, 5);

        //when
        final String storedPattern = dp.getPattern();

        //then
        assertThat(storedPattern, is(pattern));
    }

    @Test
    public void datePattern_should_create_DateFormatter() {
        //given
        final String pattern = "d/MM/yyyy";
        final DatePattern dp = new DatePattern(pattern);

        final LocalDateTime date = LocalDateTime.of(2016, JANUARY, 25, 0, 0);

        //when
        final String formattedDate = date.format(dp.getFormatter());

        //then
        assertThat(formattedDate, is("25/01/2016"));
    }

    @Test
    public void datePattern_should_set_DateFormatter_local_to_ENGLISH() {
        //given
        final String pattern = "d-MMM-yyyy";
        final DatePattern dp = new DatePattern(pattern);

        final LocalDateTime date = LocalDateTime.of(2016, JULY, 25, 0, 0);

        //when
        final String formattedDate = date.format(dp.getFormatter());

        //then
        assertThat(formattedDate, is("25-Jul-2016"));
    }

    @Test
    public void datePattern_natural_equals_compare_should_return_0() {
        //given
        final String pattern = "d-MMM-yyyy";
        final DatePattern dp1 = new DatePattern(pattern, 25);
        final DatePattern dp2 = new DatePattern(pattern, 25);

        //when
        final int compareResult = dp1.compareTo(dp2);

        //then
        assertThat(compareResult, is(0));
    }

    @Test
    public void datePattern_compare_on_same_occurrences_should_return_1() {
        //given
        final DatePattern dp1 = new DatePattern("d-MMM-yyyy", 25);
        final DatePattern dp2 = new DatePattern("d/MM/yyyy", 25);

        //when
        final int compareResult = dp1.compareTo(dp2);

        //then
        assertThat(compareResult, is(1));
    }

    @Test
    public void datePattern_natural_order_should_be_desc_occurrences() {
        //given
        final DatePattern dp1 = new DatePattern("d-MMM-yyyy", 5);
        final DatePattern dp2 = new DatePattern("d/MM/yyyy", 25);
        final DatePattern dp3 = new DatePattern("d/MM/yyyy", 1);
        final List<DatePattern> patterns = Lists.newArrayList(dp1, dp2, dp3);

        //when
        Collections.sort(patterns);

        //then
        assertThat(patterns, contains(dp2, dp1, dp3));
    }

    @Test
    public void toString_should_return_a_readable_description() {
        //given
        final DatePattern dp = new DatePattern("d-MMM-yyyy", 5);

        //when
        final String description = dp.toString();

        //then
        assertThat(description, is("DatePattern{occurrences=5, pattern='d-MMM-yyyy'}"));
    }
}