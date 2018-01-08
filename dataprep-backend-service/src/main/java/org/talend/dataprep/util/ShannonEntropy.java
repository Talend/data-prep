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

package org.talend.dataprep.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Computes the <a href="https://en.wikipedia.org/wiki/Entropy_(information_theory)">Shannon entropy</a>.
 */
public class ShannonEntropy {

    /**
     * Computes the Shannon entropy according to the frequencies of values specified in <tt>frequencies</tt>.
     *
     * @param frequencies the collection containing the frequencies of values
     * @return the shannon compute
     */
    public static double computeWithFrequencies(Collection<Double> frequencies) {
        if (frequencies.contains(0D)) {
            throw new IllegalArgumentException("Values with null frequency are not permitted when computing Shannon entropy");
        }

        if (frequencies.isEmpty()) {
            return Double.MAX_VALUE;
        }
        double entropy = frequencies.stream().mapToDouble(d -> d).map(s -> s * Math.log(s)).sum();
        entropy = (-entropy / Math.log(2));

        return Math.abs(entropy);
    }

    /**
     * Computes the Shannon entropy of values specified in <tt>values</tt>.
     *
     * @param values the list of occurring values
     * @param <T> the types of values specified in <tt>values</tt>
     * @return
     */
    public static <T> double computeWithValues(Collection<T> values) {
        final Map<T, Integer> map = new HashMap<T, Integer>();
        // count the occurrences of each value
        for (T t : values) {
            if (!map.containsKey(t)) {
                map.put(t, 0);
            }
            map.put(t, map.get(t) + 1);
        }

        Collection<Double> frequencies = map.values().stream().mapToDouble(s -> (double) s / values.size()).boxed()
                .collect(Collectors.toList());
        return computeWithFrequencies(frequencies);
    }

    /**
     * Computes the maximum value of a entropy every possible value as the same probability to appear compared to another value and
     * <tt>cardinality</tt> represents the number of possible values considered.
     * @param cardinality represents the number of possible values considered
     * @return the maximun entropy
     */
    public static double maxEntropy(int cardinality){
        if (cardinality < 0){
            throw new IllegalArgumentException("The cardinality must be positive");
        }
        double equiprobility = (double) 1 / cardinality;
        Collection<Double> frequencies = new ArrayList<>();
        for (int i = 0; i < cardinality; i++){
            frequencies.add(equiprobility);
        }

        return computeWithFrequencies(frequencies);
    }

}
