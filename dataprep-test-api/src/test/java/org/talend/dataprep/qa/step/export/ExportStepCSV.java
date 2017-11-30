package org.talend.dataprep.qa.step.export;

import static org.talend.dataprep.qa.util.export.ExportParamCSV.CSV_ENCLOSURE_CHARACTER;
import static org.talend.dataprep.qa.util.export.ExportParamCSV.CSV_ENCLOSURE_MODE;
import static org.talend.dataprep.qa.util.export.ExportParamCSV.CSV_ENCODING;
import static org.talend.dataprep.qa.util.export.ExportParamCSV.CSV_ESCAPE_CHARACTER;
import static org.talend.dataprep.qa.util.export.ExportParamCSV.CSV_FIELDS_DELIMITER;
import static org.talend.dataprep.qa.util.export.ExportParamCSV.DATASET_ID;
import static org.talend.dataprep.qa.util.export.ExportParamCSV.EXPORT_TYPE;
import static org.talend.dataprep.qa.util.export.ExportParamCSV.FILENAME;
import static org.talend.dataprep.qa.util.export.ExportParamCSV.PREPARATION_ID;
import static org.talend.dataprep.qa.util.export.ExportParamCSV.STEP_ID;
import static org.talend.dataprep.qa.util.export.MandatoryParameters.DATASET_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.util.StepParamType;
import org.talend.dataprep.qa.util.export.ExportParam;
import org.talend.dataprep.qa.util.export.ExportType;
import org.talend.dataprep.qa.util.export.MandatoryParameters;

/**
 * 
 */
@Component
public class ExportStepCSV extends DataPrepStep implements ExportStep {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportStepCSV.class);

    @Override
    public void export(Map<String, String> params) throws IOException {

        // Preparation
        String preparationName = params.get(MandatoryParameters.PREPARATION_NAME.getName());
        String preparationId = context.getPreparationId(preparationName);

        // Dataset
        String datasetName = params.get(DATASET_NAME.getName());
        String datasetId = context.getDatasetId(datasetName);

        // File exported
        String filename = params.get(FILENAME.getName());

        LOGGER.debug("Preparation sample export with preparationName={}, datasetName={}, filename={}.", preparationName,
                datasetName, filename);

        // TODO manage export from step ? (or from version)
        List<String> steps = api.getPreparation(preparationId).then().statusCode(200).extract().body().jsonPath()
                .getJsonObject("steps");

        Map<String, Object> exportParams = new HashMap<>();
        feedExportParam(exportParams, PREPARATION_ID, preparationId);
        feedExportParam(exportParams, STEP_ID, steps.get(steps.size() - 1));
        feedExportParam(exportParams, DATASET_ID, datasetId);
        feedExportParam(exportParams, EXPORT_TYPE, ExportType.CSV.name());
        feedExportParam(exportParams, FILENAME, filename);
        feedExportParam(exportParams, CSV_ESCAPE_CHARACTER, params);
        feedExportParam(exportParams, CSV_FIELDS_DELIMITER, params);
        feedExportParam(exportParams, CSV_ENCLOSURE_CHARACTER, params);
        feedExportParam(exportParams, CSV_ENCLOSURE_MODE, params);
        feedExportParam(exportParams, CSV_ENCODING, params);

        final InputStream csv = api.executeExport(exportParams).asInputStream();

        // store the body content in a temporary File
        File tempFile = api.storeInputStreamAsTempFile(filename, csv);
        context.storeTempFile(filename, tempFile);
    }

    private void feedExportParam(Map<String, Object> exportParams, ExportParam param, String value) {
        if (!param.getType().equals(StepParamType.IN)) {
            if (value != null) {
                exportParams.put(param.getJsonName(), value);
            }
        }
    }

    private void feedExportParam(Map<String, Object> exportParams, ExportParam param, Map<String, String> dataTable) {
        if (!param.getType().equals(StepParamType.IN)) {
            String value = dataTable.get(param.getName());
            if (value != null) {
                exportParams.put(param.getJsonName(), dataTable.get(param.getName()));
            }
        }
    }
}
