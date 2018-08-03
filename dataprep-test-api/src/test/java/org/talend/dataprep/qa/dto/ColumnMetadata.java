package org.talend.dataprep.qa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Dataset metadatas.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ColumnMetadata {

    public Statistics statistics;

}
