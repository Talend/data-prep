package org.talend.dataprep.schema.csv;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.talend.dataprep.api.type.Type;

/**
 * Javabean that models a CSV value
 */
public class Separator {

    /** The value char. */
    private char value;

    /** Total count of separators. */
    private int totalCount = 0;

    /** Current line count. */
    private Map<Integer, Long> countPerLine = new HashMap<>();

    /** This separator score. */
    private double score = Double.MAX_VALUE;

    /**
     * is first line identified as a header
     */
    private boolean firstLineAHeader = false;

    /**
     * If some lines does not contains the separator, consider this separator as inconsistent
     */
    private boolean headerInfoReliable = true;

    /**
     * A map associating to each column of the header a type.
     */
    private Map<String, Type> headers = Collections.emptyMap();

    public Map<String, Type> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Type> headers) {
        this.headers = headers;
    }

    public boolean isFirstLineAHeader() {
        return firstLineAHeader;
    }

    public void setFirstLineAHeader(boolean header) {
        this.firstLineAHeader = header;
    }

    public boolean isHeaderInfoReliable() {
        return headerInfoReliable;
    }

    public void setHeaderInfoReliable(boolean headerInfoReliable) {
        this.headerInfoReliable = headerInfoReliable;
    }

    public char getValue() {
        return value;
    }

    public void setValue(char value) {
        this.value = value;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public Map<Integer, Long> getCountPerLine() {
        return countPerLine;
    }

    public void setCountPerLine(Map<Integer, Long> countPerLine) {
        this.countPerLine = countPerLine;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Constructor.
     *
     * @param separator the value to use.
     */
    public Separator(char separator) {
        this.value = separator;
    }

    /**
     * @return the value.Math.log(frequency)
     */
    public char getSeparator() {
        return value;
    }

    /**
     * Add one to the current total count.
     */
    public void incrementCount(int lineNumber) {
        totalCount++;

        if (!countPerLine.containsKey(lineNumber)) {
            countPerLine.put(lineNumber, 0L);
        }
        countPerLine.put(lineNumber, countPerLine.get(lineNumber) + 1);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "Separator{" + "value='" + value + '\'' + ", totalCount=" + totalCount + ", score=" + score + '}';
    }
}