package org.talend.dataprep.transformation.aggregation.operation;

/**
 * Base abstract class for aggregator used to factorize code across aggregators.
 */
public abstract class AbstractAggregator {

    /** Group by key. */
    protected String groupBy;

    /** Column id to aggregate. */
    protected String columnId;

    /**
     * Package protected constructor to ensure the factory.
     *
     * @param groupBy group by key.
     * @param columnId column id to aggregate
     */
    AbstractAggregator(String groupBy, String columnId) {
        this.groupBy = groupBy;
        this.columnId = columnId;
    }


}
