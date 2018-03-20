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

import org.springframework.stereotype.Service;
import org.talend.dataprep.transformation.aggregation.api.AggregationOperation;
import org.talend.dataprep.transformation.aggregation.api.AggregationParameters;

/**
 * Create on demand aggregator.
 */
@Service
public class AggregatorFactory {

    /**
     * Return the first aggregator out of the given parameters.
     *
     * @param parameters the aggregation parameters.
     * @return the first aggregator out of the given parameters or an empty aggregator if there's none.
     */
    public Aggregator get(AggregationParameters parameters) {

        // return empty aggregator if empty
        if (parameters.getOperations().isEmpty() || parameters.getGroupBy().isEmpty()) {
            throw new IllegalArgumentException("Invalid aggregation parameters");
        }

        final AggregationOperation operation = parameters.getOperations().get(0);
        String groupBy = parameters.getGroupBy().get(0);

        switch (operation.getOperator()) {
        case AVERAGE:
            return new Average(groupBy, operation.getColumnId());
        case MIN:
            return new Min(groupBy, operation.getColumnId());
        case MAX:
            return new Max(groupBy, operation.getColumnId());
        case SUM:
            return new Sum(groupBy, operation.getColumnId());
        default:
            throw new IllegalArgumentException("Operation '" + operation.getOperator() + "' not supported");
        }
    }
}
