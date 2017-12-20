package org.talend.dataprep.qa.step.export;

import static org.talend.dataprep.qa.config.FeatureContext.suffixName;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.CSV_ENCLOSURE_CHARACTER;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.CSV_ENCLOSURE_MODE;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.CSV_ENCODING;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.CSV_ESCAPE_CHARACTER;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.CSV_FIELDS_DELIMITER;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.DATASET_ID;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.EXPORT_TYPE;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.FILENAME;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.PREPARATION_ID;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.STEP_ID;
import static org.talend.dataprep.qa.util.export.MandatoryParameters.DATASET_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.util.export.ExportType;
import org.talend.dataprep.qa.util.export.ExportUtil;
import org.talend.dataprep.qa.util.export.MandatoryParameters;

/**
 * CSV Exporter.
 */
@Component
public class ExportSampleStepCSV extends DataPrepStep implements ExportSampleStep {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportSampleStepCSV.class);

    @Autowired
    ExportUtil exportUtil;

    @Override
    public void exportSample(Map<String, String> params) throws IOException {

        // File exported
        String filename = params.get(FILENAME.getName());

        Map<String, Object> exportParams = extractParameters(params);

        final InputStream csv = api.executeExport(exportParams).asInputStream();

        // store the body content in a temporary File
        File tempFile = api.storeInputStreamAsTempFile(filename, csv);
        context.storeTempFile(filename, tempFile);
        LOGGER.debug("Sample exported to {}", tempFile.getPath());
    }

    @Override
    public Map<String, Object> extractParameters(Map<String, String> params) {
        Map<String, Object> ret = new HashMap<>();

        // Preparation
        String suffixedPreparationName = suffixName(params.get(MandatoryParameters.PREPARATION_NAME.getName()));
        String preparationId = context.getPreparationId(suffixedPreparationName);

        // Dataset
        String suffixedDatasetName = suffixName(params.get(DATASET_NAME.getName()));
        String datasetId = context.getDatasetId(suffixedDatasetName);

        // File exported
        String filename = params.get(FILENAME.getName());

        // TODO manage export from step ? (or from version)
        List<String> steps = api.getPreparation(preparationId).then().statusCode(200).extract().body().jsonPath()
                .getJsonObject("steps");

        exportUtil.feedExportParam(ret, PREPARATION_ID, preparationId);
        exportUtil.feedExportParam(ret, STEP_ID, steps.get(steps.size() - 1));
        exportUtil.feedExportParam(ret, DATASET_ID, datasetId);
        exportUtil.feedExportParam(ret, EXPORT_TYPE, ExportType.CSV.name());
        exportUtil.feedExportParam(ret, FILENAME, filename);
        exportUtil.feedExportParam(ret, CSV_ESCAPE_CHARACTER, params);
        exportUtil.feedExportParam(ret, CSV_FIELDS_DELIMITER, params);
        exportUtil.feedExportParam(ret, CSV_ENCLOSURE_CHARACTER, params);
        exportUtil.feedExportParam(ret, CSV_ENCLOSURE_MODE, params);
        exportUtil.feedExportParam(ret, CSV_ENCODING, params);
        return ret;
    }
}
