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

package org.talend.dataprep.transformation.preview.api;

import org.talend.dataprep.api.export.ExportParameters;

/**
 * Bean that holds all required data to perform a preview.
 */
public class PreviewParameters {

    /** Old actions to perform to get the base state of the preview. */
    private String baseActions;

    /** New actions to perform to get the new state for the diff. */
    private String newActions;

    /** List of row ids to perform the preview on. */
    private String tdpIds;

    /** Id of the dataset to perform the preview on. */
    private String dataSetId;

    /** Preparation id in case we want the preview on a specific sample */
    private String preparationId;

    /** Source type in case we want the preview on a specific sample */
    private ExportParameters.SourceType sourceType;

    /**
     * Default empty constructor.
     */
    public PreviewParameters() {
        // empty constructor needed for json de/serialization
    }

    /**
     * Default constructor.
     *
     * @param baseActions Actions to perform to get the base state of the preview.
     * @param newActions Actions to perform to get the new state for the diff.
     * @param datasetId Id of the dataset to perform the preview on.
     * @param preparationId Id of the preparation to apply.
     * @param tdpIds List of row ids to perform the preview on.
     * @param sourceType The source type.
     */
    public PreviewParameters(String baseActions, String newActions, String datasetId, String preparationId, String tdpIds, ExportParameters.SourceType sourceType) {
        this();
        this.baseActions = baseActions;
        this.newActions = newActions;
        this.dataSetId = datasetId;
        this.preparationId = preparationId;
        this.tdpIds = tdpIds;
        this.sourceType = sourceType;
    }

    /**
     * @return the BaseActions
     */
    public String getBaseActions() {
        return baseActions;
    }

    /**
     * @return the NewActions
     */
    public String getNewActions() {
        return newActions;
    }

    /**
     * @return the TdpIds
     */
    public String getTdpIds() {
        return tdpIds;
    }

    /**
     * @return the DataSetId
     */
    public String getDataSetId() {
        return dataSetId;
    }

    /**
     * @return the preparation id
     */
    public String getPreparationId() {
        return preparationId;
    }

    /**
     * @return the source type
     */
    public ExportParameters.SourceType getSourceType() {
        return sourceType;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "PreviewParameters{" + "baseActions='" + baseActions + '\'' + ", newActions='" + newActions + '\'' + ", tdpIds='"
                + tdpIds + '\'' + ", dataSetId='" + dataSetId + '\'' + '}';
    }
}
