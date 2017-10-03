package org.talend.dataprep.helper.api;

/**
 * POJO representation of a preparation creation request.
 */
public class PreparationRequest {

    private String dataSetId;
    private String name;

    public PreparationRequest(String dataSetId, String name) {
        this.dataSetId = dataSetId;
        this.name = name;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
