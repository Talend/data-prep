package org.talend.dataprep.qa.util.export;

import static org.talend.dataprep.qa.util.StepParamType.IN_OUT;
import static org.talend.dataprep.qa.util.StepParamType.OUT;

import java.util.Arrays;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.talend.dataprep.qa.util.StepParamType;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represent parameters from a CSV export.
 */
public enum ExportSampleParamCSV implements ExportParam {
    PREPARATION_ID(OUT, null, "preparationId"), //
    STEP_ID(OUT, null, "stepId"), //
    DATASET_ID(OUT, null, "datasetId"), //
    EXPORT_TYPE(IN_OUT, "exportType", "exportType"), //
    FILENAME(IN_OUT, "fileName", "exportParameters.fileName"), //
    CSV_FIELDS_DELIMITER(IN_OUT, "csv_fields_delimiter", "exportParameters.csv_fields_delimiter"), //
    CSV_ESCAPE_CHARACTER(IN_OUT, "csv_escape_character", "exportParameters.csv_escape_character"), //
    CSV_ENCLOSURE_CHARACTER(IN_OUT, "csv_enclosure_char", "exportParameters.csv_enclosure_character"), //
    CSV_ENCLOSURE_MODE(IN_OUT, "csv_enclosure_mode", "exportParameters.csv_enclosure_mode"), //
    CSV_ENCODING(IN_OUT, "csv_charset", "exportParameters.csv_encoding");

    private StepParamType type;

    private String name;

    private String jsonName;

    ExportSampleParamCSV(StepParamType pType, String pName, String pJsonName) {
        type = pType;
        name = pName;
        jsonName = pJsonName;
    }

    /**
     * Get a corresponding {@link ExportSampleParamCSV} from a {@link String}.
     *
     * @param pName the {@link ExportSampleParamCSV#name}.
     * @return the corresponding {@link ExportSampleParamCSV} or <code>null</code> if there isn't.
     */
    @Nullable
    public static ExportSampleParamCSV getExportSampleParamCSV(@NotNull String pName) {
        return Arrays.stream(ExportSampleParamCSV.values()) //
                .filter(e -> !e.type.equals(OUT) && e.name.equalsIgnoreCase(pName)) //
                .findFirst() //
                .orElse(null);
    }

    @Override
    public StepParamType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @JsonValue
    @Override
    public String getJsonName() {
        return jsonName;
    }
}
