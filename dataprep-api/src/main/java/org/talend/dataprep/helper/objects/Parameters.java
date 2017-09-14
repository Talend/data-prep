package org.talend.dataprep.helper.objects;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "scope",
        "column_id",
        "column_name",
        "row_id"
})
public class Parameters {

    @JsonProperty("scope")
    private String scope;
    @JsonProperty("column_id")
    private String columnId;
    @JsonProperty("column_name")
    private String columnName;
    @JsonProperty("row_id")
    private Object rowId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Parameters(String columnId, String columnName, String rowId, String scope) {
        setColumnId(columnId);
        setColumnName(columnName);
        setRowId(rowId);
        setScope(scope);
    }

    @JsonProperty("scope")
    public String getScope() {
        return scope;
    }

    @JsonProperty("scope")
    public void setScope(String scope) {
        this.scope = scope;
    }

    @JsonProperty("column_id")
    public String getColumnId() {
        return columnId;
    }

    @JsonProperty("column_id")
    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    @JsonProperty("column_name")
    public String getColumnName() {
        return columnName;
    }

    @JsonProperty("column_name")
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @JsonProperty("row_id")
    public Object getRowId() {
        return rowId;
    }

    @JsonProperty("row_id")
    public void setRowId(Object rowId) {
        this.rowId = rowId;
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
