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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.logging.audit.StandardEventAuditLogger;

@Component
@ConditionalOnProperty(name = "audit.log.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpAuditLogger implements StandardEventAuditLogger {

    @Override
    public void loginSuccess(Object... args) {

    }

    @Override
    public void loginFail(Object... args) {

    }

    @Override
    public void userLockout(Object... args) {

    }

    @Override
    public void userCreated(Object... args) {

    }

    @Override
    public void userModified(Object... args) {

    }

    @Override
    public void userDeleted(Object... args) {

    }

    @Override
    public void passwordChanged(Object... args) {

    }

    @Override
    public void passwordReset(Object... args) {

    }

    @Override
    public void roleCreated(Object... args) {

    }

    @Override
    public void roleDeleted(Object... args) {

    }

    @Override
    public void roleAssigned(Object... args) {

    }

    @Override
    public void roleRevoked(Object... args) {

    }

    @Override
    public void invalidInput(Object... args) {

    }

    @Override
    public void invalidSession(Object... args) {

    }

    @Override
    public void systemException(Throwable throwable, Object... args) {

    }
}
