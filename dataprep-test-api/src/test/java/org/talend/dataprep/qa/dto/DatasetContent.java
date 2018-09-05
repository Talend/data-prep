package org.talend.dataprep.qa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
//FIXME: to DELETE ?
public class DatasetContent {

    public List<Object> records = new ArrayList<>();

    public ContentMetadata metadata;

    /**
     * For integration tests only: Indicates if the dataset content and metadata are
     * considered as up-to-date or not.
     */
    public boolean isUpToDate = false;

}
