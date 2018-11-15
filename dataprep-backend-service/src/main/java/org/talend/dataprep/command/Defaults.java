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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.io.ReleasableInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.talend.daikon.exception.ExceptionContext.build;

/**
 * A helper class for common behavior definition.
 */
public class Defaults {

    private Defaults() {
    }

    /**
     * @return A default that returns the underlying exception as is. In other words, command will rethrow the original
     * exception as its own.
     */
    public static Function<Exception, RuntimeException> passthrough() {
        return e -> {
            if (e instanceof RuntimeException) {
                return (RuntimeException) e;
            } else {
                return new TDPException(CommonErrorCodes.UNEXPECTED_SERVICE_EXCEPTION, e,
                        build().put("message", e.getMessage()));
            }
        };
    }

    /**
     * @param <T> The expected type for the command's return.
     * @return <code>null</code> whatever request or response contains.
     */
    public static <T> BiFunction<HttpRequestBase, HttpResponse, T> asNull() {
        return (request, response) -> {
            EntityUtils.consumeQuietly(response.getEntity());
            request.releaseConnection();
            return null;
        };
    }

    /**
     * @return A 'to string' of the response's body.
     */
    public static BiFunction<HttpRequestBase, HttpResponse, String> asString() {
        return (request, response) -> {
            try (InputStream content = response.getEntity().getContent()) {
                return IOUtils.toString(content, UTF_8);
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            } finally {
                EntityUtils.consumeQuietly(response.getEntity());
                request.releaseConnection();
            }
        };
    }

    /**
     * @return An empty string whatever request or response contains.
     */
    public static BiFunction<HttpRequestBase, HttpResponse, String> emptyString() {
        return (request, response) -> {
            request.releaseConnection();
            EntityUtils.consumeQuietly(response.getEntity());
            return StringUtils.EMPTY;
        };
    }

    /**
     * @return An empty {@link InputStream stream} whatever request or response contains.
     */
    public static BiFunction<HttpRequestBase, HttpResponse, InputStream> emptyStream() {
        return (request, response) -> {
            request.releaseConnection();
            EntityUtils.consumeQuietly(response.getEntity());
            return new ByteArrayInputStream(new byte[0]);
        };
    }

    /**
     * @return A stream to the underlying service's response (and release HTTP connection once returned stream is fully
     * consumed).
     */
    public static BiFunction<HttpRequestBase, HttpResponse, InputStream> pipeStream() {
        return (request, response) -> {
            try {
                return new ReleasableInputStream(response.getEntity().getContent(), request::releaseConnection);
            } catch (IOException e) {
                EntityUtils.consumeQuietly(response.getEntity());
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        };
    }

    /**
     * Read content from HTTP response and convert response to <code>T</code> using provided <code>mapper</code>.
     *
     * @param mapper The mapper to use for reading value.
     * @param clazz The result class.
     * @param <T> The result type
     * @return The response converted as <code>T</code>.
     */
    public static <T> BiFunction<HttpRequestBase, HttpResponse, T> convertResponse(ObjectMapper mapper,
            Class<T> clazz) {
        return (request, response) -> {
            try (final InputStream content = response.getEntity().getContent()) {
                final String contentAsString = IOUtils.toString(content, UTF_8);
                if (StringUtils.isEmpty(contentAsString)) {
                    return null;
                } else {
                    return mapper.readerFor(clazz).readValue(contentAsString);
                }
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            } finally {
                request.releaseConnection();
            }
        };
    }

    /**
     * Read content from HTTP response and convert response to <code>T</code> using provided <code>mapper</code>.
     *
     * @param mapper The mapper to use for reading value.
     * @param typeReference The result class.
     * @param <T> The result type
     * @return The response converted as <code>T</code>.
     */
    public static <T> BiFunction<HttpRequestBase, HttpResponse, T> convertResponse(ObjectMapper mapper,
            TypeReference<T> typeReference) {
        return convertResponse(mapper, typeReference, e -> {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        });
    }

    /**
     * Read content from HTTP response and convert response to <code>T</code> using provided <code>mapper</code>.
     *
     * @param mapper The mapper to use for reading value.
     * @param typeReference The result class.
     * @param errorHandler The error handler to be called on error.
     * @param <T> The result type
     * @return The response converted as <code>T</code>.
     */
    public static <T> BiFunction<HttpRequestBase, HttpResponse, T> convertResponse(ObjectMapper mapper,
            TypeReference<T> typeReference, Function<Exception, T> errorHandler) {
        return (request, response) -> {
            try (InputStream content = response.getEntity().getContent()) {
                return mapper.readerFor(typeReference).readValue(content);
            } catch (Exception e) {
                return errorHandler.apply(e);
            } finally {
                request.releaseConnection();
            }
        };
    }

    /**
     * Read content from HTTP response and convert response to a {@link JsonNode}.
     *
     * @param mapper The mapper to use for creating the JSON tree.
     * @return The response converted as a {@link JsonNode tree}.
     */
    public static BiFunction<HttpRequestBase, HttpResponse, JsonNode> toJson(ObjectMapper mapper) {
        return (request, response) -> {
            try (InputStream content = response.getEntity().getContent()) {
                JsonNode jsonNode = mapper.readTree(content);
                if (jsonNode == null) {
                    throw new IllegalArgumentException("Source should not be empty");
                }
                return jsonNode;
            } catch (Exception e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            } finally {
                request.releaseConnection();
            }
        };
    }

    public static <T, S> BiFunction<HttpRequestBase, HttpResponse, S> iterate(Class<T> clazz, ObjectMapper mapper,
            Function<Iterator<T>, S> convert) {
        return (request, response) -> {
            try (InputStream content = response.getEntity().getContent()) {
                try (MappingIterator<T> objects = mapper.readerFor(clazz).readValues(content)) {
                    return convert.apply(objects);
                }
            } catch (Exception e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            } finally {
                request.releaseConnection();
            }
        };
    }

    public static ResponseEntity<String> getResponseEntity(HttpStatus status, HttpResponse response) {
        final MultiValueMap<String, String> headers = new HttpHeaders();
        for (Header header : response.getAllHeaders()) {
            headers.put(header.getName(), Collections.singletonList(header.getValue()));
        }
        try (InputStream content = response.getEntity().getContent()) {
            return new ResponseEntity<>(IOUtils.toString(content, UTF_8), headers, status);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
