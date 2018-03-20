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

package org.talend.dataprep.transformation.aggregation.api;

/**
 * An aggregation operation specify an operator to be applied on a column.
 */
public class AggregationOperation {

    /** The operation column. */
    private String columnId;
    /** The operation operator. */
    private Operator operator;

    /**
     * Default empty constructor needed for json serialization.
     */
    public AggregationOperation() {
    }

    /**
     * Create an aggregation operation with the given parameters.
     *
     * @param columnId the column id to aggregate.
     * @param operator the operator to performed.
     */
    public AggregationOperation(String columnId, Operator operator) {
        this.columnId = columnId;
        this.operator = operator;
    }

    /**
     * @return the ColumnId
     */
    public String getColumnId() {
        return columnId;
    }

    /**
     * @param columnId the columnId to set.
     */
    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    /**
     * @return the Operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * @param operator the operator to set.
     */
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "AggregationOperation{" + "columnId='" + columnId + '\'' + ", operator=" + operator + '}';
    }
}
