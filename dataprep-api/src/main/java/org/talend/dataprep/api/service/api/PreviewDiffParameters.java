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

import org.talend.dataprep.api.export.ExportParameters;

import java.util.List;

/**
 * Bean that models a preview on an "Diff action" request.
 */
public class PreviewDiffParameters {

    private String preparationId;
    private String currentStepId;
    private String previewStepId;
    private List<Integer> tdpIds;
    private ExportParameters.SourceType sourceType = ExportParameters.SourceType.HEAD;

    /** The sample size (null means full dataset/preparation). */
    private Long sample;

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
     * @return the PreviewStepId
     */
    public String getPreviewStepId() {
        return previewStepId;
    }

    /**
     * @param previewStepId the previewStepId to set.
     */
    public void setPreviewStepId(String previewStepId) {
        this.previewStepId = previewStepId;
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
