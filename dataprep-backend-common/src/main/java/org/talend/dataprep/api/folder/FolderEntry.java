package org.talend.dataprep.api.folder;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.talend.dataprep.api.preparation.Identifiable;

import java.io.Serializable;

public class FolderEntry extends Identifiable implements Serializable {

    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("contentType")
    /**
     * this is the type of the content dataset or preparation or something else..
     */
    private String contentType;

    /* id of the content i.e datasetId or preparationId or something else */
    @JsonProperty("contentId")
    private String contentId;


    @JsonProperty("path")
    private String path;

    public FolderEntry()
    {
        // no op only to help Jackson
    }

    public FolderEntry(String contentType, String contentId, String path) {
        this.contentType = contentType;
        this.contentId = contentId;
        this.path = path;
        this.buildId();
    }

    public void buildId(){
        if (this.id == null){
            this.id = contentType + '@' + contentId;
        }
    }


    @Override
    public String id() {
        return this.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType( String contentType )
    {
        this.contentType = contentType;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    @Override
    public String toString()
    {
        return "FolderEntry{" +
            "contentId='" + contentId + '\'' +
            ", id='" + id + '\'' +
            ", contentType='" + contentType + '\'' +
            ", path='" + path + '\'' +
            '}';
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        FolderEntry that = (FolderEntry) o;

        if ( id != null ? !id.equals( that.id ) : that.id != null )
        {
            return false;
        }
        return !( path != null ? !path.equals( that.path ) : that.path != null );

    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + ( path != null ? path.hashCode() : 0 );
        return result;
    }
}
