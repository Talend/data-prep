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

import java.util.Map;

public interface BaseDataprepAuditService {

    /**
     * Tells if audit logging is active or not.
     * Can be useful to check before calling an audit method if it implies to pre process data.
     * 
     * @return <code>true</code> is active <code>else</code> if inactive.
     */
    boolean isActive();

    void auditPreparationCreation(String preparationName, String preparationId, String datasetName, String datasetId,
            String folderId);

    void auditPreparationDeletion(String prepId);

    void auditPreparationCopy(String preparationId, String destinationFolderId, String newPreparationName,
            String newPreparationId);

    void auditPreparationMove(String preparationId, String sourceFolderId, String destinationFolderId,
            String preparationNewName);

    void auditPreparationRename(String preparationId, String prepNewName);

    void auditPreparationSampleExport(String preparationId, String stepId, String exportType,
            Map<String, String> exportOptions);

    void auditPreparationFullrunExport(String preparationId, String stepId, String exportType,
            Map<String, String> exportOptions);

    void auditPreparationAddStep(String preparationId, Map<String, Map<String, String>> stepActionsAndParameters);

    void auditPreparationUpdateStep(String preparationId, String modifiedStepId,
            Map<String, Map<String, String>> newStepActionsAndParameters);

    void auditPreparationDeleteStep(String preparationId, String preparationName, String deletedStepId);

    void auditPreparationMoveStep(String preparationId, String preparationName, String movedStepId,
            String parentStepId);

    void auditPreparationCopySteps(String fromPreparationId, String fromPreparationName, String newPreparationId,
            String newPreparationName);

    void auditFolderCreation(String folderId, String folderName, String parentFolderId);

    void auditFolderDeletion(String folderId);

    void auditFolderRename(String folderId, String folderName);

}
