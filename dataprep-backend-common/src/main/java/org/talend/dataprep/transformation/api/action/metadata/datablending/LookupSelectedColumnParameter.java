package org.talend.dataprep.transformation.api.action.metadata.datablending;

/**
 * Parameter used by the lookup action used to select the columns to add.
 */
public class LookupSelectedColumnParameter {

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
