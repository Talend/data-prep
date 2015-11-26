package org.talend.dataprep.api.folder;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.talend.dataprep.api.preparation.Identifiable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Folder extends Identifiable implements Serializable {

    /**
     * repository path as /foo/bar/beer
     */
    @Id
    @JsonProperty("path")
    private String path;

    @JsonProperty("name")
    private String name;

    @JsonProperty("creationDate")
    private long creationDate;

    @JsonProperty("modificationDate")
    private long modificationDate;

    public Folder() {
        // no op
    }

    public Folder(String path) {
        this.path = path;
    }

    @Override
    public String getId() {
        return this.getPath();
    }

    @Override
    public String id() {
        return this.getPath();
    }

    public void setId(String id) {
        this.setPath(id);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        this.id = path;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public long getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate( long creationDate )
    {
        this.creationDate = creationDate;
    }

    public long getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate( long modificationDate )
    {
        this.modificationDate = modificationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Folder that = (Folder) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString()
    {
        return "Folder{" +
            "name='" + name + '\'' +
            ", id='" + id + '\'' +
            ", path='" + path + '\'' +
            ", creationDate='" + creationDate + '\'' +
            ", modificationDate='" + modificationDate + '\'' +
            '}';
    }

    public static class Builder {

        private String path;

        private String name;

        private long creationDate;

        private long modificationDate;

        public static Builder folder() {
            return new Folder.Builder();
        }

        public Folder.Builder path(String path) {
            this.path = path;
            return this;
        }

        public Folder.Builder name(String name) {
            this.name = name;
            return this;
        }

        public Folder.Builder creationDate(long creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Folder.Builder modificationDate(long modificationDate) {
            this.modificationDate = modificationDate;
            return this;
        }

        public Folder build() {

            Folder folder = new Folder();
            folder.setPath(path);
            folder.setName( name );
            folder.setCreationDate( creationDate );
            folder.setModificationDate( modificationDate );
            return folder;
        }

    }
}
