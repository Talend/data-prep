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

package org.talend.dataprep.dataset.service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.api.share.SharedResource;

public class UserDataSetMetadata extends DataSetMetadata implements SharedResource {

    /**
     * flag to tell the dataset is one of the favorites for the current user this value is sent back to front but not
     * stored because it stored in another user related storage
     */
    private boolean favorite;

    /** True if this dataset metadata is shared by another user. */
    private boolean sharedDataSet = false;

    /** True if this dataset metadata is shared by current user. */
    private boolean sharedByMe = false;

    /** This dataset metadata owner. */
    private Owner owner;

    /** What role has the current user on this folder. */
    private Set<String> roles = new HashSet<>();

    /**
     * Getter for favorite.
     *
     * @return the favorite
     */
    public boolean isFavorite() {
        return this.favorite;
    }

    /**
     * Sets the favorite.
     *
     * @param favorite the favorite to set
     */
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    /**
     * @return the SharedDataSet
     */
    public boolean isSharedDataSet() {
        return sharedDataSet;
    }

    /**
     * @param sharedDataSet the sharedDataSet to set.
     */
    public void setSharedDataSet(boolean sharedDataSet) {
        this.sharedDataSet = sharedDataSet;
    }

    /**
     * @return sharedByMe
     */
    public boolean isSharedByMe() {
        return sharedByMe;
    }

    /**
     * @see SharedResource#setSharedByMe(boolean)
     */
    @Override
    public void setSharedByMe(boolean sharedByMe) {
        this.sharedByMe = sharedByMe;
    }

    /**
     * @return the Owner
     */
    public Owner getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set.
     */
    @Override
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    /**
     * @return the Roles
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set.
     */
    @Override
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /**
     * Set the shared resource flag.
     *
     * @param shared the shared resource flag value.
     */
    @Override
    public void setSharedResource(boolean shared) {
        this.setSharedDataSet(shared);
    }

    /**
     * @return this shared resource owner/author id.
     */
    @Override
    public String getOwnerId() {
        return getAuthor();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        UserDataSetMetadata that = (UserDataSetMetadata) o;
        return favorite == that.favorite && //
                sharedDataSet == that.sharedDataSet && //
                sharedByMe == that.sharedByMe && //
                Objects.equals(owner, that.owner) && //
                Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), favorite, sharedDataSet, sharedByMe, owner, roles);
    }
}
