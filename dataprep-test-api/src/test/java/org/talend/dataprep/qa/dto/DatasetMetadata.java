package org.talend.dataprep.qa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Dataset metadatas.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetMetadata {

    public List<ColumnMetadata> columns = new ArrayList<>();
}
