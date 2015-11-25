package org.talend.dataprep.api.service.command.common;

import static org.talend.dataprep.api.service.command.common.Defaults.passthrough;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.json.JsonErrorCode;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

@Component
@Scope("request")
public class GenericCommand<T> extends HystrixCommand<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericCommand.class);

    protected final HttpClient client;

    private final Map<HttpStatus, BiFunction<HttpRequestBase, HttpResponse, T>> behavior = new EnumMap<>(HttpStatus.class);

    @Autowired
    protected Jackson2ObjectMapperBuilder builder;

    @Autowired
    protected ApplicationContext context;

    @Value("${transformation.service.url}")
    protected String transformationServiceUrl;

    @Value("${dataset.service.url}")
    protected String datasetServiceUrl;

    @Value("${api.service.url}")
    protected String apiServiceUrl;

    @Value("${preparation.service.url}")
    protected String preparationServiceUrl;

    private Supplier<HttpRequestBase> httpCall;

    /** Headers of the response received by the command. Set in the run command. */
    private Header[] commandResponseHeaders = new Header[0];

    private Function<Exception, RuntimeException> onError = passthrough();

    protected GenericCommand(HystrixCommandGroupKey group, HttpClient client) {
        super(group);
        this.client = client;
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
        final HttpResponse response = client.execute(request);
        commandResponseHeaders = response.getAllHeaders();

        final HttpStatus status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
        // handle response's HTTP status
        if (status.is4xxClientError() || status.is5xxServerError()) {
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
        return (req, res) -> {
            final int statusCode = res.getStatusLine().getStatusCode();
            try {
                JsonErrorCode code = builder.build().reader(JsonErrorCode.class).readValue(res.getEntity().getContent());
                code.setHttpStatus(statusCode);
                final TDPException cause = new TDPException(code);
                throw onError.apply(cause);
            } catch (JsonMappingException e) {
                LOGGER.debug("Cannot parse response content as JSON.", e);
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
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            } finally {
                req.releaseConnection();
            }
        };
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
}
