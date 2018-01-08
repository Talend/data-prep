package org.talend.dataprep.qa.step.export;

import org.springframework.stereotype.Component;
import org.talend.dataprep.qa.util.export.ExportParam;
import org.talend.dataprep.qa.util.export.ExportType;

import java.util.List;

import static java.util.Arrays.asList;
import static org.talend.dataprep.qa.util.export.ExportSampleParamCSV.*;

/**
 * CSV Exporter.
 */
@Component
public class ExportSampleStepCSV extends AbstractExportSampleStep {

    @Override
    public String getExportTypeName() {
        return ExportType.CSV.name();
    }

    @Override
    public List<ExportParam> getExtraExportParameter() {
        return asList(CSV_ESCAPE_CHARACTER, //
                CSV_FIELDS_DELIMITER, //
                CSV_ENCLOSURE_CHARACTER, //
                CSV_ENCLOSURE_MODE, //
                CSV_ENCODING);
    }
}
