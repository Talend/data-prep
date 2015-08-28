package org.talend.dataprep.transformation.aggregation.operation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.dataprep.transformation.aggregation.api.AggregationResult;
import org.talend.dataprep.transformation.aggregation.api.Operator;

/**
 * Unit test for the Average aggregator.
 * 
 * @see Average
 */
public class AverageTest extends OperationBaseTest {

    /**
     * Init the unit test.
     */
    public AverageTest() {
        super(Operator.AVERAGE);
    }

    @Test
    public void shouldComputeAverage() {

        AggregationResult result = new AggregationResult(Operator.AVERAGE);
        aggregator.accept(getRow("toto", "10"), result);
        aggregator.accept(getRow("toto", "2"), result);
        aggregator.accept(getRow("toto", "3.6"), result);
        aggregator.accept(getRow("toto", ""), result);
        aggregator.accept(getRow("toto", "8.2"), result);
        aggregator.accept(getRow("tata", "10"), result);
        aggregator.accept(getRow("toto", "-8"), result);
        aggregator.accept(getRow("toto", "12.3"), result);
        aggregator.accept(getRow("tata", "5"), result);

        final Average.AverageContext toto = (Average.AverageContext) result.get("toto");
        assertEquals(4.683d, toto.getValue(), 0.001d);

        final Average.AverageContext tata = (Average.AverageContext) result.get("tata");
        assertEquals(7.5d, tata.getValue(), 0);

    }


}