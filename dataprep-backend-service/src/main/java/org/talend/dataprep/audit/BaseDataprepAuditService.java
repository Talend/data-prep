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

package org.talend.dataprep.audit;

public interface BaseDataprepAuditService {

    void auditPreparationCreation(String prepName, String prepId, String datasetName, String datasetId,
            String folderId);

    void auditFolderCreation(String folderId, String folderName);

    void auditFolderRename(String folderId, String folderName);
}
