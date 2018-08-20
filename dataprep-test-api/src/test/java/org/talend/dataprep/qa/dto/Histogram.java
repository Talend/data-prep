package org.talend.dataprep.qa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Dataset metadatas statistics Histogram.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Histogram {

    public String type;

    public List<Object> items = new ArrayList<>();
}
