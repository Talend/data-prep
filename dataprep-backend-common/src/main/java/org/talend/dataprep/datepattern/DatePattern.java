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

package org.talend.dataprep.datepattern;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Bean that model a date pattern.
 */
public class DatePattern implements Comparable {

    /**
     * Number of occurrences of this date pattern within a dataset.
     */
    private long occurrences;

    /**
     * The date pattern as String.
     */
    private String pattern;

    /**
     * The date pattern formatter.
     */
    @JsonIgnore
    private DateTimeFormatter formatter;

    public DatePattern() {
        // no op only to help jackson
    }

    /**
     * Constructor with the pattern.
     *
     * @param pattern the pattern.
     */
    public DatePattern(final String pattern) {
        this.pattern = pattern;
        this.formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
    }

    /**
     * Constructor from the pattern and occurrence.
     *
     * @param pattern the date pattern.
     * @param occurrences the number of occurrences.
     */
    public DatePattern(final String pattern, final long occurrences) {
        this(pattern);
        this.occurrences = occurrences;
    }

    /**
     * @return the Occurrences
     */
    public long getOccurrences() {
        return occurrences;
    }

    /**
     * @return the Pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @return the Formatter
     */
    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(final Object o) {
        final DatePattern other = (DatePattern) o;

        // needs to call equals in order not to add the same object
        if (this.equals(other)) {
            return 0;
        }

        // 0 must not be returned if patterns have the same occurrences value, otherwise the new one won't be added,
        // by default, return 1. This will keep the current order on the same occurrences.
        if (other.getOccurrences() == this.getOccurrences()) {
            return 1;
        }

        return Long.compare(other.getOccurrences(), this.getOccurrences());
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DatePattern that = (DatePattern) o;
        // this.formatter is not used for comparison since it does not implements properly Object.equals()
        return Objects.equals(occurrences, that.occurrences) && Objects.equals(pattern, that.pattern);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(occurrences, pattern);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "DatePattern{" + "occurrences=" + occurrences + ", pattern='" + pattern + '\'' + '}';
    }
}