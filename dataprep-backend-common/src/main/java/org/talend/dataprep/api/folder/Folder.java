package org.talend.dataprep.api.folder;

import java.io.Serializable;
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

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

        private String id;

        private String name;

        public static Builder folder() {
            return new Folder.Builder();
        }

        public Folder.Builder id(String id) {
            this.id = id;
            return this;
        }

        public Folder.Builder name(String name) {
            this.name = name;
            return this;
        }

        public Folder build() {
            if (id == null) {
                throw new IllegalStateException("No id set for Folder.");
            }

            Folder folder = new Folder();
            folder.setId(id);
            folder.setName(name);

            return folder;
        }

    }
}
