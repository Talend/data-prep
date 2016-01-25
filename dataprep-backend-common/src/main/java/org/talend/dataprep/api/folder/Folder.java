package org.talend.dataprep.api.folder;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.SynchronousQueue;

import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.talend.dataprep.api.preparation.Identifiable;

public class Folder implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @AccessType(AccessType.Type.PROPERTY)
    protected String id;

    /**
     * repository path as /foo/bar/beer
     */
    @Id
    @AccessType(AccessType.Type.PROPERTY)
    @JsonProperty("path")
    private String path;

    @JsonProperty("name")
    private String name;

    @JsonProperty("creationDate")
    private long creationDate;

    @JsonProperty("lastModificationDate")
    private long lastModificationDate;

    public Folder() {
        // no op
        this.creationDate = System.currentTimeMillis();
        this.lastModificationDate = this.creationDate;
    }

    public Folder(String path) {
        this.path = path;
    }

    public String getId() {
        return this.getPath();
    }

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

    public long getLastModificationDate()
    {
        return lastModificationDate;
    }

    public void setLastModificationDate( long lastModificationDate )
    {
        this.lastModificationDate = lastModificationDate;
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
            ", lastModificationDate='" + lastModificationDate + '\'' +
            '}';
    }

    public static class Builder {

        private String path;

        private String name;

        private long creationDate = -1;

        private long modificationDate = -1;

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
            long currentDateTime = System.currentTimeMillis();
            folder.setLastModificationDate( modificationDate == -1 ? currentDateTime : modificationDate);
            folder.setCreationDate( creationDate == -1 ? currentDateTime : creationDate );

            return folder;
        }

    }
}
