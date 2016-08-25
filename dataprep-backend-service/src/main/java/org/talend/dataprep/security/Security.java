//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.Set;

import org.talend.dataprep.api.user.UserGroup;

public interface Security {

    /**
     * @return Get user id based on the user name from Spring Security context, return "anonymous" if no user is
     * currently logged in.
     */
    String getUserId();

    /**
     * @return the current user display name, e.g. first name + last name.
     */
    default String getUserDisplayName() {
        return getUserId();
    }

    /**
     * @return an authentication token.
     */
    String getAuthenticationToken();

    /**
     * @return the user groups.
     */
    Set<UserGroup> getGroups();
}
