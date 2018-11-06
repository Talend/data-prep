package org.talend.dataprep.qa.step.export;

import static org.talend.dataprep.qa.config.FeatureContext.suffixName;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.DATASET_ID;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.EXPORT_TYPE;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.FILENAME;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.FILTER;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.PREPARATION_ID;
import static org.talend.dataprep.qa.util.export.MandatoryParameters.DATASET_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.util.export.ExportParam;
import org.talend.dataprep.qa.util.export.ExportUtil;
import org.talend.dataprep.qa.util.export.MandatoryParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.restassured.response.Response;

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
    public void exportSample(@NotNull Map<String, String> params) throws IOException {
        // File exported
        String filename = params.get(FILENAME.getName());

        Map<String, String> exportParams = extractParameters(params);

        Response response = api.executeExport(exportParams);
        response.then().statusCode(200);

        final InputStream csv = response.asInputStream();

        // store the body content in a temporary File
        File tempFile = api.storeInputStreamAsTempFile(filename, csv);
        context.storeTempFile(filename, tempFile);
        LOGGER.debug("Sample exported to {}", tempFile.getPath());
    }

    @Override
    public Map<String, String> extractParameters(@NotNull Map<String, String> params) throws JsonProcessingException {
        Map<String, String> ret = new HashMap<>();

        // Preparation
        String prepFullName = params.get(MandatoryParameters.PREPARATION_NAME.getName());
        String prepPath = util.extractPathFromFullName(prepFullName);
        String suffixedPrepName = suffixName(util.extractNameFromFullName(prepFullName));
        String preparationId = context.getPreparationId(suffixedPrepName, prepPath);

        // Dataset Id should be provided only if preparationId is not set
        String datasetName = params.get(DATASET_NAME.getName());
        if (datasetName != null) {
            String suffixedDatasetName = suffixName(datasetName);
            String datasetId = context.getDatasetId(suffixedDatasetName);
            exportUtil.feedExportParam(ret, DATASET_ID, datasetId);
        }

        // File exported
        String filename = params.get(FILENAME.getName());
        exportUtil.feedExportParam(ret, FILENAME, filename);
        exportUtil.feedExportParam(ret, PREPARATION_ID, preparationId);
        exportUtil.feedExportParam(ret, EXPORT_TYPE, getExportTypeName());
        exportUtil.feedExportParam(ret, FILTER, params);

        for (ExportParam exportParam : getExtraExportParameter()) {
            exportUtil.feedExportParam(ret, exportParam, params);
        }

        // manage filter : for example for fetchmore
        String tqlFilter = params.get(FILTER.getName());
        if (tqlFilter != null) {
            ret.put("filter", tqlFilter);
            ret.put("from", "FILTER");
        }

        return ret;
    }

    public abstract String getExportTypeName();

    public abstract List<ExportParam> getExtraExportParameter();
}
