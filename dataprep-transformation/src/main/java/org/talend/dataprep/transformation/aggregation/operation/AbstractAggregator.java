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
