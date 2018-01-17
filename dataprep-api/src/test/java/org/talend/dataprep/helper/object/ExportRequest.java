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

package org.talend.dataprep.helper.object;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represent the parameters of a full run export request.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExportRequest {

    public static final String EXPORT_PARAM_PREFIX = "exportParameters.";

    private String exportType;

    private String datasetId;

    private String preparationId;

    private String stepId;

    @JsonProperty("arguments.csv_fields_delimiter")
    private String csv_fields_delimiter; // arguments.csv_fields_delimiter

    @JsonProperty("arguments.fileName")
    private String fileName; // arguments.filename

    @JsonProperty("arguments.csv_escape_character")
    private String escapeCharacter; // arguments.csv_fields_delimiter

    @JsonProperty("arguments.csv_enclosure_character")
    private String enclosureCharacter; // arguments.filename

    @JsonProperty("arguments.csv_enclosure_mode")
    private String enclosureMode; // arguments.csv_fields_delimiter

    @JsonProperty("arguments.csv_encoding")
    private String charset; // arguments.csv_fields_delimiter

    public Map<String, Object> returnParameters() {

        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("preparationId", preparationId);
        parametersMap.put("stepId", stepId);
        parametersMap.put("datasetId", datasetId);
        parametersMap.put("exportType", exportType);
        parametersMap.put(EXPORT_PARAM_PREFIX + "csv_fields_delimiter", csv_fields_delimiter);
        parametersMap.put(EXPORT_PARAM_PREFIX + "fileName", fileName);
        parametersMap.put(EXPORT_PARAM_PREFIX + "csv_escape_character", escapeCharacter);
        parametersMap.put(EXPORT_PARAM_PREFIX + "csv_enclosure_character", enclosureCharacter);
        parametersMap.put(EXPORT_PARAM_PREFIX + "csv_enclosure_mode", enclosureMode);
        parametersMap.put(EXPORT_PARAM_PREFIX + "csv_encoding", charset);

        return parametersMap;
    }

    public String getExportType() {
        return exportType;
    }

    public ExportRequest setExportType(String exportType) {
        this.exportType = exportType;
        return this;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public ExportRequest setDatasetId(String datasetId) {
        this.datasetId = datasetId;
        return this;
    }

    public String getPreparationId() {
        return preparationId;
    }

    public ExportRequest setPreparationId(String preparationId) {
        this.preparationId = preparationId;
        return this;
    }

    public String getStepId() {
        return stepId;
    }

    public ExportRequest setStepId(String stepId) {
        this.stepId = stepId;
        return this;
    }

    public String getCsv_fields_delimiter() {
        return csv_fields_delimiter;
    }

    public ExportRequest setCsv_fields_delimiter(String csv_fields_delimiter) {
        this.csv_fields_delimiter = csv_fields_delimiter;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ExportRequest setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getEscapeCharacter() {
        return escapeCharacter;
    }

    public ExportRequest setEscapeCharacter(String escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
        return this;
    }

    public String getEnclosureCharacter() {
        return enclosureCharacter;
    }

    public ExportRequest setEnclosureCharacter(String enclosureCharacter) {
        this.enclosureCharacter = enclosureCharacter;
        return this;
    }

    public String getEnclosureMode() {
        return enclosureMode;
    }

    public ExportRequest setEnclosureMode(String enclosureMode) {
        this.enclosureMode = enclosureMode;
        return this;
    }

    public String getCharset() {
        return charset;
    }

    public ExportRequest setCharset(String charset) {
        this.charset = charset;
        return this;
    }
}
