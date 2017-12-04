package org.talend.dataprep.qa.step.export;

import java.io.IOException;
import java.util.Map;

import javax.validation.constraints.NotNull;

/**
 * All sample export step should implements this interface.
 */
public interface ExportSampleStep {

    /**
     * Realize a preparation sample export.
     * 
     * @param params the step parameters
     * @throws IOException if needed
     */
    void exportSample(@NotNull Map<String, String> params) throws IOException;

    /**
     * Extract all parameters needed for a sample export from a Cucumber DataTable.
     * 
     * @param params the step parameters.
     * @return a {@link Map} of sample export specific parameters.
     */
    @NotNull
    Map<String, Object> extractParameters(@NotNull Map<String, String> params);
}
