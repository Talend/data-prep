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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "audit.log.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpAuditService implements BaseDataprepAuditService {

    @Override
    public void auditPreparationCreation(String prepName, String prepId, String datasetName, String datasetId,
            String folderId) {
        // Do nothing
    }

    @Override
    public void auditPreparationDeletion(String prepId) {
        // Do nothing
    }

    @Override
    public void auditPreparationCopy(String preparationId, String destinationFolderId, String newPreparationName,
            String newPreparationId) {
        // Do nothing
    }

    @Override
    public void auditPreparationMove(String preparationId, String sourceFolderId, String destinationFolderId,
            String preparationNewName) {
        // Do nothing
    }

    @Override
    public void auditPreparationRename(String preparationId, String prepNewName) {
        // Do nothing
    }

    @Override
    public void auditPreparationImport(String preparationId, String preparationName, String folderId) {
        // Do nothing
    }

    @Override
    public void auditPreparationExport(String preparationId, String exportOptions) {
        // Do nothing
    }

    @Override
    public void auditPreparationAddStep(String preparationId,
            Map<String, Map<String, String>> stepActionsAndParameters) {
        // Do nothing
    }

    @Override
    public void auditPreparationUpdateStep(String preparationId, String modifiedStepId,
            Map<String, Map<String, String>> newStepActionsAndParameters) {
        // Do nothing
    }

    @Override
    public void auditPreparationDeleteStep(String preparationId, String preparationName, String deletedStepId) {
        // Do nothing
    }

    @Override
    public void auditPreparationMoveStep(String preparationId, String preparationName, String movedStepId,
            String parentStepId) {
        // Do nothing
    }

    @Override
    public void auditFolderCreation(String folderId, String folderName, String parentFolderId) {
        // Do nothing
    }

    @Override
    public void auditFolderRename(String folderId, String folderName) {
        // Do nothing
    }

    @Override
    public void auditTDataprepRun(String preparationId, String exportParameters) {
        // Do nothing
    }

}
