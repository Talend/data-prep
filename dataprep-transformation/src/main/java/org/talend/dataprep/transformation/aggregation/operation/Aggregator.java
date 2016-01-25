package org.talend.dataprep.transformation.aggregation.operation;

import java.util.function.BiConsumer;

import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.aggregation.api.AggregationResult;

/**
 * Base interface for all aggregators.
 *
 * So far, Aggregators are stateful hence not meant to be used across multiple aggregations at the same time.
 */
public interface Aggregator extends BiConsumer<DataSetRow, AggregationResult> {

    /**
     * Allow aggregators to perform normalization operations on result (e.g. remove invalid values that can only be
     * detected at end of aggregation).
     * @param result The {@link AggregationResult result} to normalize.
     */
    void normalize(AggregationResult result);
}
