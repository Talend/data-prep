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

package org.talend.dataprep.date;

import static java.time.Instant.ofEpochMilli;
import static java.time.Month.*;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.IsoFields.QUARTER_OF_YEAR;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;

public class DateManipulator {
    /**
     * Supported pace for date ranges
     */
    public enum Pace {
        DAY(86400000L), //
        WEEK(604800000L), //
        MONTH(2678400000L), //
        QUARTER(7884000000L), //
        HALF_YEAR(15768000000L), //
        YEAR(31536000000L), //
        DECADE(315360000000L), //
        CENTURY(3153600000000L);

        /**
         * Number of milliseconds that represents the pace
         */
        private final long time;

        /**
         * Constructor
         *
         * @param time The number of milliseconds in the pace
         */
        Pace(final long time) {
            this.time = time;
        }

        /**
         * Number of milliseconds getter
         *
         * @return The number of milliseconds in the pace
         */
        public long getTime() {
            return time;
        }
    }

    /**
     * It determine the right pace to use. It depends on the range limit and the maximum number of parts that is allowed
     *
     * @param min              The minimum date limit
     * @param max              The maximum date limit
     * @param maxNumberOfParts The maximum number of parts
     * @return The suitable pace to use in the range
     */
    public DateManipulator.Pace getSuitablePace(final LocalDateTime min, final LocalDateTime max, final int maxNumberOfParts) {
        final long allRangeTimetamp = Duration.between(min, max).toMillis();
        return Arrays.stream(Pace.values())
                .filter(pace -> (allRangeTimetamp / pace.getTime()) < (maxNumberOfParts - 1))
                .findFirst()
                .orElse(null);
    }

    /**
     * Compute the starting date of the range where the given date is included for a given pace.
     * <ul>
     * <li>CENTURY : first day of the date century. Ex : 25/11/2011 --> 01/01/2000</li>
     * <li>DECADE : first day of the date decade. Ex: 25/11/2011 --> 01/01/2010</li>
     * <li>YEAR : first day of the date year. Ex: 25/11/2011 --> 01/01/2011</li>
     * <li>HALF_YEAR : first day of the date half year (01/01 or 01/07). Ex: 25/11/2011 --> 01/07/2011</li>
     * <li>QUARTER : first day of the date quarter. Ex: 25/11/2011 --> 01/10/2011</li>
     * <li>TWO_MONTH : first day of the date odd month (month value is odd). Ex: 25/04/2011 --> 01/03/2011</li>
     * <li>MONTH : first day of the date month. Ex: 25/11/2011 --> 01/11/2011</li>
     * <li>TWO_WEEK : first day of the date odd week (week of year number is odd).</li>
     * <li>WEEK : first day of the date week.</li>
     * <li>TWO_DAYS : odd day (day of year number is odd). Ex: 02/01/2011 --> 01/01/2011</li>
     * <li>DAY : the given day</li>
     * </ul>
     *
     * @param date The date that must be included in the rance
     * @param pace The pace representing the range
     * @return The starting range date
     */
    public LocalDateTime getSuitableStartingDate(final LocalDateTime date, final DateManipulator.Pace pace) {
        switch (pace) {
            case CENTURY:
                return getFirstDayOfCentury(date);
            case DECADE:
                return getFirstDayOfDecade(date);
            case YEAR:
                return getFirstDayOfYear(date);
            case HALF_YEAR:
                return getFirstDayOfHalfYear(date);
            case QUARTER:
                return getFirstDayOfQuarter(date);
            case MONTH:
                return getFirstDayOfMonth(date);
            case WEEK:
                return getFirstDayOfWeek(date);
            default:
                return getStartOfDay(date);
        }
    }

    /**
     * Compute the day after the given pace.
     *
     * @param localDate The starting date
     * @param pace      The pace to add
     * @return The day = localDate + pace
     */
    public LocalDateTime getNext(final LocalDateTime localDate, final Pace pace) {
        switch (pace) {
            case CENTURY:
                return localDate.plus(100, ChronoUnit.YEARS);
            case DECADE:
                return localDate.plus(10, ChronoUnit.YEARS);
            case YEAR:
                return localDate.plus(1, ChronoUnit.YEARS);
            case HALF_YEAR:
                return localDate.plus(6, ChronoUnit.MONTHS);
            case QUARTER:
                return localDate.plus(3, ChronoUnit.MONTHS);
            case MONTH:
                return localDate.plus(1, ChronoUnit.MONTHS);
            case WEEK:
                return localDate.plus(1, ChronoUnit.WEEKS);
            case DAY:
                return localDate.plus(1, ChronoUnit.DAYS);
        }
        return localDate;
    }

    /**
     * Compute the first day of the date century
     *
     * @param localDate The reference date
     * @return The first day of the date century
     */
    private LocalDateTime getFirstDayOfCentury(final LocalDateTime localDate) {
        return localDate.with(temporal -> {
            final int year = localDate.getYear() / 100 * 100;
            return LocalDate.from(temporal)
                    .withYear(year)
                    .atStartOfDay()
                    .with(TemporalAdjusters.firstDayOfYear());
        });
    }

