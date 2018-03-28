package org.talend.dataprep.qa.util.export;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.talend.dataprep.qa.util.StepParamType;

/**
 * Useful stuff shared within export functionality.
 */
@Component
public class ExportUtil {

    /**
     * Store a parameters in the export {@link Map} depending of its {@link StepParamType} and its value.
     *
     * @param exportParams the export {@link Map} that'll received the parameters.
     * @param param the parameters type.
     * @param value the parameters value to store in the export {@link Map}
     */
    public void feedExportParam(Map<String, Object> exportParams, ExportParam param, Object value) {
        if (!param.getType().equals(StepParamType.IN)) {
            if (value != null) {
                exportParams.put(param.getJsonName(), value);
            }
        }
    }

    /**
     * Extract a parameters from the dataTable and store it in the export {@link Map} depending of its {@link StepParamType}.
     *
     * @param exportParams the export {@link Map} that'll received the parameters.
     * @param param the parameters type.
     * @param dataTable the dataTable expecting to contains the parameter value to store.
     */
    public void feedExportParam(Map<String, Object> exportParams, ExportParam param, Map<String, String> dataTable) {
        if (!param.getType().equals(StepParamType.IN)) {
            String value = dataTable.get(param.getName());
            if (value != null) {
                exportParams.put(param.getJsonName(), dataTable.get(param.getName()));
            }
        }
    }

}
