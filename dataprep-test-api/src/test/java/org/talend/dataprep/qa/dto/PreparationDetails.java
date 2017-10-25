package org.talend.dataprep.qa.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Preparation details.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreparationDetails {

    public List<Action> actions;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Action {

        public String action;

        public Parameters parameters;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public class Parameters {

            public String column_id;

            public String column_name;
        }
    }
}
