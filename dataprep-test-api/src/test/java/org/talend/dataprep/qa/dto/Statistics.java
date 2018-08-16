package org.talend.dataprep.qa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Dataset metadatas statistics.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Statistics {

    public Integer count;

    public Integer valid;

    public Integer invalid;

    public Integer empty;

    public List<Object> frequencyTable = new ArrayList<>();

    public List<Object> patternFrequencyTable = new ArrayList<>();

    public Histogram histogram;
}
