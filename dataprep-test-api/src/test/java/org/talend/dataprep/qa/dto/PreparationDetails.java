package org.talend.dataprep.qa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Preparation details.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreparationDetails {

    public List<Action> actions;

    public class Action {

        public String action;

        public Parameters parameters;

        public class Parameters {

            public String column_id;

            public String column_name;
        }
    }
}
