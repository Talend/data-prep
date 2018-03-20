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

package org.talend.dataprep.preparation.service;

import java.util.HashSet;
import java.util.Set;

import org.talend.dataprep.api.preparation.PreparationMessage;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.api.share.SharedResource;

public class UserPreparation extends PreparationMessage implements SharedResource {

    /** This preparation owner. */
    private Owner owner;

    /** True if this preparation is shared by another user. */
    private boolean sharedPreparation = false;

    /** True if this preparation is shared by current user. */
    private boolean sharedByMe = false;

    /** What role has the current user on this preparation. */
    private Set<String> roles = new HashSet<>();

    /** The owner id (as of authentication layer) */
    private String ownerId;

    public Owner getOwner() {
        return owner;
    }

    /**
     * @see SharedResource#setOwner(Owner)
     */
    @Override
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @Override
    public void setSharedResource(boolean shared) {
        this.sharedPreparation = shared;
    }

    public boolean isSharedByMe() {
        return sharedByMe;
    }

    @Override
    public void setSharedByMe(boolean sharedByMe) {
        this.sharedByMe = sharedByMe;
    }

    @Override
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isSharedPreparation() {
        return sharedPreparation;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getOwnerId() {
        return getAuthor();
    }

    @Override
    public String toString() {
        return "UserPreparation{" + "owner=" + owner + ", sharedPreparation=" + sharedPreparation + ", sharedByMe=" + sharedByMe
                + ", roles=" + roles + ", ownerId='" + ownerId + '\'' + "} " + super.toString();
    }
}
