package org.talend.dataprep.helper.objects;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vferreira on 01/08/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "actions"
})
public class ActionRequest {

    @JsonProperty("actions")
    private List<Action> actions = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public ActionRequest(List actions) {
        this.actions = actions;
    }

    @JsonProperty("actions")
    public List<Action> getActions() {
        return actions;
    }

    @JsonProperty("actions")
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
