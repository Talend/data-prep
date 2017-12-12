package org.talend.dataprep.qa.util.export;

/**
 * Mandatory parameters enumeration.
 * There must be a better way to handle it.
 */
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
