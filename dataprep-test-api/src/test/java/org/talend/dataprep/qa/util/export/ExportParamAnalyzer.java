package org.talend.dataprep.qa.util.export;

import java.util.Map;

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

    public ExportType detectExportType(Map<String, String> params) {
        String exportType = params.get(MandatoryParameters.EXPORT_TYPE.getName());
        return ExportType.getExportType(exportType);
    }

    public ExportSampleStep getExporter(ExportType exportType) {
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
