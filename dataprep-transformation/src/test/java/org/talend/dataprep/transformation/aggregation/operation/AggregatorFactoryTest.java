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

package org.talend.dataprep.transformation.aggregation.operation;

import org.junit.Test;
import org.talend.dataprep.transformation.aggregation.api.AggregationParameters;

/**
 * Unit test for the AggregatorFactory.
 */
public class AggregatorFactoryTest {

    /** The Factory to test. */
    private AggregatorFactory factory;

    public AggregatorFactoryTest() {
        factory = new AggregatorFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnEmptyAggregator() {
        AggregationParameters parameters = new AggregationParameters();
        parameters.addGroupBy("0000");

        Aggregator actual = factory.get(parameters);
    }
}