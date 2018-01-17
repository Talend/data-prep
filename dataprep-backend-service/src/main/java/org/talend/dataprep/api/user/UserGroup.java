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

package org.talend.dataprep.api.user;

import java.util.Objects;

/**
 * Group whose the user belong to.
 */
public class UserGroup {

    /** The group label. */
    private String label;
    /** The group technical id. */
    private String id;

    /**
     * Default empty constructor.
     */
    public UserGroup() {
        // needed for json de/serialization
    }

    /**
     * Constructor.
     * @param id the group id.
     * @param label the group label.
     */
    public UserGroup(String id, String label) {
        this.id = id;
        this.label = label;
    }

    /**
     * @return the Label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set.
     */
    public void setLabel(String label) {
        this.label = label;
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
        UserGroup userGroup = (UserGroup) o;
        return Objects.equals(label, userGroup.label) &&
                Objects.equals(id, userGroup.id);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(label, id);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "UserGroup{" +
                "label='" + label + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
