package org.talend.dataprep.schema;

/**
 * Javabean that models a CSV value
 */
public class Separator {

    /** The value char. */
    private char value;

    /** Total count of separators. */
    private int totalCount;

    /** Average per line. */
    private double averagePerLine;

    /**
     * Constructor.
     * 
     * @param separator the value to use.
     */
    public Separator(char separator) {
        this.value = separator;
        totalCount = 0;
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
    int getTotalCount() {
        return totalCount;
    }

    /**
     * Add one to the current total count.
     */
    void totalCountPlusOne() {
        totalCount++;
    }

    /**
     * @return the average per line.
     */
    double getAveragePerLine() {
        return averagePerLine;
    }

    /**
     * @param averagePerLine the average per line to set.
     */
    void setAveragePerLine(double averagePerLine) {
        this.averagePerLine = averagePerLine;
    }

    @Override
    public String toString() {
        return "Separator{" + "value=" + value + ", totalCount=" + totalCount + ", averagePerLine=" + averagePerLine + '}';
    }
}
