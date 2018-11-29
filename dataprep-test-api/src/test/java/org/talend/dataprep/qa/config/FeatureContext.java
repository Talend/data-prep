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

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
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
    private static final String TI_SUFFIX_UID = "_" + Long.toString(Math.round(Math.random() * 1000000));

    private static boolean USE_SUFFIX = true;

    /** Classify dataset id by their suffixed name (Map< Name, Id >) */
    protected Map<String, String> datasetIdByName = new HashMap<>();

    /** Classify uploaded dataset id by their suffixed name (Map< Name, Id >) in order to delete them later. */
    protected Map<String, String> datasetIdByNameToDelete = new HashMap<>();

    /** Classify uploaded preparation id by their full suffixed name (Map< Name, Id >) in order to delete them later. */
    protected Map<PreparationUID, String> preparationIdByFullNameToDelete = new HashMap<>();

    /** Classify preparation id by their full suffixed name (Map< Name, Id >). */
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
        return name + getSuffix();
    }

    public static String removeSuffixName(String name) {
        return name.substring(0, name.indexOf(TI_SUFFIX_UID));
    }

    /**
     * Add suffix to a filename. This suffix is added before the filename extensions. For example "/tmp/file.csv" become
     * "/tmp/file_654321.csv".
     *
     * @param fileName The filename.
     *
     * @return The suffixed filename.
     */
    public static String suffixFileName(String fileName) {
        if (fileName.lastIndexOf('.') < 0
                || (fileName.lastIndexOf('/') >= 0 && fileName.lastIndexOf('.') < fileName.lastIndexOf('/'))) {
            return suffixName(fileName);
        }
        return fileName.substring(0, fileName.lastIndexOf('.')) + TI_SUFFIX_UID
                + fileName.substring(fileName.lastIndexOf('.'));
    }

    /**
     * Remove the suffix from the filename.
     *
     * @param fileName The suffixed filename.
     *
     * @return The filename without suffix.
     */
    public static String removeSuffixFileName(String fileName) {
        return fileName.replaceAll(TI_SUFFIX_UID, "");
    }

    /**
     * Add a suffix to a name depending of the execution instance.
     *
     * @param folderPath to suffix.
     * @return the suffixed folderPath.
     */
    public static String suffixFolderName(String folderPath) {
        // The Home folder does not be suffixed
        if (folderPath.isEmpty() || StringUtils.equals(folderPath, "/")) {
            return folderPath;
        }
        String cleanedFolderPath = folderPath;
        StringBuilder suffixedfolderPath = new StringBuilder();
        if (folderPath.startsWith("/")) {
            cleanedFolderPath = cleanedFolderPath.substring(1);
            suffixedfolderPath.append("/");
        }
        if (folderPath.endsWith("/")) {
            cleanedFolderPath = cleanedFolderPath.substring(0, cleanedFolderPath.length() - 1);
            suffixedfolderPath.append(cleanedFolderPath.replace("/", getSuffix() + "/"));
            suffixedfolderPath.append(getSuffix());
            suffixedfolderPath.append("/");
        } else {
            suffixedfolderPath.append(cleanedFolderPath.replace("/", getSuffix() + "/"));
            suffixedfolderPath.append(getSuffix());
        }

        return suffixedfolderPath.toString();
    }

    public static String getSuffix() {
        return USE_SUFFIX ? TI_SUFFIX_UID : StringUtils.EMPTY;
    }

    public static boolean isUseSuffix() {
        return USE_SUFFIX;
    }

    public static void setUseSuffix(boolean UseSuffix) {
        USE_SUFFIX = UseSuffix;
    }

    @PostConstruct
    public void init() {
        folders = folderUtil.getEmptyReverseSortedSet();
    }

    /**
     * Store a new dataset reference. In order to delete it later.
     *
     * @param id the dataset id.
     * @param name the dataset name.
     */
    public void storeDatasetRef(String id, String name) {
        storeExistingDatasetRef(id, name);
        datasetIdByNameToDelete.put(name, id);
    }

    /**
     * Remove a dataset reference from the context.
     *
     * @param datasetName the dataset name.
     */
    public void removeDatasetRef(String datasetName) {
        datasetIdByName.remove(datasetName);
        datasetIdByNameToDelete.remove(datasetName);
    }

    /**
     * Store an existing dataset reference.
     *
     * @param id the dataset id.
     * @param name the dataset name.
     */
    public void storeExistingDatasetRef(String id, String name) {
        datasetIdByName.put(name, id);
    }

    /**
     * Store a new preparation reference. In order to delete it later.
     *
     * @param id the preparation id.
     * @param name the preparation name.
     */
    public void storePreparationRef(String id, String name, String path) {
        storeExistingPreparationRef(id, name, path);
        preparationIdByFullNameToDelete.put( //
                new PreparationUID() //
                        .setName(name) //
                        .setPath(path), //
                id);
    }

    /**
     * Store an existing preparation reference.
     *
     * @param id the preparation id.
     * @param name the preparation name.
     */
    public void storeExistingPreparationRef(String id, String name, String path) {
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
    public void storePreparationMove(String id, String oldName, String oldPath, String newName, String newPath) {
        // Note : it's inferred that the moved preparation is a created one and not a loaded one (see
        // #storeExistingPreparationRef())
        preparationIdByFullName.remove(new PreparationUID().setName(oldName).setPath(oldPath));
        preparationIdByFullNameToDelete.remove(new PreparationUID().setName(oldName).setPath(oldPath));
        preparationIdByFullName.put(new PreparationUID().setName(newName).setPath(newPath), id);
        preparationIdByFullNameToDelete.put(new PreparationUID().setName(newName).setPath(newPath), id);
    }

    /**
     * Remove a preparation reference.
     *
     * @param prepName the preparation name.
     */
    public void removePreparationRef(String prepName, String prepPath) {
        preparationIdByFullNameToDelete.remove(new PreparationUID().setPath(prepPath).setName(prepName));
    }

    /**
     * Store a temporary {@link File}.
     *
     * @param file the temporary {@link File} to store.
     */
    public void storeTempFile(String filename, File file) {
        tempFileByName.put(filename, file);
    }

    /**
     * Store an {@link Action}.
     *
     * @param alias the {@link Action} alias.
     * @param action the {@link Action} to store.
     */
    public void storeAction(String alias, Action action) {
        actionByAlias.put(alias, action);
    }

    /**
     * List all created dataset id.
     *
     * @return a {@link List} of all created dataset id.
     */
    public List<String> getDatasetIdsToDelete() {
        return new ArrayList<>(datasetIdByNameToDelete.values());
    }

    /**
     * List all created preparation id.
     *
     * @return a {@link List} of all created preparation id.
     */
    public List<String> getPreparationIdsToDelete() {
        return new ArrayList<>(preparationIdByFullNameToDelete.values());
    }

    /**
     * Get the id of a stored dataset.
     *
     * @param datasetName the name of the searched dataset.
     * @return the dataset id.
     */
    public String getDatasetId(String datasetName) {
        return datasetIdByName.get(datasetName);
    }

    /**
     * Get the id of a stored preparation.
     *
     * @param preparationName the name of the searched preparation.
     * @return the preparation id.
     */
    public String getPreparationId(String preparationName) {
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
    public String getPreparationId(String preparationName, String folder) {
        return preparationIdByFullName.get(new PreparationUID().setName(preparationName).setPath(folder));
    }

    /**
     * Get a stored temporary {@link File}.
     *
     * @param fileName the stored temporary {@link File}.
     * @return the temporary stored {@link File}.
     */
    public File getTempFile(String fileName) {
        return tempFileByName.get(fileName);
    }

    /**
     * Get a stored {@link Action}.
     *
     * @param alias the stored {@link Action} alias.
     * @return the stored {@link Action}.
     */
    public Action getAction(String alias) {
        return actionByAlias.get(alias);
    }

    /**
     * Clear the lists of dataset.
     */
    public void clearDatasetLists() {
        datasetIdByName.clear();
        datasetIdByNameToDelete.clear();
    }

    /**
     * Clear the list of preparation.
     */
    public void clearPreparationLists() {
        preparationIdByFullName.clear();
        preparationIdByFullNameToDelete.clear();
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

    public void storeObject(String key, Object object) {
        featureContext.put(key, object);
    }

    public void removeObject(String key) {
        featureContext.remove(key);
    }

    public Object getObject(String key) {
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
    public void storeFolder(Set<Folder> pFolders) { //
        pFolders.forEach(f -> folders.add(f));
    }

    /**
     * Retreive the list of stored folders.
     */
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
