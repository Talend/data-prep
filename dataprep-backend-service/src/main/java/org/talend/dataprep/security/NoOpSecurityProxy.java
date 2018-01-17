//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.security;

import org.springframework.stereotype.Component;

/**
 * No op implementation of the SecurityProxy.
 */
@Component
public class NoOpSecurityProxy implements SecurityProxy {

    /**
     * @see SecurityProxy#borrowIdentity(String)
     */
    @Override
    public void borrowIdentity(String securityToken) {
        // no op
    }

    /**
     * @see SecurityProxy#asTechnicalUser()
     */
    @Override
    public void asTechnicalUser() {
        // no op
    }

    /**
     * @see SecurityProxy#releaseIdentity()
     */
    @Override
    public void releaseIdentity() {
        // no op
    }
}
