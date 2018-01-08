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

import org.junit.Assert;
import org.junit.Test;
import org.talend.dataprep.transformation.aggregation.api.AggregationResult;
import org.talend.dataprep.transformation.aggregation.api.Operator;

/**
 * Unit test for Sum aggregator.
 * 
 * @see Sum
 */
public class SumTest extends OperationBaseTest {

    /**
     * Constructor.
     */
    public SumTest() {
        super(Operator.SUM);
    }

    @Test
    public void shouldComputeSum() {
        // given when
        AggregationResult result = new AggregationResult(Operator.SUM);
        aggregator.accept(getRow("toto", "514.3"), result);
        aggregator.accept(getRow("toto", "0"), result);
        aggregator.accept(getRow("toto", ""), result);
        aggregator.accept(getRow("toto", "-786.25"), result);
        aggregator.accept(getRow("toto", "235874"), result);
        aggregator.accept(getRow("toto", "-8760"), result);

        // then
        Assert.assertEquals(result.get("toto").getValue(), 226842.05, 0);

    }

    @Test
    public void shouldNormalizeHaveNoEffect() {
        // given when
        AggregationResult result = new AggregationResult(Operator.SUM);
        aggregator.accept(getRow("toto", "514.3"), result);
        aggregator.accept(getRow("toto", "0"), result);
        aggregator.accept(getRow("toto", ""), result);
        aggregator.accept(getRow("toto", "-786.25"), result);
        aggregator.accept(getRow("toto", "235874"), result);
        aggregator.accept(getRow("toto", "-8760"), result);
        aggregator.normalize(result); // No effect for operation

        // then
        Assert.assertEquals(result.get("toto").getValue(), 226842.05, 0);
    }

}