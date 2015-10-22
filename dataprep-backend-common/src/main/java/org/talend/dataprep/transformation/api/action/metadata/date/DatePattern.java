package org.talend.dataprep.transformation.api.action.metadata.date;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 *
 */
public class DatePattern implements Comparable {

    private long occurrences;

    private String pattern;

    private DateTimeFormatter formatter;

    public DatePattern(long occurrences, String pattern) {
        this.occurrences = occurrences;
        this.pattern = pattern;
    }

    public DatePattern(String pattern, DateTimeFormatter formatter) {
        this.pattern = pattern;
        this.formatter = formatter;
    }

    /**
     * @return the Occurences
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
     * @param formatter the formatter to set.
     */
    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(Object o) {

        DatePattern other = (DatePattern) o;

        // needs to call equals in order not to add the same object
        if (this.equals(other)) {
            return 0;
        }

        // 0 must not be returned if patterns have the same occurrences value, otherwise the new one won't be added,
        // by default, return -1
        if (other.getOccurrences() == this.getOccurrences()) {
            return -1;
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
        DatePattern that = (DatePattern) o;
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

    @Override
    public String toString() {
        return "DatePattern{" + "occurrences=" + occurrences + ", pattern='" + pattern + '\'' + '}';
    }
}