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

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.talend.dataprep.validation.OneNotBlank;

/**
 * Parameter for dataset/preparation format
 */
@OneNotBlank({ "preparationId", "datasetId" })
public class ExportParameters {

    /** The export format. */
    @NotNull
    private String exportType;

    /** The preparation id to format. If this is null, datasetId must be set. */
    private String preparationId;

    /** The step id to format at a specific state. If null, the preparation head version will be exported. */
    private String stepId;

    /** The dataset id to format. If this is null, preparationId must be set. */
    private String datasetId;

    private Map<String, String> arguments;

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String format) {
        this.exportType = format;
    }

    public String getPreparationId() {
        return preparationId;
    }

    public void setPreparationId(String preparationId) {
        this.preparationId = preparationId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public Map<String, String> getArguments()
    {
        return arguments;
    }

    public void setArguments( Map<String, String> arguments )
    {
        this.arguments = arguments;
    }
}
