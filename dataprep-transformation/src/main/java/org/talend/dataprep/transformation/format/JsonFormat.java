package org.talend.dataprep.transformation.format;

import java.util.Collections;

import org.springframework.stereotype.Component;
import org.talend.dataprep.format.export.ExportFormat;

/**
 * Json format type.
 */
@Component("format#" + JsonFormat.JSON)
public class JsonFormat extends ExportFormat {

    /** Json format type name. */
    public static final String JSON = "JSON";

    /**
     * Default constructor.
     */
    public JsonFormat() {
        super(JSON, "application/json", ".json", false, false, Collections.<Parameter> emptyList());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
