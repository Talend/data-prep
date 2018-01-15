// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.daikon.exception.json.JsonErrorCode;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.exception.ErrorCodeDto;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.TdpExceptionDto;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.security.Security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Base Hystrix command request for all DataPrep commands.
 *
 * @param <T> Command result type.
 */
public class GenericCommand<T> extends HystrixCommand<T> {

    /** Hystrix group used for dataset related commands. */
    public static final HystrixCommandGroupKey DATASET_GROUP = HystrixCommandGroupKey.Factory.asKey("dataset");

    /** Hystrix group used for preparation related commands. */
    public static final HystrixCommandGroupKey PREPARATION_GROUP = HystrixCommandGroupKey.Factory.asKey("preparation");

    /** Hystrix group used for transformation related commands. */
    public static final HystrixCommandGroupKey TRANSFORM_GROUP = HystrixCommandGroupKey.Factory.asKey("transform");

    /** Hystrix group used for transformation related commands. */
    public static final HystrixCommandGroupKey FULLRUN_GROUP = HystrixCommandGroupKey.Factory.asKey("fullrun");

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericCommand.class);

    /** Behaviours map. */
    private final Map<HttpStatus, BiFunction<HttpRequestBase, HttpResponse, T>> behavior = new EnumMap<>(HttpStatus.class);

    /** The http client. */
    @Autowired
    protected HttpClient client;

    /** Jackson object mapper to handle json. */
    @Autowired
    protected ObjectMapper objectMapper;

    /** Spring application context. */
    @Autowired
    protected ApplicationContext context;

    /** Transformation service URL. */
    @Value("${transformation.service.url:}")
    protected String transformationServiceUrl;

    /** Full run service URL. */
    @Value("${fullrun.service.url:}")
    protected String fullRunServiceUrl;

    /** Dataset service URL. */
    @Value("${dataset.service.url:}")
    protected String datasetServiceUrl;

    /** Preparation service URL. */
    @Value("${preparation.service.url:}")
    protected String preparationServiceUrl;

    @Autowired
    private BeanConversionService conversionService;

    private final Map<String, String> headers = new HashMap<>();

    private Supplier<HttpRequestBase> httpCall;

    /** Headers of the response received by the command. Set in the run command. */
    private Header[] commandResponseHeaders = new Header[0];

    /** Default onError behaviour. */
    private Function<Exception, RuntimeException> onError = Defaults.passthrough();

    private HttpStatus status;

    private static final HttpStatus[] INFO_STATUS = Stream.of(HttpStatus.values()) //
            .filter(HttpStatus::is1xxInformational) //
            .collect(Collectors.toList()) //
            .toArray(new HttpStatus[0]);

    private static final HttpStatus[] SUCCESS_STATUS = Stream.of(HttpStatus.values()) //
            .filter(HttpStatus::is2xxSuccessful) //
            .collect(Collectors.toList()) //
            .toArray(new HttpStatus[0]);

    private static final HttpStatus[] REDIRECT_STATUS = Stream.of(HttpStatus.values()) //
            .filter(HttpStatus::is3xxRedirection) //
            .collect(Collectors.toList()) //
            .toArray(new HttpStatus[0]);

    private static final HttpStatus[] USER_ERROR_STATUS = Stream.of(HttpStatus.values()) //
            .filter(HttpStatus::is4xxClientError) //
            .collect(Collectors.toList()) //
            .toArray(new HttpStatus[0]);

    private static final HttpStatus[] SERVER_ERROR_STATUS = Stream.of(HttpStatus.values()) //
            .filter(HttpStatus::is5xxServerError) //
            .collect(Collectors.toList()) //
            .toArray(new HttpStatus[0]);

    /**
     * Protected constructor.
     *
     * @param group the command group.
     */
    protected GenericCommand(final HystrixCommandGroupKey group) {
        super(group);
    }

    protected GenericCommand(final HystrixCommandGroupKey group, final Map<String, String> headers) {
        this(group);
        this.headers.putAll(headers);

    }

    /** Override this method to change security token source. Executed in post construct with all fields initialized. */
    public String getAuthenticationToken() {
        return context.getBean(Security.class).getAuthenticationToken();
    }

    @Override
    protected RuntimeException decomposeException(Exception e) {
        Throwable current = e;
        while (current.getCause() != null) {
            if (current instanceof TDPException) {
                break;
            }
            current = current.getCause();
        }
        if (current instanceof TDPException) {
            return (TDPException) current;
        } else {
            return super.decomposeException(e);
        }
    }

    /**
     * Runs a data prep command with the following steps:
     * <ul>
     * <li>Gets the HTTP command to execute (see {@link #execute(Supplier)}.</li>
     * <li>Gets the behavior to adopt based on returned HTTP code (see {@link #on(HttpStatus...)}).</li>
     * <li>If no behavior was defined for returned code, returns an error as defined in {@link #onError(Function)}</li>
     * <li>If a behavior was defined, invokes defined behavior.</li>
     * </ul>
     *
     * @return A instance of <code>T</code>.
     * @throws Exception If command execution fails.
     */
    @Override
    protected T run() throws Exception {

        final HttpRequestBase request = httpCall.get();

        // insert all the provided headers in the request
        if (headers.size() > 0) {
            headers.forEach(request::addHeader);
        }

        // update request header with security token
        if (StringUtils.isNotBlank(getAuthenticationToken())) {
            request.addHeader(AUTHORIZATION, getAuthenticationToken());
        } else {
            // Intentionally left as debug to prevent log flood in open source edition.
            LOGGER.debug("No current authentication token for {}.", this.getClass());
        }

        final HttpResponse response;
        try {
            LOGGER.trace("Requesting {} {}", request.getMethod(), request.getURI());
            response = client.execute(request);
        } catch (Exception e) {
            throw onError.apply(e);
        }
        commandResponseHeaders = response.getAllHeaders();

        status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());

        Header cookies = response.getFirstHeader("Set-Cookie");
        if (cookies != null) {
            LOGGER.warn("request {} {}: Cookie detected in responseHeaders (check security.oauth2.resource.uri settings)",
                    request.getMethod(), request.getURI());
        }

        // do we have a behavior for this status code (even an error) ?
        // if yes use it
        BiFunction<HttpRequestBase, HttpResponse, T> function = behavior.get(status);
        if (function != null) {
            try {
                return function.apply(request, response);
            } catch (Exception e) {
                throw onError.apply(e);
            }
        }

        // handle response's HTTP status
        if (status.is4xxClientError() || status.is5xxServerError()) {
            LOGGER.debug("request {} {} : response on error {}", request.getMethod(), request.getURI(), response.getStatusLine());
            // Http status >= 400 so apply onError behavior
            return callOnError(onError).apply(request, response);
        } else {
            // Http status is not error so apply onError behavior
            return behavior.getOrDefault(status, missingBehavior()).apply(request, response);
        }
    }

    /**
     * @return the CommandResponseHeader
     */
    public Header[] getCommandResponseHeaders() {
        return commandResponseHeaders;
    }

    /**
     * @return The HTTP status of the executed request.
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * @return A {@link BiFunction} to handle missing behavior definition for HTTP response's code.
     */
    private BiFunction<HttpRequestBase, HttpResponse, T> missingBehavior() {
        return (req, res) -> {
            LOGGER.error("Unable to process message for request {} (response code: {}).", req,
                    res.getStatusLine().getStatusCode());
            req.releaseConnection();
            return Defaults.<T> asNull().apply(req, res);
        };
    }

    /**
     * @param onError The {@link Supplier} to handle error cases (to throw custom exceptions).
     * @return A {@link BiFunction} that throws a {@link TDPException exception} for proper HTTP response.
     * @see Defaults#passthrough()
     */
    private BiFunction<HttpRequestBase, HttpResponse, T> callOnError(Function<Exception, RuntimeException> onError) {
        return new ErrorHandler(onError);
    }

    /**
     * Declares what exception should be thrown in case of error.
     *
     * @param onError A {@link Function function} that returns a {@link RuntimeException}.
     * @see TDPException
     */
    protected void onError(Function<Exception, RuntimeException> onError) {
        this.onError = onError;
    }

    /**
     * Declares which {@link HttpRequestBase http request} to execute in command.
     *
     * @param call The {@link Supplier} to provide the {@link HttpRequestBase} to execute.
     */
    protected void execute(Supplier<HttpRequestBase> call) {
        httpCall = call;
    }

    /**
     * Starts declaration of behavior(s) to adopt when HTTP response has status code <code>status</code>.
     *
     * @param status One of more HTTP {@link HttpStatus status(es)}.
     * @return A {@link BehaviorBuilder builder} to continue behavior declaration for the HTTP status(es).
     * @see BehaviorBuilder#then(BiFunction)
     */
    protected BehaviorBuilder on(HttpStatus... status) {
        return new BehaviorBuilder(status);
    }

    /**
     * Starts declaration of behavior(s) to adopt when HTTP response has status code of 1xx.
     *
     * @return A {@link BehaviorBuilder builder} to continue behavior declaration for the HTTP status(es).
     * @see BehaviorBuilder#then(BiFunction)
     */
    protected BehaviorBuilder onInfo() {
        return on(INFO_STATUS);
    }

    /**
     * Starts declaration of behavior(s) to adopt when HTTP response has status code of 2xx.
     *
     * @return A {@link BehaviorBuilder builder} to continue behavior declaration for the HTTP status(es).
     * @see BehaviorBuilder#then(BiFunction)
     */
    protected BehaviorBuilder onSuccess() {
        return on(SUCCESS_STATUS);
    }

    /**
     * Starts declaration of behavior(s) to adopt when HTTP response has status code of 3xx.
     *
     * @return A {@link BehaviorBuilder builder} to continue behavior declaration for the HTTP status(es).
     * @see BehaviorBuilder#then(BiFunction)
     */
    protected BehaviorBuilder onRedirect() {
        return on(REDIRECT_STATUS);
    }

    /**
     * Starts declaration of behavior(s) to adopt when HTTP response has status code of 4xx.
     *
     * @return A {@link BehaviorBuilder builder} to continue behavior declaration for the HTTP status(es).
     * @see BehaviorBuilder#then(BiFunction)
     */
    protected BehaviorBuilder onUserErrors() {
        return on(USER_ERROR_STATUS);
    }

    /**
     * Starts declaration of behavior(s) to adopt when HTTP response has status code of 5xx.
     *
     * @return A {@link BehaviorBuilder builder} to continue behavior declaration for the HTTP status(es).
     * @see BehaviorBuilder#then(BiFunction)
     */
    protected BehaviorBuilder onServerErrors() {
        return on(SERVER_ERROR_STATUS);
    }

    /**
     * Serialize the actions to string.
     *
     * @param stepActions - map of couple (stepId, action)
     * @return the serialized actions
     */
    protected String serializeActions(final Collection<Action> stepActions) throws JsonProcessingException {
        return "{\"actions\": " + objectMapper.writeValueAsString(stepActions) + "}";
    }

    // A intermediate builder for behavior definition.
    protected class BehaviorBuilder {

        private final HttpStatus[] status;

        public BehaviorBuilder(HttpStatus[] status) {
            this.status = status;
        }

        /**
         * Declares what action should be performed for the given HTTP status(es).
         *
         * @param action A {@link BiFunction function} to be executed for given HTTP status(es).
         * @see Defaults
         */
        public void then(BiFunction<HttpRequestBase, HttpResponse, T> action) {
            for (HttpStatus currentStatus : status) {
                GenericCommand.this.behavior.put(currentStatus, action);
            }
        }
    }

    private class ErrorHandler implements BiFunction<HttpRequestBase, HttpResponse, T> {

        private final Function<Exception, RuntimeException> onError;

        private ErrorHandler(Function<Exception, RuntimeException> onError) {
            this.onError = onError;
        }

        @Override
        public T apply(HttpRequestBase req, HttpResponse res) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("request on error {} -> {}", req.toString(), res.getStatusLine());
            }
            final int statusCode = res.getStatusLine().getStatusCode();
            String content = StringUtils.EMPTY;
            try {
                if (res.getEntity() != null) {
                    content = IOUtils.toString(res.getEntity().getContent(), UTF_8);
                }

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Error received {}", content);
                }
                TdpExceptionDto exceptionDto = objectMapper.readValue(content, TdpExceptionDto.class);
                TDPException cause = conversionService.convert(exceptionDto, TDPException.class);
                ErrorCode code = cause.getCode();
                if (code instanceof ErrorCodeDto) {
                    ((ErrorCodeDto) code).setHttpStatus(statusCode);
                }
                throw onError.apply(cause);
            } catch (JsonProcessingException e) {
                LOGGER.debug("Cannot parse response content as JSON with content '" + content + "'", e);
                // Failed to parse JSON error, returns an unexpected code with returned HTTP code
                final TDPException exception = new TDPException(new JsonErrorCode() {

                    @Override
                    public String getProduct() {
                        return CommonErrorCodes.UNEXPECTED_EXCEPTION.getProduct();
                    }

                    @Override
                    public String getCode() {
                        return CommonErrorCodes.UNEXPECTED_EXCEPTION.getCode();
                    }

                    @Override
                    public int getHttpStatus() {
                        return statusCode;
                    }
                });
                throw onError.apply(exception);
            } catch (IOException e) {
                LOGGER.error("Unexpected error message: {}", buildRequestReport(req, res));
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            } finally {
                req.releaseConnection();
            }
        }

        public String buildRequestReport(HttpRequestBase req, HttpResponse res) {
            StringBuilder builder = new StringBuilder("{request:{\n");
            builder.append("uri:").append(req.getURI()).append(",\n");
            builder.append("request:").append(req.getRequestLine()).append(",\n");
            builder.append("method:").append(req.getMethod()).append(",\n");
            if (req instanceof HttpEntityEnclosingRequestBase) {
                try {
                    builder.append("load:")
                            .append(IOUtils.toString(((HttpEntityEnclosingRequestBase) req).getEntity().getContent(),
                                    UTF_8))
                            .append(",\n");
                } catch (IOException e) {
                    // We ignore the field
                }
            }
            builder.append("}, response:{\n");
            try {
                builder.append(IOUtils.toString(res.getEntity().getContent(), UTF_8));
            } catch (IOException e) {
                // We ignore the field
            }
            builder.append("}\n}");
            return builder.toString();
        }
    }
}
