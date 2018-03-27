package org.talend.dataprep.qa.util;

import com.google.common.base.Objects;

/**
 * Unique representation of a preparation.
 */
public class PreparationUID {

    protected String name;

    protected String path;

    public String getName() {
        return name;
    }

    public PreparationUID setName(String name) {
        this.name = name;
        return this;
    }

    public String getPath() {
        return path;
    }

    public PreparationUID setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PreparationUID))
            return false;
        PreparationUID that = (PreparationUID) o;
        return Objects.equal(getName(), that.getName()) && Objects.equal(getPath(), that.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), getPath());
    }
}
