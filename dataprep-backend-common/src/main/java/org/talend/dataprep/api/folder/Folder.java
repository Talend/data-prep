package org.talend.dataprep.api.folder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    @Override public String toString() {
        return "Folder{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    public static class Builder {

        private String name;

        private String path;

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

        public Folder build() {

            Folder folder = new Folder();
            folder.setPath(path);

            return folder;
        }

    }
}
