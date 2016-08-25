// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.grants;

import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

/**
 * Checks whether or not current user is allowed to perform the a specified action. User is always allowed to perform
 * the action with this implementation.
 */
@Component
@ConditionalOnSingleCandidate(AccessGrantChecker.class)
public class NoOpAccessGrantChecker implements AccessGrantChecker {

    @Override
    public boolean allowed(RestrictedAction action) {
        return true;
    }
}
