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

package org.talend.dataprep.command;

import static org.junit.Assert.*;
import static org.talend.dataprep.command.Defaults.toJson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultsTest {

    @Test
    public void shoudPassthrough() throws Exception {
        // Given
        final RuntimeException runtimeException = new RuntimeException();
        final Exception exception = new Exception();

        // When
        try {
            Defaults.passthrough().apply(runtimeException);
        } catch (Exception e) {
            // Then
            assertEquals(runtimeException, e);
        }

        // When
        try {
            Defaults.passthrough().apply(exception);
        } catch (TDPException e) {
            // Then
            assertEquals(CommonErrorCodes.UNEXPECTED_EXCEPTION, e.getCode());
        }
    }

    @Test
    public void shouldReturnNull() throws Exception {
        // When
        final Object value = Defaults.asNull().apply(buildRequest(), buildResponse());

        // Then
        assertNull(value);
    }

    @Test
    public void shouldReturnString() throws Exception {
        // When
        final BasicHttpResponse response = buildResponse();
        response.setEntity(new StringEntity("My response"));
        final String value = Defaults.asString().apply(buildRequest(), response);

        // Then
        assertEquals("My response", value);
    }

    @Test
    public void shouldReturnString_handleIOError() throws Exception {
        // When
        final BasicHttpResponse response = buildResponse();
        response.setEntity(new StringEntity("") {
            @Override
            public InputStream getContent() throws IOException {
                throw new IOException("Unexpected exception");
            }
        });

        try {
            Defaults.asString().apply(buildRequest(), response);
        } catch (TDPException e) {
            // Then
            assertEquals(CommonErrorCodes.UNEXPECTED_EXCEPTION, e.getCode());
        }
    }

    @Test
    public void shouldReturnEmpty() throws Exception {
        // When
        final String value = Defaults.emptyString().apply(buildRequest(), buildResponse());

        // Then
        assertEquals("", value);
    }

    @Test
    public void shouldReturnEmptyInputStream() throws Exception {
        // When
        final InputStream value = Defaults.emptyStream().apply(buildRequest(), buildResponse());

        // Then
        assertEquals("", IOUtils.toString(value, Charset.defaultCharset()));
    }

    @Test
    public void shouldPipeInputStream() throws Exception {
        // When
        final BasicHttpResponse response = buildResponse();
        response.setEntity(new StringEntity(""));
        final InputStream value = Defaults.pipeStream().apply(buildRequest(), response);

        // Then
        assertEquals("", IOUtils.toString(value, Charset.defaultCharset()));
    }

    @Test
    public void shouldPipeInputStream_handleIOError() throws Exception {
        // When
        final BasicHttpResponse response = buildResponse();
        response.setEntity(new StringEntity("") {
            @Override
            public InputStream getContent() throws IOException {
                throw new IOException("Unexpected exception");
            }
        });

        try {
            Defaults.pipeStream().apply(buildRequest(), response);
        } catch (TDPException e) {
            // Then
            assertEquals(CommonErrorCodes.UNEXPECTED_EXCEPTION, e.getCode());
        }
    }

    @Test
    public void shouldReadJsonResponse() throws Exception {
        // Given
        final Response response = new Response();
        response.value = "My Value";
        final ObjectMapper mapper = new ObjectMapper();
        final BasicHttpResponse httpResponse = buildResponse();
        httpResponse.setEntity(new StringEntity(mapper.writeValueAsString(response)));

        // When
        final Response value = Defaults.convertResponse(mapper, Response.class).apply(buildRequest(), httpResponse);

        // Then
        assertEquals("My Value", value.value);
    }

    @Test
    public void shouldReadEmptyJsonResponse() throws Exception {
        // Given
        final ObjectMapper mapper = new ObjectMapper();
        final BasicHttpResponse httpResponse = buildResponse();
        httpResponse.setEntity(new StringEntity(""));

        // When
        final Response value = Defaults.convertResponse(mapper, Response.class).apply(buildRequest(), httpResponse);

        // Then
        assertNull(value);
    }

    @Test
    public void shouldReadJsonResponse_ioException() throws Exception {
        // Given
        final ObjectMapper mapper = new ObjectMapper();
        final BasicHttpResponse httpResponse = buildResponse();
        httpResponse.setEntity(new StringEntity("") {
            @Override
            public InputStream getContent() throws IOException {
                throw new IOException();
            }
        });

        // When
        try {
            Defaults.convertResponse(mapper, Response.class).apply(buildRequest(), httpResponse);
        } catch (TDPException e) {
            // Then
            assertEquals(CommonErrorCodes.UNEXPECTED_EXCEPTION, e.getCode());
        }
    }

    @Test
    public void shouldParseJsonArray() throws Exception {
        // Given
        final BiFunction<HttpRequestBase, HttpResponse, Set<String>> extractStatuses = toJson(new ObjectMapper()).andThen(jsonNode -> {
            final Iterator<JsonNode> elements = jsonNode.elements();
            Set<String> statuses = new HashSet<>();
            while (elements.hasNext()) {
                final JsonNode element = elements.next();
                statuses.add(element.get("status").asText());
            }
            return statuses;
        });
        final BasicHttpResponse response = buildResponse();
        response.setEntity(new StringEntity("[{\"status\":\"OK\"}, {\"status\":\"OK\"}, {\"status\":\"CANCEL\"}]"));
        final HttpGet request = buildRequest();

        // When
        final Set<String> statuses = extractStatuses.apply(request, response);

        // Then
        assertEquals(2, statuses.size());
        assertTrue(statuses.contains("OK"));
        assertTrue(statuses.contains("CANCEL"));
    }

    private HttpGet buildRequest() {
        return new HttpGet("http://fake_url");
    }

    private BasicHttpResponse buildResponse() {
        return new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "");
    }

    private static class Response {

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
