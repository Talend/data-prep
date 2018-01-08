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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ShannonEntropyTest {

    @Test(expected = IllegalArgumentException.class)
    public void should_not_compute_entropy_for_a_value_with_frequency_of_zero() {
        // given
        List frequencies = Arrays.asList(0.25, 0.25, 0.25, 0.0);

        // when
        ShannonEntropy.computeWithFrequencies(frequencies);
        // then
    }

    @Test
    public void should_return_two_when_four_values_are_equiprobable() {
        // given
        List values = Arrays.asList(0, 1, 2, 3);
        List frequencies = Arrays.asList(0.25, 0.25, 0.25, 0.25);

        // when
        double valuesEntropy = ShannonEntropy.computeWithValues(values);
        double frequenciesEntropy = ShannonEntropy.computeWithFrequencies(frequencies);

        // then
        Assert.assertEquals(2, valuesEntropy, 0.0);
        Assert.assertEquals(2, frequenciesEntropy, 0.0);
    }

    @Test
    public void should_return_two_when_two_values_are_equiprobable() {
        // given
        List values = Arrays.asList(0, 1);
        List frequencies = Arrays.asList(0.5, 0.5);

        // when
        double valuesEntropy = ShannonEntropy.computeWithValues(values);
        double frequenciesEntropy = ShannonEntropy.computeWithFrequencies(frequencies);

        // then
        Assert.assertEquals(1, valuesEntropy, 0.0);
        Assert.assertEquals(1, frequenciesEntropy, 0.0);
    }

    @Test
    public void should_return_two_when_each_of_four_values_appears_as_much_as_its_value() {
        // given
        List values = Arrays.asList(1, 2, 2, 3, 3, 3, 4, 4, 4, 4);
        List frequencies = Arrays.asList(0.1, 0.2, 0.3, 0.4);

        // when
        double valuesEntropy = ShannonEntropy.computeWithValues(values);
        double frequenciesEntropy = ShannonEntropy.computeWithFrequencies(frequencies);

        // then
        Assert.assertEquals(1.8464393446710157, valuesEntropy, 0.0000000000001);
        Assert.assertEquals(1.8464393446710157, frequenciesEntropy, 0.0000000000001);
    }

}