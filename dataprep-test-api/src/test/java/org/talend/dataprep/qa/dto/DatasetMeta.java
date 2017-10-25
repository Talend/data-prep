package org.talend.dataprep.qa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Dataset metadatas.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetMeta {

    public String name;

    public String records;

}
