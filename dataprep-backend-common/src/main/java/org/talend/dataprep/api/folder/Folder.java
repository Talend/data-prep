package org.talend.dataprep.api.folder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.talend.dataprep.api.preparation.Identifiable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Folder extends Identifiable implements Serializable {

    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("pathParts")
    private List<String> pathParts;

    @JsonProperty("folderEntries")
    private List<FolderEntry> folderEntries = new ArrayList<>();

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    /**
     * <b>do not use it</b> the backend system will generate an Id
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FolderEntry> getFolderEntries() {
        return folderEntries;
    }

    public void addFolderEntry(FolderEntry folderEntry) {
        this.folderEntries.add(folderEntry);
    }

    public List<String> getPathParts() {
        return pathParts;
    }

    public void setPathParts(List<String> pathParts) {
        this.pathParts = pathParts;
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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Folder{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }

    public static class Builder {

        private String name;

        private List<String> pathParts;

        public static Builder folder() {
            return new Folder.Builder();
        }

        public Folder.Builder pathParts(List<String> pathParts) {
            this.pathParts = pathParts;
            return this;
        }

        public Folder.Builder name(String name) {
            this.name = name;
            return this;
        }

        public Folder build() {

            Folder folder = new Folder();
            folder.setPathParts(pathParts);
            folder.setName(name);

            return folder;
        }

    }
}
