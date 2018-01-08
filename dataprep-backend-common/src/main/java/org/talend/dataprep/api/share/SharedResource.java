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

package org.talend.dataprep.api.share;

import java.util.Set;

/**
 * Interface used to share code across different shared resource.
 */
public interface SharedResource {

    /**
     * Set the owner information.
     * 
     * @param owner the resource owner.
     */
    void setOwner(Owner owner);

    /**
     * Set the shared resource flag.
     * 
     * @param shared the shared resource flag value.
     */
    void setSharedResource(boolean shared);

    /**
     * Set the sharedByMe resource flag.
     * 
     * @param sharedByMe the shared resource flag value.
     */
    void setSharedByMe(boolean sharedByMe);

    /**
     * Set the current user role on this resource.
     * 
     * @param roles the roles as a list of string.
     */
    void setRoles(Set<String> roles);

    /**
     * @return this shared resource owner/author id.
     */
    String getOwnerId();

    /**
     * @return the resource id.
     */
    String getId();

}
