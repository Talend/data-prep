// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.schema.csv;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.talend.dataprep.util.ShannonEntropy;

/**
 * This class performs header and scoring analysis on a separator. It uses {@link CSVFastHeaderAndTypeAnalyzer} to
 * determine the header. It also associates a score to a separator using a heuristic based upon the
 * <a href="https://en.wikipedia.org/wiki/Entropy_(information_theory)">Shanon entropy</a>. The entropy of the separator
 * is computed according to the specified number of lines used as the sample size.
 */
public class SeparatorAnalyzer implements Consumer<Separator> {

    /** Number of lines read in the dataset. */
    private final int numberOfLines;

    /**
     * Some sample lines used to make the score more accurate.
     */
    private final List<String> sampleLines;

    /**
     * Internal comparator used to compare separators between them
     */
    private final SeparatorComparator comparator;

    /**
     * Priority of valid separators
     */
    private final List<Character> priority = Arrays.asList(';', ',', '\t', ' ', '|');

    /**
     * Constructor.
     *
     * @param numberOfLines the number of lines.
     */
    public SeparatorAnalyzer(int numberOfLines, List<String> sampleLines) {
        if (numberOfLines <= 0) {
            throw new IllegalArgumentException("The number of lines must be strictly positive");
        }
        this.numberOfLines = numberOfLines;
        this.sampleLines = sampleLines;
        this.comparator = new SeparatorComparator();
    }

    private int prio(char c1, char c2) {
        int c1Priority = priority.indexOf(c1);
        c1Priority = c1Priority == -1 ? Integer.MAX_VALUE : c1Priority;
        int c2Priority = priority.indexOf(c2);
        c2Priority = c2Priority == -1 ? Integer.MAX_VALUE : c2Priority;
        return Integer.compare(c1Priority, c2Priority);
    }

    /**
     * Returns a positive integer indicating the confidence about the specified separator. {@code 0} is returned if
     * <tt>s</tt>is frequent ( more than half of the number of lines) and is present in the first line. {@code 1} is
     * returned if <tt>s</tt> is frequent and is not present in the first line. {@code 2} is returned if <tt>s</tt> is
     * infrequent and is present in the first line. {@code 3} is returned if the separator is infrequent and is not
     * present in the first line.
     *
     * @param s the specified separator
     * @return a positive integer indicating the confidence about the specified separator
     */
    private int consistencyLevel(Separator s) {
        int result = (s.getCountPerLine().size() > (numberOfLines / 2)) ? 0 : 2;
        result += (s.getCountPerLine().containsKey(1) ? 0 : 1);
        return result;
    }

    /**
     * Returns the frequency of each count (per line) of the separator.
     *
     * @param separator
     * @return
     */
    private Collection<Double> countFrequency(Separator separator) {
        Map<Long, Long> countOccurrences = new HashMap<>();

        if (separator.getCountPerLine().isEmpty()) {
            separator.setScore(Double.MAX_VALUE);
            return Collections.emptyList();
        }
        // count the number of occurrence of each count
        for (Long separatorCount : separator.getCountPerLine().values()) {
            // increment the number of occurrence of the separatorCount
            Long separatorCountOccurrence = countOccurrences.get(separatorCount);
            separatorCountOccurrence = separatorCountOccurrence == null ? 1L : separatorCountOccurrence + 1;
            countOccurrences.put(separatorCount, separatorCountOccurrence);
        }
        int zeroCount = numberOfLines - separator.getCountPerLine().size();
        if (zeroCount > 0) {
            countOccurrences.put(0L, (long) zeroCount);
        }
        return countOccurrences.values().stream().mapToDouble(s -> (double) s / numberOfLines).boxed()
                .collect(Collectors.toList());
    }

    /**
     * Computes the entropy of a separator.
     *
     * The lower the entropy, the better, i.e, the number of occurrence of the separator remains stable while processing
     * the lines, which means that the separator has a huge probability of being the wanted one.
     */
    private double computeEntropy(Separator separator) {
        final Collection<Double> countFrequencies = countFrequency(separator);
        return ShannonEntropy.computeWithFrequencies(countFrequencies);
    }

    /**
     * @see Consumer#accept(Object)
     *
     *
     */
    @Override
    public void accept(Separator separator) {
        separator.setScore(computeEntropy(separator));
        CSVFastHeaderAndTypeAnalyzer csvFastHeaderAndTypeAnalyzer = new CSVFastHeaderAndTypeAnalyzer(sampleLines, separator);
        csvFastHeaderAndTypeAnalyzer.analyze();
        separator.setFirstLineAHeader(csvFastHeaderAndTypeAnalyzer.isFirstLineAHeader());
        separator.setHeaderInfoReliable(csvFastHeaderAndTypeAnalyzer.isHeaderInfoReliable());
        separator.setHeaders(csvFastHeaderAndTypeAnalyzer.getHeaders());
    }

    /**
     * Compares two separator in order to choose the best one.
     *
     * @param s1 the first specified separator
     * @param s2 the second specified separator
     * @return the value {@code 0} if {@code s1} is equal to {@code s2}; a value less than {@code 0} if {@code s1} is
     * better than {@code s2}; and a value greater than {@code 0} if {@code s1} is numerically greater than {@code s2}.
     */
    public int compare(Separator s1, Separator s2) {
        return comparator.compare(s1, s2);
    }

    private class SeparatorComparator implements Comparator<Separator> {

        @Override
        public int compare(Separator s1, Separator s2) {
            if (s1 == null) {
                throw new IllegalArgumentException("The first separator being compared must not be null!");
            }

            if (s2 == null) {
                throw new IllegalArgumentException("The second separator being compared must not be null!");
            }
            double s1Score = s1.getScore();
            double s2Score = s2.getScore();
            int result = 0;

            // if both score are zero or both score are positive and close enough to be compared according to a
            // criterion
            // other than score
            if (Double.compare(0.0, s1Score) == 0 && Double.compare(0.0, s2Score) == 0
                    || (Double.compare(0.0, s1Score) != 0 && Double.compare(0.0, s2Score) != 0
                            && Math.abs(s1Score - s2Score) < (ShannonEntropy.maxEntropy(numberOfLines) / 2))) {
                // choose according to consistency
                result = Integer.compare(consistencyLevel(s1), consistencyLevel(s2));
                if (result == 0) {
                    // choose according to header confidence
                    result = Boolean.compare(s2.isHeaderInfoReliable(), s1.isHeaderInfoReliable());
                    // if both have same header confidence
                    if (result == 0) {
                        // choose the separator having first line as header
                        result = Boolean.compare(s2.isFirstLineAHeader(), s1.isFirstLineAHeader());
                        // if both separators have or do not have first line as header then choose the one with more
                        // columns
                        result = result != 0 ? result : Integer.compare(s2.getHeaders().size(), s1.getHeaders().size());
                    }
                }
            }
            if (result == 0) {
                // f a decision could still not be made then choose the separator with lower score
                result = Double.compare(s1Score, s2Score);
                if (result == 0) {
                    // if separators have similar score then choose according to priority
                    result = prio(s1.getSeparator(), s2.getSeparator());
                    if (result == 0) {
                        // choose arbitrarily the first separator
                        result = -1;
                    }
                }
            }

            return result;
        }
    }
}