package org.talend.dataprep.transformation.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriUtils;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.http.HttpResponseContext;

public class ExportUtils {

    private ExportUtils() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportUtils.class);

    public static void setExportHeaders(String exportName, ExportFormat format) {
        HttpResponseContext.contentType(format.getMimeType());
        // TDP-2925 a multi-byte file name cannot export the file correctly
        // Current [RFC 2045] grammar restricts parameter values (and hence Content-Disposition filenames) to US-ASCII
        try {
            HttpResponseContext.header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + UriUtils.encodePath(exportName, UTF_8.toString()) + format.getExtension() + "\"");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Can't encode '{}' from UTF-8", exportName);
            throw new RuntimeException(e);
        }
    }
}
