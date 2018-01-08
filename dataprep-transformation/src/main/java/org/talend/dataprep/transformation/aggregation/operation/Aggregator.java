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

import java.util.function.BiConsumer;

import org.talend.dataprep.api.dataset.row.DataSetRow;
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
