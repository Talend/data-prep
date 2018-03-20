/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.preparation.service;

/**
 * Group any criteria available to filter preparations.
 */
public class PreparationSearchCriterion {

    private String dataSetId;
    private String folderId;
    private String name;
    private boolean nameExactMatch;
    private String folderPath;

    public static PreparationSearchCriterion filterPreparation() {
        return new PreparationSearchCriterion();
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public PreparationSearchCriterion byDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
        return this;
    }

    public String getFolderId() {
        return folderId;
    }

    public PreparationSearchCriterion byFolderId(String folderId) {
        this.folderId = folderId;
        return this;
    }

    public String getName() {
        return name;
    }

    public PreparationSearchCriterion byName(String name) {
        this.name = name;
        return this;
    }

    public boolean isNameExactMatch() {
        return nameExactMatch;
    }

    public PreparationSearchCriterion withNameExactMatch(boolean nameExactMatch) {
        this.nameExactMatch = nameExactMatch;
        return this;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public PreparationSearchCriterion byFolderPath(String folderPath) {
        this.folderPath = folderPath;
        return this;
    }
}
