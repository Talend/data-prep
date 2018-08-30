package org.talend.dataprep.api.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasetDetailsDTO extends DatasetDTO {

    private List<Preparation> preparations = new ArrayList<>();

    private String encoding;

    private DataSetLocation location;

    private Map<String, String> parameters = new HashMap<>();

    public List<Preparation> getPreparations() {
        return preparations;
    }

    public void setPreparations(List<Preparation> preparations) {
        this.preparations = preparations;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public DataSetLocation getLocation() {
        return location;
    }

    public void setLocation(DataSetLocation location) {
        this.location = location;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Linked Preparation to dataset
     */
    private class Preparation {

        /**
         * The creation date.
         */
        private String id;

        private String name;

        private long nbSteps;

        private long lastModificationDate;

    }
}
