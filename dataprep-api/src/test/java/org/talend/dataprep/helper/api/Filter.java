package org.talend.dataprep.helper.api;

import java.util.EnumMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Filter {

    public EnumMap<ActionFilterEnum, Object> range = new EnumMap<>(ActionFilterEnum.class);

}
