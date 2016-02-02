//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.api;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.validation.OneNotBlank;

/**
 * Bean that models a preview on an "Add action" request.
 */
@OneNotBlank({"preparationId", "datasetId"})
public class PreviewAddInput {

    /** The action to preview. */
    @NotNull
    private Action action;

    /** The list of lines to preview. */
    @NotEmpty
    private List<Integer> tdpIds;

    /** The dataset ID to work on. */
    private String datasetId;

    /** The preparation id to work on. */
    private String preparationId;

    /** The sample size (null means full dataset/preparation). */
    private Long sample;

    /**
     * @return the Action
     */
    public Action getAction() {
        return action;
    }

    /**
     * @param action the action to set.
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * @return the TdpIds
     */
    public List<Integer> getTdpIds() {
        return tdpIds;
    }

    /**
     * @param tdpIds the tdpIds to set.
     */
    public void setTdpIds(List<Integer> tdpIds) {
        this.tdpIds = tdpIds;
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
     * @return the sample
     */
    public Long getSample() {
        return sample;
    }

    /**
     * @param sample the sample to set.
     */
    public void setSample(Long sample) {
        this.sample = sample;
    }

    @Override
    public String toString() {
        return "PreviewAddInput{" + "action=" + action + ", tdpIds=" + tdpIds + ", datasetId='" + datasetId + '\''
                + ", preparationId='" + preparationId + '\'' + ", sample=" + sample + '}';
    }
}
