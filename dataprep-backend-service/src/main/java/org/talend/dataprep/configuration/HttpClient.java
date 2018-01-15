// ============================================================================
//
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

package org.talend.dataprep.configuration;

import java.util.concurrent.TimeUnit;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Http client bean name.
 */
@Configuration
@SuppressWarnings("InsufficientBranchCoverage")
public class HttpClient {

    /** This class' logger. */

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

    /** Maximum connection pool size. */
    @Value("${http.pool.size:50}")
    private int maxPoolSize;

    /**
     * Maximum connection allowed per route. see
     * https://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e393
     */
    @Value("${http.pool.maxPerRoute:50}")
    private int maxPerRoute;

    /** Default keep alive time for http connections if the optional keep alive is not set in the http headers. */
    @Value("${http.defaultKeepAlive:60}") // default is 60 seconds
    private int defaultKeepAlive;

    /** Timeout in seconds used when requesting a connection from the connection manager. */
    @Value("${http.pool.connectionRequestTimeout:10}") // default is 10 seconds
    private int connectionRequestTimeout;

    /** Optional SSL socket factory. */
    @Autowired(required = false)
    private LayeredConnectionSocketFactory sslSocketFactory;

    /**
     * @return the http connection manager.
     */
    @Bean(destroyMethod = "shutdown")
    public PoolingHttpClientConnectionManager getConnectionManager() {

        // fallback to default implementation
        if (sslSocketFactory == null) {
            sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        }

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(RegistryBuilder
                .<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory).build());

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
                .setKeepAliveStrategy(getKeepAliveStrategy()) //
                .setDefaultRequestConfig(getRequestConfig()) //
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)) //
                .setRedirectStrategy(new RedirectTransferStrategy()) //
                .build();
    }

    /**
     * @return the http request configuration to use.
     */
    private RequestConfig getRequestConfig() {
        return RequestConfig.custom() //
                .setContentCompressionEnabled(true)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();
    }

    /**
     * @return The connection keep alive strategy.
     */
    private ConnectionKeepAliveStrategy getKeepAliveStrategy() {

        return (response, context) -> {
            // Honor 'keep-alive' header
            HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && "timeout".equalsIgnoreCase(param)) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch(NumberFormatException ignore) {
                        // let's move on the next header value
                        break;
                    }
                }
            }
            // otherwise use the default value
            return defaultKeepAlive * 1000;
        };
    }

    @Component
    static class IdleConnectionJanitor {

        @Autowired
        PoolingHttpClientConnectionManager connectionManager;

        // periodically checks for idle connections
        @Scheduled(fixedDelay = 30000)
        public void idleConnectionJanitor() {
            connectionManager.closeExpiredConnections();
            connectionManager.closeIdleConnections(60, TimeUnit.SECONDS);
            LOGGER.debug("connection pool status {}", connectionManager.getTotalStats());
        }
    }

    /**
     * Default redirection strategy that does not follow redirection.
     */
    static class RedirectTransferStrategy extends DefaultRedirectStrategy {

        /**
         * @see DefaultRedirectStrategy#isRedirected(HttpRequest, HttpResponse, HttpContext)
         */
        @Override
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
            return false;
        }

    }

}
