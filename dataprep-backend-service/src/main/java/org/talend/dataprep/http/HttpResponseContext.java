//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.http;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * A helper class around {@link RequestContextHolder}: allow simple modifications on HTTP context without worrying
 * whether code is called in a web context or not.
 */
public class HttpResponseContext {

    /**
     * Default empty constructor.
     */
    private HttpResponseContext() {
        // private constructor for this utility class
    }

    /**
     * Set the http status to set to the request (if available).
     *
     * @param status the http status to set.
     */
    public static void status(HttpStatus status) {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes instanceof ServletRequestAttributes) {
            ((ServletRequestAttributes) attributes).getResponse().setStatus(status.value());
        }
    }

    /**
     * Set the header to the request (if available).
     *
     * @param header The header name.
     * @param value The header value.
     */
    public static void header(String header, String value) {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes instanceof ServletRequestAttributes) {
            ((ServletRequestAttributes) attributes).getResponse().setHeader(header, value);
        }
    }

    /**
     * @param contentType the content type to set.
     */
    public static void contentType(String contentType) {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes instanceof ServletRequestAttributes) {
            ((ServletRequestAttributes) attributes).getResponse().setContentType(contentType);
        }
    }

    public static void setAttachmentHeaders(String fileName) {
        HttpHeaders headers = getAttachmentHeaders(fileName);

        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes instanceof ServletRequestAttributes) {
            ((ServletRequestAttributes) attributes).getResponse().addHeader("Content-Type", headers.getContentType().toString());
            ((ServletRequestAttributes) attributes).getResponse().addHeader("Content-Disposition", headers.getFirst("Content-Disposition"));
        }
    }

    public static HttpHeaders getAttachmentHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachement", fileName, UTF_8);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return headers;
    }
}