    /**
     * Compute the first day of the date decade
     *
     * @param localDate The reference date
     * @return The first day of the date decade
     */
    private LocalDateTime getFirstDayOfDecade(final LocalDateTime localDate) {
        return localDate.with(temporal -> {
            final int year = localDate.getYear() / 10 * 10;
            return LocalDate.from(temporal)
                    .withYear(year)
                    .atStartOfDay()
                    .with(TemporalAdjusters.firstDayOfYear());
        });
    }

    /**
     * Compute the first day of the date year
     *
     * @param localDate The reference date
     * @return The first day of the date year
     */
    private LocalDateTime getFirstDayOfYear(final LocalDateTime localDate) {
        return localDate.with(temporal -> LocalDate.from(temporal)
                .atStartOfDay()
                .with(TemporalAdjusters.firstDayOfYear()));
    }

    /**
     * Compute the first day of the date half year
     *
     * @param localDate The reference date
     * @return The first day of the date half year
     */
    private LocalDateTime getFirstDayOfHalfYear(final LocalDateTime localDate) {
        return localDate.with(temporal -> {
            final Month semesterMonth = localDate.getMonth().getValue() < JULY.getValue() ? JANUARY : JULY;
            return LocalDate.from(temporal)
                    .withMonth(semesterMonth.getValue())
                    .atStartOfDay()
                    .with(TemporalAdjusters.firstDayOfMonth());
        });
    }

    /**
     * Compute the first day of the date quarter
     *
     * @param localDate The reference date
     * @return The first day of the date quarter
     */
    private LocalDateTime getFirstDayOfQuarter(final LocalDateTime localDate) {
        return localDate.with(temporal -> {
            int currentQuarter = YearMonth.from(temporal).get(QUARTER_OF_YEAR);
            switch (currentQuarter) {
                case 1:
                    return LocalDate.from(temporal)
                            .atStartOfDay()
                            .with(TemporalAdjusters.firstDayOfYear());
                case 2:
                    return LocalDate.from(temporal)
                            .atStartOfDay()
                            .withMonth(APRIL.getValue())
                            .with(TemporalAdjusters.firstDayOfMonth());
                case 3:
                    return LocalDate.from(temporal)
                            .atStartOfDay()
                            .withMonth(JULY.getValue())
                            .with(TemporalAdjusters.firstDayOfMonth());
                default:
                    return LocalDate.from(temporal)
                            .atStartOfDay()
                            .withMonth(OCTOBER.getValue())
                            .with(TemporalAdjusters.firstDayOfMonth());
            }
        });
    }

    /**
     * Compute the first day of the date month
     *
     * @param localDate The reference date
     * @return The first day of the date month
     */
    private LocalDateTime getFirstDayOfMonth(final LocalDateTime localDate) {
        return localDate.with(temporal -> LocalDate.from(temporal)
                .atStartOfDay()
                .with(TemporalAdjusters.firstDayOfMonth()));
    }

    /**
     * Compute the first day of the date week
     *
     * @param localDate The reference date
     * @return The first day of the date week
     */
    private LocalDateTime getFirstDayOfWeek(final LocalDateTime localDate) {
        return localDate.with(temporal -> LocalDate.from(temporal)
                .atStartOfDay()
                .with(DayOfWeek.MONDAY));
    }

    /**
     * Compute the start of the provided date
     *
     * @param localDate The reference date
     * @return The start of the provided date
     */
    private LocalDateTime getStartOfDay(final LocalDateTime localDate) {
        return localDate.with(temporal -> LocalDate.from(temporal).atStartOfDay());
    }

    /**
     * Compute the number of milliseconds for UTC timestamp
     *
     * @param localDate The date to convert
     * @return The UTC epoch milliseconds
     */
    public long getUTCEpochMilliseconds(final LocalDateTime localDate) {
        return localDate.toEpochSecond(ZoneOffset.UTC) * 1000;
    }

    /**
     * Create a LocalDateTime from epochMillis by adding system offset.
     * 0L will be converted to 1970-01-01 in the system timezone.
     *
     * @param epochMillis The epoch time in milliseconds
     * @return The local date that correspond to the epoch milliseconds minus the system offset
     */
    public LocalDateTime fromEpochMillisecondsWithSystemOffset(final long epochMillis) {
        //get the offset at the time
        final ZonedDateTime referenceUTC = ZonedDateTime.ofInstant(ofEpochMilli(epochMillis), systemDefault());
        final long offsetMillis = referenceUTC.getOffset().getTotalSeconds() * 1000;

        //create LocalDateTime from epochMillis with the offset
        return LocalDateTime.ofInstant(ofEpochMilli(epochMillis - offsetMillis), systemDefault());
    }
}
