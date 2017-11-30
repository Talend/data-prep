package org.talend.dataprep.qa.util.export;

public enum MandatoryParameters {
    EXPORT_TYPE("exportType"), //
    PREPARATION_NAME("preparationName"), //
    DATASET_NAME("dataSetName"); //

    private String name;

    MandatoryParameters(String pName) {
        name = pName;
    }

    public String getName() {
        return name;
    }
}
