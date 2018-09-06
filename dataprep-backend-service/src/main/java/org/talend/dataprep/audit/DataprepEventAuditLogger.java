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

import org.talend.logging.audit.*;

public interface DataprepEventAuditLogger extends EventAuditLogger {

    @AuditEvent(category = "activity", message = "Preparation has been created")
    void preparationCreated(Object... args);

    @AuditEvent(category = "activity", message = "Folder has been created")
    void folderCreated(Object... args);

    @AuditEvent(category = "activity", message = "Folder has been renamed")
    void folderRename(Object... args);

    @AuditEvent(category = "activity", message = "Folder has been shared")
    void folderShare(Object... args);
}
