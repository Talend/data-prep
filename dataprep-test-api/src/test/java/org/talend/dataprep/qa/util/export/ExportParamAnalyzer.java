package org.talend.dataprep.qa.util.export;

import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.qa.step.export.ExportSampleStep;
import org.talend.dataprep.qa.step.export.ExportSampleStepCSV;
import org.talend.dataprep.qa.step.export.ExportSampleStepXLSX;

@Component
public class ExportParamAnalyzer {

    @Autowired
    private ExportSampleStepCSV exportSampleStepCSV;

    @Autowired
    private ExportSampleStepXLSX exportSampleStepXLSX;

    @Nullable
    public ExportType detectExportType(@NotNull Map<String, String> params) {
        String exportType = params.get(MandatoryParameters.EXPORT_TYPE.getName());
        return ExportType.getExportType(exportType);
    }

    @Nullable
    public ExportSampleStep getExporter(@NotNull ExportType exportType) {
        ExportSampleStep ret = null;
        switch (exportType) {
        case CSV:
            ret = exportSampleStepCSV;
            break;
        case XLSX:
            ret = exportSampleStepXLSX;
            break;
        case HDFS:
            break;
        case EXCEL:
            break;
        case TABLEAU:
            break;
        default:
            break;
        }
        return ret;
    }

}
