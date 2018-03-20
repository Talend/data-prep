// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriUtils;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.http.HttpResponseContext;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class ExportUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportUtils.class);

    private ExportUtils() {
    }

    public static void setExportHeaders(String exportName, String encoding, ExportFormat format) {
        Charset responseEncoding;
        try {
            responseEncoding = Charset.forName(encoding);
        } catch (Exception e) {
            responseEncoding = UTF_8;
        }

        HttpResponseContext.contentType(format.getMimeType() + ";Charset=" + responseEncoding);
        // TDP-2925 a multi-byte file name cannot export the file correctly
        // Current [RFC 2045] grammar restricts parameter values (and hence Content-Disposition filenames) to US-ASCII
        try {
            HttpResponseContext.header(
                HttpHeaders.CONTENT_DISPOSITION,
                String.format(
                    "attachment; filename*=%s''%s",
                    UTF_8,
                    UriUtils.encodePath(exportName, UTF_8.toString()) + format.getExtension()
                )
            );
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Can't encode '{}' from UTF-8", exportName, e);
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
