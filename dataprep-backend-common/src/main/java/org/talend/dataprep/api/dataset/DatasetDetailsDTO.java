package org.talend.dataprep.api.dataset;

import java.util.ArrayList;
import java.util.List;

public class DatasetDetailsDTO extends DatasetDTO {

    private List<Preparation> preparations = new ArrayList<>();

    private String encoding;

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

    /**
     * Linked Preparation to dataset
     */
    public static class Preparation {

        /**
         * The creation date.
         */
        private String id;

        private String name;

        private long nbSteps;

        private long lastModificationDate;

        public Preparation(String id, String name, long nbSteps, long lastModificationDate) {
            this.id = id;
            this.name = name;
            this.nbSteps = nbSteps;
            this.lastModificationDate = lastModificationDate;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getNbSteps() {
            return nbSteps;
        }

        public void setNbSteps(long nbSteps) {
            this.nbSteps = nbSteps;
        }

        public long getLastModificationDate() {
            return lastModificationDate;
        }

        public void setLastModificationDate(long lastModificationDate) {
            this.lastModificationDate = lastModificationDate;
        }

        @Override
        public String toString() {
            return "Preparation{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", nbSteps=" + nbSteps
                    + ", lastModificationDate=" + lastModificationDate + '}';
        }
    }

    @Override
    public String toString() {
        return "DatasetDetailsDTO{" + "preparations=" + preparations + ", encoding='" + encoding + '\'' + '}';
    }
}
