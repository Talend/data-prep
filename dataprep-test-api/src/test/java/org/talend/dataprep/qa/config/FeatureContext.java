// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.qa.config;

import java.io.File;
import java.util.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.talend.dataprep.format.export.ExportFormatMessage;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.qa.dto.Folder;

/**
 * Used to share data within steps.
 */
@Component
public class FeatureContext {

    /**
     * Prefix used to build storage key, for fullrun references.
     */
    public static final String FULL_RUN_PREFIX = "fullrun-";

    /**
     * Suffix used to differentiate persisted TDP items during parallel IT runs.
     */
    private static String TI_SUFFIX_UID = "_" + Long.toString(Math.round(Math.random() * 1000000));

    private Map<String, String> datasetIdByName = new HashMap<>();

    // TODO : Refactoring : various preparation can have the same name but in different folder
    // TODO : change the data model (Map<String, List<String>> ?)
    private Map<String, String> preparationIdByName = new HashMap<>();

    private Map<String, File> tempFileByName = new HashMap<>();

    private Map<String, Action> actionByAlias = new HashMap<>();

    private Map<String, ExportFormatMessage[]> parametersByPreparationName = new HashMap<>();

    private SortedSet<Folder> folders = new TreeSet<>((o1, o2) -> {
        // reverse order : the longer string is the first one.
        if (o1 == null && o2 == null)
            return 0;
        if (o1 == null)
            return 1;
        if (o2 == null)
            return -1;
        return ((Integer) o2.path.length()).compareTo(o1.path.length());
    });

    /**
     * All object store on a feature execution.
     */
    private Map<String, Object> featureContext = new HashMap<>();

    /**
     * Add a suffix to a name depending of the execution instance.
     *
     * @param name the to suffix.
     * @return the suffixed name.
     */
    public static String suffixName(String name) {
        return name + TI_SUFFIX_UID;
    }

    /**
     * Store a new dataset reference. In order to delete it later.
     *
     * @param id the dataset id.
     * @param name the dataset name.
     */
    public void storeDatasetRef(@NotNull String id, @NotNull String name) {
        datasetIdByName.put(name, id);
    }

    /**
     * Store a new preparation reference. In order to delete it later.
     *
     * @param id the preparation id.
     * @param name the preparation name.
     */
    public void storePreparationRef(@NotNull String id, @NotNull String name) {
        preparationIdByName.put(name, id);
    }

    /**
     * Store a temporary {@link File}.
     *
     * @param file the temporary {@link File} to store.
     */
    public void storeTempFile(@NotNull String filename, @NotNull File file) {
        tempFileByName.put(filename, file);
    }

    /**
     * Store an {@link Action}.
     *
     * @param alias the {@link Action} alias.
     * @param action the {@link Action} to store.
     */
    public void storeAction(@NotNull String alias, @NotNull Action action) {
        actionByAlias.put(alias, action);
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
     * Get a stored temporary {@link File}.
     *
     * @param fileName the stored temporary {@link File}.
     * @return the temporary stored {@link File}.
     */
    @Nullable
    public File getTempFile(@NotNull String fileName) {
        return tempFileByName.get(fileName);
    }

    /**
     * Get a stored {@link Action}.
     *
     * @param alias the stored {@link Action} alias.
     * @return the stored {@link Action}.
     */
    public Action getAction(@NotNull String alias) {
        return actionByAlias.get(alias);
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

    /**
     * Clear the list of temporary {@link File}.
     */
    public void clearTempFile() {
        tempFileByName.clear();
    }

    /**
     * Clear the list of {@link Action}}.
     */
    public void clearAction() {
        actionByAlias.clear();
    }

    public void storeObject(@NotNull String key, @NotNull Object object) {
        featureContext.put(key, object);
    }

    public void removeObject(@NotNull String key) {
        featureContext.remove(key);
    }

    public Object getObject(@NotNull String key) {
        return featureContext.get(key);
    }

    public void clearObject() {
        featureContext.clear();
    }

    /**
     * Store a folder.
     *
     * @param folder the folder to store.
     */
    public void storeFolder(@NotNull Folder folder) {
        folders.add(folder);
    }

    /**
     * Retreive the list of stored folders.
     */
    @NotNull
    public Set<Folder> getFolders() {
        return folders;
    }

    /**
     * Clear the list of folders.
     */
    public void clearFolders() {
        folders.clear();
    }

    public void storePreparationExportFormat(String preparationName, ExportFormatMessage[] parameters) {
        parametersByPreparationName.put(preparationName, parameters);
    }

    public void clearPreparationExportFormat() {
        parametersByPreparationName.clear();
    }

    public ExportFormatMessage[] getExportFormatsByPreparationName(String preparationName) {
        return parametersByPreparationName.get(preparationName);
    }

}
