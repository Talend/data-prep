package org.talend.dataprep.qa.dto;

import java.util.EnumMap;
import java.util.List;

import org.talend.dataprep.helper.api.ActionParamEnum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Preparation details.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreparationDetails {

    public List<String> steps;

    // TODO : check if this class isn't a duplication of Action in dataprep-api/helper/api/Action.
    public List<Action> actions;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Action {

        public String action;

        // not to be loaded by jackson but to be inferred from steps attribute
        public String id;

        public EnumMap<ActionParamEnum, String> parameters;
    }

}
