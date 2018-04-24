package org.talend.dataprep.qa.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetContent {

    public List<Object> records = new ArrayList<>();
    public ContentMetadata metadata;

}
