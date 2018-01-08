package org.talend.dataprep.qa.step.export;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;
import org.talend.dataprep.qa.util.export.ExportParam;
import org.talend.dataprep.qa.util.export.ExportType;

/**
 * CSV Exporter.
 */
@Component
public class ExportSampleStepXLSX extends AbstractExportSampleStep {

    @Override
    public String getExportTypeName() {
        return ExportType.XLSX.getName();
    }

    @Override
    public List<ExportParam> getExtraExportParameter() {
        return Collections.emptyList();
    }
}
