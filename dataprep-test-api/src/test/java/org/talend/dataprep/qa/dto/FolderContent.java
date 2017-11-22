package org.talend.dataprep.qa.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Java representation of a Data-prep folder content.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FolderContent {

    public List<PreparationDetails> preparations;
}
