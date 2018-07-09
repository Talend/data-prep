package org.talend.dataprep.helper.api;

import java.util.EnumMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Filter {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public EnumMap<ActionFilterEnum, Object> range;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public EnumMap<ActionFilterEnum, Object> invalid;

}
