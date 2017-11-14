// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represent the parameters of a full run export request.
 */
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

    public ExportRequest(String exportType, String datasetId, String preparationId, String stepId, String csv_fields_delimiter,
            String fileName, String escapeCharacter, String enclosureCharacter, String enclosureMode, String charset) {
        this.exportType = exportType;
        this.datasetId = datasetId;
        this.preparationId = preparationId;
        this.stepId = stepId;
        this.csv_fields_delimiter = csv_fields_delimiter;
        this.fileName = fileName;
        this.escapeCharacter = escapeCharacter;
        this.enclosureCharacter = enclosureCharacter;
        this.enclosureMode = enclosureMode;
        this.charset = charset;
    }

    public Map<String, Object> getParameters() {

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

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getPreparationId() {
        return preparationId;
    }

    public void setPreparationId(String preparationId) {
        this.preparationId = preparationId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getCsv_fields_delimiter() {
        return csv_fields_delimiter;
    }

    public void setCsv_fields_delimiter(String csv_fields_delimiter) {
        this.csv_fields_delimiter = csv_fields_delimiter;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getEscapeCharacter() {
        return escapeCharacter;
    }

    public void setEscapeCharacter(String escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    public String getEnclosureCharacter() {
        return enclosureCharacter;
    }

    public void setEnclosureCharacter(String enclosureCharacter) {
        this.enclosureCharacter = enclosureCharacter;
    }

    public String getEnclosureMode() {
        return enclosureMode;
    }

    public void setEnclosureMode(String enclosureMode) {
        this.enclosureMode = enclosureMode;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
