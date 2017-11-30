package org.talend.dataprep.qa.step.export;

import java.io.IOException;
import java.util.Map;

import javax.validation.constraints.NotNull;

public interface ExportStep {

    /**
     * Realize a preparation sample export.
     * 
     * @param params the step parameters0
     */
    void export(@NotNull Map<String, String> params) throws IOException;
}
