package org.talend.dataprep.qa.step.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.util.export.ExportParam;
import org.talend.dataprep.qa.util.export.ExportUtil;
import org.talend.dataprep.qa.util.export.MandatoryParameters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.talend.dataprep.qa.config.FeatureContext.suffixName;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.*;
import static org.talend.dataprep.qa.util.export.MandatoryParameters.DATASET_NAME;

/**
 * CSV Exporter.
 */
public abstract class AbstractExportSampleStep extends DataPrepStep implements ExportSampleStep {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExportSampleStep.class);

    @Autowired
    private ExportUtil exportUtil;

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
        List<String> steps =
                api.getPreparation(preparationId).then().statusCode(200).extract().body().jsonPath().getJsonObject("steps");

        exportUtil.feedExportParam(ret, PREPARATION_ID, preparationId);
        exportUtil.feedExportParam(ret, STEP_ID, steps.get(steps.size() - 1));
        exportUtil.feedExportParam(ret, DATASET_ID, datasetId);
        exportUtil.feedExportParam(ret, FILENAME, filename);

        exportUtil.feedExportParam(ret, EXPORT_TYPE, getExportTypeName());

        for (ExportParam exportParam : getExtraExportParameter()) {
            exportUtil.feedExportParam(ret, exportParam, params);
        }

        return ret;
    }

    public abstract String getExportTypeName();

    public abstract List<ExportParam> getExtraExportParameter();
}
