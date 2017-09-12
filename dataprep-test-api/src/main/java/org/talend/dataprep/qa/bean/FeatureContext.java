package org.talend.dataprep.qa.bean;

import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Used to share data within steps.
 */
@Component
public class FeatureContext {

    private Map<String, String> datasetIdByName = new HashMap<>();

    private Map<String, String> preparationIdByName = new HashMap<>();

    /**
     * Store a new dataset reference. In order to delete it later.
     *
     * @param id   the dataset id.
     * @param name the dataset name.
     */
    public void storeDatasetRef(@NotNull String id, @NotNull String name) {
        datasetIdByName.put(name, id);
    }

    /**
     * Store a new preparation reference. In order to delete it later.
     *
     * @param id   the preparation id.
     * @param name the preparation name.
     */
    public void storePreparationRef(@NotNull String id, @NotNull String name) {
        preparationIdByName.put(name, id);
    }

    /**
     * List all created dataset id.
     *
     * @return a {@link List} of all created dataset id.
     */
    @NotNull
    public List<String> getDatasetIds() {
        return new ArrayList<>(datasetIdByName.values());
    }

    /**
     * List all created preparation id.
     *
     * @return a {@link List} of all created preparation id.
     */
    @NotNull
    public List<String> getPreparationIds() {
        return new ArrayList<>(preparationIdByName.values());
    }

    /**
     * List all created dataset name.
     *
     * @return a {@link Set} of all created dataset name.
     */
    @NotNull
    public List<String> getDatasetNames() {
        return new ArrayList<>(datasetIdByName.keySet());
    }

    /**
     * Get the id of a stored dataset.
     *
     * @param datasetName the name of the searched dataset.
     * @return the dataset id.
     */
    @Nullable
    public String getDatasetId(@NotNull String datasetName) {
        return datasetIdByName.get(datasetName);
    }

    /**
     * Get the id of a stored preparation.
     *
     * @param preparationName the name of the searched preparation.
     * @return the preparation id.
     */
    @Nullable
    public String getPreparationId(@NotNull String preparationName) {
        return preparationIdByName.get(preparationName);
    }

    /**
     * Clear the list of dataset.
     */
    public void clearDataset() {
        datasetIdByName.clear();
    }

    /**
     * Clear the list of preparation.
     */
    public void clearPreparation() {
        preparationIdByName.clear();
    }
}
