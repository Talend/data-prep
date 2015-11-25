package org.talend.dataprep.configuration;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Http client bean name.
 */
@Configuration
public class HttpClient {

    /** Maximum connection pool size. */
    @Value("${http.pool.size:50}")
    private int maxPoolSize;

    /** Default connection timeout (in milliseconds). */
    @Value("${http.pool.timeout:5000}")
    private int timeout;

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
        connectionManager.setValidateAfterInactivity(timeout);
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
                .setDefaultRequestConfig(getRequestConfig()) //
                .setKeepAliveStrategy(getKeepAliveStrategy()) //
                .setRedirectStrategy(new RedirectTransferStrategy()) //
                .build();
    }

    /**
     * @return the http request configuration to use.
     */
    private RequestConfig getRequestConfig() {
        return RequestConfig.custom() //
                .setConnectTimeout(timeout) //
                .setConnectionRequestTimeout(timeout) //
                .setSocketTimeout(timeout) //
                .build();
    }

    /**
     * @return the connection keep alive strategy.
     */
    private ConnectionKeepAliveStrategy getKeepAliveStrategy() {

        // use the http response "Keep-Alive" header if present, else use the specified timeout
        return (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return timeout;
        };

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
