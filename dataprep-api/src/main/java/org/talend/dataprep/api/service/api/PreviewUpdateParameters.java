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

package org.talend.dataprep.api.service.api;

import java.util.List;

import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.Action;

public class PreviewUpdateParameters {

    private Action action;
    private List<Integer> tdpIds;
    private String currentStepId;
    private String updateStepId;
    private String preparationId;
    private ExportParameters.SourceType sourceType = ExportParameters.SourceType.HEAD;

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
     * @return the CurrentStepId
     */
    public String getCurrentStepId() {
        return currentStepId;
    }

    /**
     * @param currentStepId the currentStepId to set.
     */
    public void setCurrentStepId(String currentStepId) {
        this.currentStepId = currentStepId;
    }

    /**
     * @return the UpdateStepId
     */
    public String getUpdateStepId() {
        return updateStepId;
    }

    /**
     * @param updateStepId the updateStepId to set.
     */
    public void setUpdateStepId(String updateStepId) {
        this.updateStepId = updateStepId;
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
     * @return the Sample
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

    /**
     * @return the source type
     */
    public ExportParameters.SourceType getSourceType() {
        return sourceType;
    }

    /**
     * @param sourceType the source type
     */
    public void setSourceType(ExportParameters.SourceType sourceType) {
        this.sourceType = sourceType;
    }
}
