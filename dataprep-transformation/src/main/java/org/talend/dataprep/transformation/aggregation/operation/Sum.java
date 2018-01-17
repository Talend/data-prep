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

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.aggregation.api.AggregationResult;

/**
 * Sum aggregator.
 */
public class Sum extends AbstractAggregator implements Aggregator {

    /**
     * Sum aggregator constructor. Package visible to ensure the use of the factory.
     *
     * @param groupBy group by key.
     * @param columnId column id to aggregate.
     */
    Sum(String groupBy, String columnId) {
        super(groupBy, columnId);
    }

    /**
     * @see java.util.function.BiConsumer#accept(Object, Object)
     */
    @Override
    public void accept(DataSetRow row, AggregationResult result) {
        final String sumKey = row.get(groupBy);

        // skip value not found
        if (StringUtils.isEmpty(sumKey)) {
            return;
        }

        // get the value
        double toAdd;
        try {
            toAdd = Double.parseDouble(row.get(columnId));
        } catch (NumberFormatException e) {
            // skip non number
            return;
        }

        // init the group by in the result
        if (!result.contains(sumKey)) {
            result.put(sumKey, new NumberContext(0d));
        }

        NumberContext context = (NumberContext) result.get(sumKey);
        context.setValue(context.getValue() + toAdd);

    }

    @Override
    public void normalize(AggregationResult result) {
        // Nothing to do
    }
}
