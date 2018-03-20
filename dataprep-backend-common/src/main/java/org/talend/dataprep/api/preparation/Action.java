// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.preparation;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Class used to wrap DataSetRowAction into json.
 */
@JsonRootName("action")
@JsonPropertyOrder(value = { "action", "parameters" })
public class Action implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** Json description of the action. */
    private String action;

    /** Parameters needed for the action. */
    private MixedContentMap parameters = new MixedContentMap();

    /**
     * Default empty constructor.
     */
    public Action() {
    }

    /**
     * @return the json description of the action.
     */
    @JsonProperty("action")
    public String getName() {
        return action;
    }

    /**
     * @param action the json description of the action to set.
     */
    @JsonProperty("action")
    public void setName(String action) {
        this.action = action;
    }

    /**
     * @return the action parameters.
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the action parameters to set.
     */
    public void setParameters(MixedContentMap parameters) {
        this.parameters = parameters;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Action action1 = (Action) o;
        return Objects.equals(action, action1.action) && Objects.equals(parameters, action1.parameters);
    }

    @Override
    public String toString() {
        return "Action{" + "action='" + action + '\'' + ", parameters=" + parameters + '}';
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(action, parameters);
    }

    /**
     * Builder used to ease the Action creation.
     */
    public static class Builder {

        private Map<String, String> parameters;

        private String name;

        /**
         * @return the Builder to use.
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * @return the built row action.
         */
        public Action build() {
            final Action builtAction = new Action();
            builtAction.getParameters().putAll(parameters);
            builtAction.setName(name);
            return builtAction;
        }

        public Builder withParameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

    }

}
