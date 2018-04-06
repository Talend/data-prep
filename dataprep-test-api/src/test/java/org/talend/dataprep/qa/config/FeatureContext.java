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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.format.export.ExportFormatMessage;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.util.OSIntegrationTestUtil;
import org.talend.dataprep.qa.util.PreparationUID;
import org.talend.dataprep.qa.util.folder.FolderUtil;

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

    /** Classify uploaded dataset id by their name (Map< Name, Id >) */
    protected Map<String, String> datasetIdByName = new HashMap<>();

    protected Map<PreparationUID, String> preparationIdByFullName = new HashMap<>();

    protected SortedSet<Folder> folders;

    @Autowired
    private FolderUtil folderUtil;

    @Autowired
    private OSIntegrationTestUtil util;

    private Map<String, File> tempFileByName = new HashMap<>();

    private Map<String, Action> actionByAlias = new HashMap<>();

    private Map<String, ExportFormatMessage[]> parametersByPreparationName = new HashMap<>();

    /**
     * All object store on a feature execution.
     */
    private Map<String, Object> featureContext = new HashMap<>();

    /**
     * Add a suffix to a name depending of the execution instance.
     *
     * @param name the name to suffix.
     * @return the suffixed name.
     */
    public static String suffixName(String name) {
        return name + TI_SUFFIX_UID;
    }

    @PostConstruct
    public void init() {
        folders = folderUtil.getEmptyReverseSortedSet();
    }

    /**
     * Add a suffix to a name depending of the execution instance.
     *
     * @param folderPath to suffix.
     * @return the suffixed folderPath.
     */
    public static String suffixFolderName(String folderPath) {
        // The Home folder does not be suffixed
        if (StringUtils.equals(folderPath, "/")) {
            return folderPath;
        }
        // 2 cases, following the path starts from the root or not
        return folderPath.startsWith("/")
                ? "/" + folderPath.substring(1).replace("/", TI_SUFFIX_UID + "/") + TI_SUFFIX_UID
                : folderPath.replace("/", TI_SUFFIX_UID + "/") + TI_SUFFIX_UID;
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
    public void storePreparationRef(@NotNull String id, @NotNull String name, @NotNull String path) {
        preparationIdByFullName.put( //
                new PreparationUID() //
                        .setName(name) //
                        .setPath(path), //
                id);
    }

    /**
     * Store the information about a preparation movement. In order to delete it later.
     *
     * @param id the preparation id.
     * @param oldName the old preparation name.
     * @param oldPath the old preparation path.
     * @param newName the new preparation name.
     * @param newPath the new preparation path.
     */
    public void storePreparationMove(@NotNull String id, @NotNull String oldName, @NotNull String oldPath,
            @NotNull String newName, @NotNull String newPath) {
        preparationIdByFullName.remove(new PreparationUID().setName(oldName).setPath(oldPath));
        preparationIdByFullName.put(new PreparationUID().setName(newName).setPath(newPath), id);
    }

    /**
     * Remove a preparation reference.
     *
     * @param prepName the preparation name.
     */
    public void removePreparationRef(@NotNull String prepName, @NotNull String prepPath) {
        preparationIdByFullName.remove(new PreparationUID().setPath(prepPath).setName(prepName));
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
        return new ArrayList<>(preparationIdByFullName.values());
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
        String path = util.extractPathFromFullName(preparationName);
        String name = util.extractNameFromFullName(preparationName);
        return preparationIdByFullName.get(new PreparationUID().setName(name).setPath(path));
    }

    /**
     * Get the id of a stored preparation.
     *
     * @param preparationName the name of the searched preparation.
     *
     * @return the preparation id.
     */
    @Nullable
    public String getPreparationId(@NotNull String preparationName, @NotNull String folder) {
        return preparationIdByFullName.get(new PreparationUID().setName(preparationName).setPath(folder));
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
        preparationIdByFullName.clear();
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
     * Store folders in order to delete them later.
     *
     * @param pFolders the folders to store.
     */
    public void storeFolder(@NotNull Set<Folder> pFolders) { //
        pFolders.forEach(f -> folders.add(f));
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
