package org.talend.dataprep.configuration;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Http client bean name.
 */
@Configuration
@SuppressWarnings("InsufficientBranchCoverage")
public class HttpClient {

    /** Maximum connection pool size. */
    @Value("${http.pool.size:50}")
    private int maxPoolSize;

    /**
     * Maximum connection allowed per route. see
     * https://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e393
     */
    @Value("${http.pool.maxPerRoute:50}")
    private int maxPerRoute;

    /**
     * @return the http connection manager.
     */
    @Bean(destroyMethod = "shutdown")
    public PoolingHttpClientConnectionManager getConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxPoolSize);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        return connectionManager;
    }


    /**
     * @param connectionManager the connection manager.
     * @return the http client to use backed by a pooling connection manager.
     */
    @Bean(destroyMethod = "close")
    public CloseableHttpClient getHttpClient(PoolingHttpClientConnectionManager connectionManager) {
        return HttpClientBuilder.create() //
                .setConnectionManager(connectionManager) //
                .setRedirectStrategy(new RedirectTransferStrategy()) //
                .build();
    }

    /**
     * Default redirection strategy that does not follow redirection.
     */
    static class RedirectTransferStrategy extends DefaultRedirectStrategy {

        @Override
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
            return false;
        }

    }

}
