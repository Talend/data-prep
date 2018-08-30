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

import org.talend.logging.audit.AuditEvent;
import org.talend.logging.audit.EventAuditLogger;

public interface DataprepEventAuditLogger extends EventAuditLogger {

    @AuditEvent(category = "activity", message = "Preparations were listed successfully")
    void preparationsListed(Object... args);
}
