package org.talend.dataprep.helper.objects;

/**
 * POJO representation of a preparation.
 *
 * @author vferreira
 * @since 19/07/17
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
