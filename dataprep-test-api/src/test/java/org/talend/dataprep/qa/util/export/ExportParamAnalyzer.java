package org.talend.dataprep.qa.util.export;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.qa.step.export.ExportSampleStep;
import org.talend.dataprep.qa.step.export.ExportSampleStepCSV;

@Component
public class ExportParamAnalyzer {

    @Autowired
    private ExportSampleStepCSV exportSampleStepCSV;

    @Nullable
    public ExportType detectExportType(@NotNull Map<String, String> params) {
        String exportType = params.get(MandatoryParameters.EXPORT_TYPE.getName());
        return ExportType.getExportType(exportType);
    }

    @NotNull
    public EnumMap<MandatoryParameters, String> extractMandatoryParameters(@NotNull Map<String, String> params) {
        EnumMap<MandatoryParameters, String> ret = new EnumMap<>(MandatoryParameters.class);
        Arrays.asList(MandatoryParameters.values()).forEach(mp -> {
            String pValue = params.get(mp.getName());
            if (pValue != null) {
                ret.put(mp, pValue);
            }
        });
        return ret;
    }

    @Nullable
    public ExportSampleStep getExporter(@NotNull ExportType exportType) {
        ExportSampleStep ret = null;
        switch (exportType) {
        case CSV:
            ret = exportSampleStepCSV;
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
