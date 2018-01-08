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

package org.talend.dataprep.date;

import static java.time.Month.JANUARY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.talend.dataprep.date.DateManipulator.Pace.*;

import java.time.LocalDateTime;

import org.junit.Test;

public class DateManipulatorTest {

    //------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------getSuitablePace------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void getSuitablePace_should_return_computed_pace() throws Exception {
        testGetSuitablePaceWith(LocalDateTime.of(2015, 1, 1, 0, 0), LocalDateTime.of(2015, 1, 15, 0, 0), 20, DAY);
        testGetSuitablePaceWith(LocalDateTime.of(2015, 1, 1, 0, 0), LocalDateTime.of(2015, 4, 15, 0, 0), 20, WEEK);
        testGetSuitablePaceWith(LocalDateTime.of(2015, 1, 1, 0, 0), LocalDateTime.of(2016, 3, 15, 0, 0), 20, MONTH);
        testGetSuitablePaceWith(LocalDateTime.of(2015, 1, 1, 0, 0), LocalDateTime.of(2019, 1, 15, 0, 0), 20, QUARTER);
        testGetSuitablePaceWith(LocalDateTime.of(2015, 1, 1, 0, 0), LocalDateTime.of(2022, 1, 15, 0, 0), 20, HALF_YEAR);
        testGetSuitablePaceWith(LocalDateTime.of(2015, 1, 1, 0, 0), LocalDateTime.of(2032, 1, 15, 0, 0), 20, YEAR);
        testGetSuitablePaceWith(LocalDateTime.of(2015, 1, 1, 0, 0), LocalDateTime.of(2200, 1, 15, 0, 0), 20, DECADE);
        testGetSuitablePaceWith(LocalDateTime.of(2015, 1, 1, 0, 0), LocalDateTime.of(3015, 1, 15, 0, 0), 20, CENTURY);
        testGetSuitablePaceWith(LocalDateTime.of(0, 1, 1, 0, 0), LocalDateTime.of(1000000, 1, 15, 0, 0), 20, null);
    }

    private void testGetSuitablePaceWith(final LocalDateTime min, final LocalDateTime max, final int maxBins, final DateManipulator.Pace expectedPace) throws Exception {
        //when
        final DateManipulator.Pace pace = DateManipulator.getSuitablePace(min, max, maxBins);

        //then
        assertThat(pace, is(expectedPace));
    }

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------getSuitableStartingDate-------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void getSuitableStartingDate_should_return_computed_starting_range_date() throws Exception {
        testGetSuitableStartingDateWith(LocalDateTime.of(2015, 1, 2, 0, 0), DAY,          LocalDateTime.of(2015, 1, 2, 0, 0));
        testGetSuitableStartingDateWith(LocalDateTime.of(2015, 1, 2, 0, 0), WEEK,         LocalDateTime.of(2014, 12, 29, 0, 0));
        testGetSuitableStartingDateWith(LocalDateTime.of(2015, 1, 2, 0, 0), MONTH,        LocalDateTime.of(2015, 1, 1, 0, 0));
        testGetSuitableStartingDateWith(LocalDateTime.of(2015, 3, 2, 0, 0), QUARTER,      LocalDateTime.of(2015, 1, 1, 0, 0));
        testGetSuitableStartingDateWith(LocalDateTime.of(2015, 8, 2, 0, 0), HALF_YEAR,    LocalDateTime.of(2015, 7, 1, 0, 0));
        testGetSuitableStartingDateWith(LocalDateTime.of(2015, 9, 2, 0, 0), YEAR,         LocalDateTime.of(2015, 1, 1, 0, 0));
        testGetSuitableStartingDateWith(LocalDateTime.of(2015, 1, 2, 0, 0), DECADE,       LocalDateTime.of(2010, 1, 1, 0, 0));
        testGetSuitableStartingDateWith(LocalDateTime.of(2015, 1, 2, 0, 0), CENTURY,      LocalDateTime.of(2000, 1, 1, 0, 0));
    }

    private void testGetSuitableStartingDateWith(final LocalDateTime date, final DateManipulator.Pace pace, final LocalDateTime expectedDate) throws Exception {
        //when
        final LocalDateTime computedDate = DateManipulator.getSuitableStartingDate(date, pace);

        //then
        assertThat(computedDate, is(expectedDate));
    }

    //------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------------getNext---------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void testGetNext_should_return_date_with_added_pace() throws Exception {
        testGetNextWith(LocalDateTime.of(2015, 1, 1, 0, 0), DAY,          LocalDateTime.of(2015, 1, 2, 0, 0));
        testGetNextWith(LocalDateTime.of(2015, 1, 1, 0, 0), WEEK,         LocalDateTime.of(2015, 1, 8, 0, 0));
        testGetNextWith(LocalDateTime.of(2015, 1, 1, 0, 0), MONTH,        LocalDateTime.of(2015, 2, 1, 0, 0));
        testGetNextWith(LocalDateTime.of(2015, 1, 1, 0, 0), QUARTER,      LocalDateTime.of(2015, 4, 1, 0, 0));
        testGetNextWith(LocalDateTime.of(2015, 1, 1, 0, 0), HALF_YEAR,    LocalDateTime.of(2015, 7, 1, 0, 0));
        testGetNextWith(LocalDateTime.of(2015, 1, 1, 0, 0), YEAR,         LocalDateTime.of(2016, 1, 1, 0, 0));
        testGetNextWith(LocalDateTime.of(2015, 1, 1, 0, 0), DECADE,       LocalDateTime.of(2025, 1, 1, 0, 0));
        testGetNextWith(LocalDateTime.of(2015, 1, 1, 0, 0), CENTURY,      LocalDateTime.of(2115, 1, 1, 0, 0));
    }

    private void testGetNextWith(final LocalDateTime date, final DateManipulator.Pace pace, final LocalDateTime expectedDate) throws Exception {
        //when
        final LocalDateTime computedDate = DateManipulator.getNext(date, pace);

        //then
        assertThat(computedDate, is(expectedDate));
    }

    //------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------UTC-----------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void should_return_epoch_millis_in_UTC_timezone() {
        //given
        final LocalDateTime date = LocalDateTime.of(1970, JANUARY, 1, 0, 0);

        //when
        final long epochMillis = DateManipulator.getUTCEpochMilliseconds(date);

        //then
        assertThat(epochMillis, is(0L));
    }

    @Test
    public void should_create_local_date_by_adding_current_offset_to_UTC_timestamp() {
        //given
        final long utcEpochMillis = 0L;

        //when
        final LocalDateTime date = DateManipulator.fromEpochMillisecondsWithSystemOffset(utcEpochMillis);

        //then
        final LocalDateTime expectedDate = LocalDateTime.of(1970, JANUARY, 1, 0, 0); //local timezone
        assertThat(date, is(expectedDate));
    }
}
