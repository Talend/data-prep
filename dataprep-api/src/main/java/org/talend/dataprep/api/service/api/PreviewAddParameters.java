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

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.validation.OneNotBlank;

/**
 * Bean that models a preview on an "Add action" request.
 */
@OneNotBlank({"preparationId", "datasetId"})
public class PreviewAddParameters {

    /** The action to preview. */
    @NotNull
    private List<Action> actions;

    /** The list of lines to preview. */
    @NotEmpty
    private List<Integer> tdpIds;

    /** The dataset ID to work on. */
    private String datasetId;

    /** The preparation id to work on. */
    private String preparationId;

    /** The sample size (null means full dataset/preparation). */
    private Long sample;

    /** The dataset sample source type */
    private ExportParameters.SourceType sourceType = ExportParameters.SourceType.HEAD;

    /**
     * @return the Action
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * @param actions the action to set.
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
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

    @Override
    public String toString() {
        return "PreviewAddParameters{" + "actions=" + actions + ", tdpIds=" + tdpIds + ", datasetId='" + datasetId + '\''
                + ", preparationId='" + preparationId + '\'' + ", sample=" + sample + '}';
    }
}
