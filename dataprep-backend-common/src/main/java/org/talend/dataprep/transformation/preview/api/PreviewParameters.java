package org.talend.dataprep.transformation.preview.api;

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
     * @param tdpIds List of row ids to perform the preview on.
     */
    public PreviewParameters(String baseActions, String newActions, String datasetId, String tdpIds) {
        this();
        this.baseActions = baseActions;
        this.newActions = newActions;
        this.dataSetId = datasetId;
        this.tdpIds = tdpIds;
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
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "PreviewParameters{" + "baseActions='" + baseActions + '\'' + ", newActions='" + newActions + '\'' + ", tdpIds='"
                + tdpIds + '\'' + ", dataSetId='" + dataSetId + '\'' + '}';
    }
}
