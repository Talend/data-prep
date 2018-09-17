// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.aggregation.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.talend.dataprep.validation.OneNotBlank;

/**
 * JavaBean used to model aggregation parameters.
 */
@OneNotBlank({ "preparationId", "datasetId" })
public class AggregationParameters {

    /** The dataset id. */
    private String datasetId;

    /** The preparation id. */
    private String preparationId;

    /** The Step id, if null, 'head' is used. */
    private String stepId = "head";

    /** List of column ids to group by. */
    @NotEmpty
    private List<String> groupBy;

    /** List of aggregation operations to apply. */
    @NotEmpty
    private List<AggregationOperation> operations;

    /** TQL filter */
    private String filter;

    /** Optional sample size (null for the whole thing). */
    private String sampleSize;

    /**
     * Default empty constructor.
     */
    public AggregationParameters() {
        groupBy = new ArrayList<>();
        operations = new ArrayList<>();
    }

    /**
     * @return the DatasetId
     */
    public String getDatasetId() {
        return datasetId;
    }

    /**
     * @param datasetId the datasetId to set.
     */
    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * @return the PreparationId
     */
    public String getPreparationId() {
        return preparationId;
    }

    /**
     * @param preparationId the preparationId to set.
     */
    public void setPreparationId(String preparationId) {
        this.preparationId = preparationId;
    }

    /**
     * @return the StepId
     */
    public String getStepId() {
        return stepId;
    }

    /**
     * @param stepId the stepId to set.
     */
    public void setStepId(String stepId) {
        this.stepId = StringUtils.isBlank(stepId) ? "head" : stepId;
    }

    /**
     * @return the GroupBy
     */
    public List<String> getGroupBy() {
        return groupBy;
    }

    /**
     * @param groupBy the groupBy to set.
     */
    public void setGroupBy(List<String> groupBy) {
        this.groupBy = groupBy;
    }

    /**
     * Add the given group by.
     *
     * @param groupBy the group by to add.
     */
    public void addGroupBy(String groupBy) {
        this.groupBy.add(groupBy);
    }

    /**
     * @return The filter (as TQL) for the aggregation.
     * @see org.talend.dataprep.api.filter.FilterService
     */
    public String getFilter() {
        return filter;
    }

    /**
     * @param filter The filter (as TQL) for the aggregation.
     * @see org.talend.dataprep.api.filter.FilterService
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * @return the Operations
     */
    public List<AggregationOperation> getOperations() {
        return operations;
    }

    /**
     * @param operations the operations to set.
     */
    public void setOperations(List<AggregationOperation> operations) {
        this.operations = operations;
    }

    /**
     * Add the given operation to the parameters.
     *
     * @param operation the operatio to add.
     */
    public void addOperation(AggregationOperation operation) {
        this.operations.add(operation);
    }

    /**
     * @return the SampleSize
     */
    public Long getSampleSize() {
        try {
            return Long.parseLong(sampleSize);
        } catch (NumberFormatException e) {

            return null;
        }
    }

    /**
     * @param sampleSize the sampleSize to set.
     */
    public void setSampleSize(String sampleSize) {
        this.sampleSize = sampleSize;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "AggregationParameters{" + "datasetId='" + datasetId + '\'' + ", preparationId='" + preparationId + '\''
                + ", stepId='" + stepId + '\'' + ", groupBy=" + groupBy + ", operations=" + operations + ", filter="
                + filter + ", sampleSize=" + sampleSize + '}';
    }

}
