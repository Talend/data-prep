package org.talend.dataprep.schema;

import java.util.HashMap;
import java.util.Map;

/**
 * Javabean that models a CSV value
 */
public class Separator {

    /** The value char. */
    private char value;
    /** Total count of separators. */
    private int totalCount = 0;
    /** Average per line. */
    private double averagePerLine = 0;

    /** Current line count. */
    private Map<Integer, Long> countPerLine = new HashMap<>();

    /** The standard deviation. */
    private double standardDeviation;

    /**
     * Constructor.
     * 
     * @param separator the value to use.
     */
    public Separator(char separator) {
        this.value = separator;
    }

    /**
     * @return the value.
     */
    public char getSeparator() {
        return value;
    }

    /**
     * @return the total count.
     */
    public int getTotalCount() {
        return totalCount;
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
     * Return the count for the given line.
     * 
     * @param lineNumber the wanted line for the count.
     * @return the count for the given line.
     */
    public double getCount(int lineNumber) {
        if (countPerLine.containsKey(lineNumber)) {
            return countPerLine.get(lineNumber);
        }
        return 0;
    }

    /**
     * @return the average per line.
     */
    public double getAveragePerLine() {
        return averagePerLine;
    }

    /**
     * @param averagePerLine the average per line to set.
     */
    public void setAveragePerLine(double averagePerLine) {
        this.averagePerLine = averagePerLine;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "Separator{" + "value=" + value + ", totalCount=" + totalCount + ", averagePerLine=" + averagePerLine + '}';
    }

    /**
     * @param standardDeviation the standard deviation.
     */
    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    /**
     * @return the StandardDeviation
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }
}
