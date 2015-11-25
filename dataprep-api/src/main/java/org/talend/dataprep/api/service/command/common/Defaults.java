package org.talend.dataprep.api.service.command.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.talend.dataprep.api.service.command.ReleasableInputStream;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

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
        return e -> (RuntimeException) e;
    }

    /**
     * @param <T> The expected type for the command's return.
     * @return <code>null</code> whatever request or response contains.
     */
    public static <T> BiFunction<HttpRequestBase, HttpResponse, T> asNull() {
        return (request, response) -> null;
    }

    /**
     * @return A 'to string' of the response's body.
     */
    public static BiFunction<HttpRequestBase, HttpResponse, String> asString() {
        return (request, response) -> {
            try {
                return IOUtils.toString(response.getEntity().getContent());
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            } finally {
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
            return StringUtils.EMPTY;
        };
    }

    /**
     * @return An empty {@link InputStream stream} whatever request or response contains.
     */
    public static BiFunction<HttpRequestBase, HttpResponse, InputStream> emptyStream() {
        return (request, response) -> {
            request.releaseConnection();
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
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        };
    }

}
