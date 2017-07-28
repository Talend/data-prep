package org.talend.dataprep.transformation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.http.HttpResponseContext;

public class ExportUtils {

    private ExportUtils() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportUtils.class);

    public static void setExportHeaders(String exportName, ExportFormat format) {
        HttpResponseContext.setAttachmentHeaders(exportName + format.getExtension());
        HttpResponseContext.contentType(format.getMimeType());
    }
}
