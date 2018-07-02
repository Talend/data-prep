// ============================================================================
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

package org.talend.dataprep.security;

import org.talend.dataprep.dataset.DatasetConfiguration;

/**
 * Security Proxy to let a thread borrow the identity of a user out of its authentication token.
 */
public interface SecurityProxy {

    /**
     * Let a thread borrow the identity of a user.
     *
     * @param securityToken the security token to use for the proxy identity.
     */
    void borrowIdentity(String securityToken);

    /**
     * Let a thread use the technical user. This method does <b>NOT</b> change the current tenant, meaning current
     * thread will become a technical user for the current tenant.
     */
    void asTechnicalUser();

    /**
     * @deprecated This method should be deleted when the support of dataset legacy mode will be removed
     * @see DatasetConfiguration
     */
    void asTechnicalUserForDataSet();

    /**
     * Release the borrowed identity.
     */
    void releaseIdentity();

}
