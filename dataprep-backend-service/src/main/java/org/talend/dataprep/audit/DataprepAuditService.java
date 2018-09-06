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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.logging.audit.ContextBuilder;

@Component
public class DataprepAuditService {

    private static final String PREPARATION_NAME_CONTEXT_KEY = "preparationName";

    private static final String PREPARATION_ID_CONTEXT_KEY = "preparationId";

    private static final String DATASET_NAME_CONTEXT_KEY = "datasetName";

    private static final String DATASET_ID_CONTEXT_KEY = "datasetId";

    private static final String TARGET_FOLDER_ID_CONTEXT_KEY = "targetFolderId";

    @Autowired
    private DataprepEventAuditLogger auditLogger;

    public void auditPreparationCreation(String prepName, String prepId, String datasetName, String datasetId,
            String folderId) {
        ContextBuilder builder = ContextBuilder
                .create()
                .with(PREPARATION_NAME_CONTEXT_KEY, prepName)
                .with(PREPARATION_ID_CONTEXT_KEY, prepId)
                .with(DATASET_ID_CONTEXT_KEY, datasetId)
                .with(TARGET_FOLDER_ID_CONTEXT_KEY, folderId);
        if (datasetName != null) {
            builder.with(DATASET_NAME_CONTEXT_KEY, datasetName);
        }
        auditLogger.preparationCreated(builder.build());
    }

}
