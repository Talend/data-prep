package org.talend.dataprep.qa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetContent {

    public List<Object> records = new ArrayList<>();

}
