package org.talend.dataprep.helper.objects;

/**
 * Created by vferreira on 09/08/17.
 */

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "exportType",
        "datasetId",
        "preparationId",
        "stepId",
        "arguments.csv_fields_delimiter",
        "arguments.fileName"
})
public class FullRunRequest {

    @JsonProperty("exportType")
    private String exportType;
    @JsonProperty("datasetId")
    private String datasetId;
    @JsonProperty("preparationId")
    private String preparationId;
    @JsonProperty("stepId")
    private String stepId;
    @JsonProperty("arguments.csv_fields_delimiter")
    private String argumentsCsvFieldsDelimiter;
    @JsonProperty("arguments.fileName")
    private String argumentsFileName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public FullRunRequest(String exportType, String datasetId, String preparationId, String stepId, String argumentsCsvFieldsDelimiter, String argumentsFileName) {
        this.exportType = exportType;
        this.datasetId = datasetId;
        this.preparationId = preparationId;
        this.stepId = stepId;
        this.argumentsCsvFieldsDelimiter = argumentsCsvFieldsDelimiter;
        this.argumentsFileName = argumentsFileName;
    }

    @JsonProperty("exportType")
    public String getExportType() {
        return exportType;
    }

    @JsonProperty("exportType")
    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    @JsonProperty("datasetId")
    public String getDatasetId() {
        return datasetId;
    }

    @JsonProperty("datasetId")
    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    @JsonProperty("preparationId")
    public String getPreparationId() {
        return preparationId;
    }

    @JsonProperty("preparationId")
    public void setPreparationId(String preparationId) {
        this.preparationId = preparationId;
    }

    @JsonProperty("stepId")
    public String getStepId() {
        return stepId;
    }

    @JsonProperty("stepId")
    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    @JsonProperty("arguments.csv_fields_delimiter")
    public String getArgumentsCsvFieldsDelimiter() {
        return argumentsCsvFieldsDelimiter;
    }

    @JsonProperty("arguments.csv_fields_delimiter")
    public void setArgumentsCsvFieldsDelimiter(String argumentsCsvFieldsDelimiter) {
        this.argumentsCsvFieldsDelimiter = argumentsCsvFieldsDelimiter;
    }

    @JsonProperty("arguments.fileName")
    public String getArgumentsFileName() {
        return argumentsFileName;
    }

    @JsonProperty("arguments.fileName")
    public void setArgumentsFileName(String argumentsFileName) {
        this.argumentsFileName = argumentsFileName;
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
