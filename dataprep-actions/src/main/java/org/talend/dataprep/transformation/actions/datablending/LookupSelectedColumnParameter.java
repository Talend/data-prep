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

package org.talend.dataprep.transformation.actions.datablending;

/**
 * Parameter used by the lookup action used to select the columns to add.
 */
class LookupSelectedColumnParameter {

    /** The column id to add. */
    private String id;

    /** The column name to add. */
    private String name;

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
     * @return the Name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "LookupSelectedColumnParameter{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }
}
