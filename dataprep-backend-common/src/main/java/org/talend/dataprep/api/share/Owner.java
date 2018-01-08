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

import java.io.Serializable;
import java.util.Objects;

/**
 * Owner of a folder.
 */
public class Owner implements Serializable {

    /** For the Serializable interface. */
    private static final long serialVersionUID = 1L;

    /** The owner user id. */
    private String id;

    /** The owner first name. */
    private String firstName;

    /** The owner last name. */
    private String lastName;

    /** Display name for UI */
    private String displayName;

    /**
     * Default empty constructor.
     */
    public Owner() {
        // default empty constructor for json de/serialization
    }

    /**
     * Full constructor.
     *
     * @param id the owner id.
     * @param firstName the owner first name.
     * @param lastName the owner last name.
     */
    public Owner(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * @return the Id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the FirstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the LastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the display name for the front.
     */
    public String getDisplayName() {
        String displayName = "";
        if (firstName != null) {
            displayName += firstName;
        }
        if (lastName != null) {
            displayName += ' ' + lastName;
        }
        return displayName.trim();
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "Owner{" + "id='" + id + '\'' + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + '}';
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Owner owner = (Owner) o;
        return Objects.equals(id, owner.id) && Objects.equals(firstName, owner.firstName)
                && Objects.equals(lastName, owner.lastName);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName);
    }
}
