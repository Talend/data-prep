package org.talend.dataprep.qa.bean;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Used to share data within steps.
 */
@Component
public class FeatureContext {

    private Map<String, String> datasetNameById = new HashMap<>();

    /**
     * Store a new dataset reference. In order to delete it later.
     *
     * @param id   the dataset id.
     * @param name the dataset name.
     */
    public void storeDatasetRef(String id, String name) {
        datasetNameById.put(id, name);
    }

    /**
     * List all created dataset id.
     *
     * @return a {@link Set} of all created dataset id.
     */
    public Set<String> getDatasetIdList() {
        return datasetNameById.keySet();
    }
}
