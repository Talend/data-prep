package org.talend.dataprep.api.service;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import io.swagger.annotations.Api;

@Api(value = "api", basePath = "/api", description = "Data Preparation API")
public class APIService {

    public static final HystrixCommandGroupKey PREPARATION_GROUP = HystrixCommandGroupKey.Factory.asKey("preparation"); //$NON-NLS-1$

    public static final HystrixCommandGroupKey TRANSFORM_GROUP = HystrixCommandGroupKey.Factory.asKey("transform"); //$NON-NLS-1$

    public static final HystrixCommandGroupKey DATASET_GROUP = HystrixCommandGroupKey.Factory.asKey("dataset"); //$NON-NLS-1$

    private final RequestConfig requestConfig = RequestConfig.custom().setRedirectsEnabled(false).build();

    protected static final Logger LOG = LoggerFactory.getLogger(APIService.class);

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CloseableHttpClient httpClient;

    @Autowired
    private PoolingHttpClientConnectionManager connectionManager;

    protected <T extends HystrixCommand> T getCommand(Class<T> clazz, Object... args) {
        try {
            return context.getBean(clazz, args);
        } catch (BeansException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_FIND_COMMAND, e, ExceptionContext.build().put("class", clazz)
                    .put("args", args));
        }
    }

    protected HttpClient getClient() {
        return httpClient;
    }

    /**
     * @return the connection pool stats.
     */
    protected PoolStats getConnectionStats() {
        return connectionManager.getTotalStats();
    }
}
