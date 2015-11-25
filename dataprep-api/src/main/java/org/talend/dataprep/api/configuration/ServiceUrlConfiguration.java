package org.talend.dataprep.api.configuration;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.stereotype.Component;

/**
 * Set the api port out of the api.service.url properties.
 */
@Component
public class ServiceUrlConfiguration implements EmbeddedServletContainerCustomizer {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ServiceUrlConfiguration.class);

    /** Default port value. */
    private final static int DEFAULT_PORT = 8888;

    @Value("${api.service.url}")
    private String apiServiceUrl;

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        int port;
        try {
            URI apiUri = new URI(apiServiceUrl);
            port = apiUri.getPort();
            LOG.debug("setting api port : {}", port);
        } catch (URISyntaxException e) {
            LOG.warn("error parsing api.service.url property ({}) default port value used : {}", apiServiceUrl, DEFAULT_PORT, e);
            port = DEFAULT_PORT;
        }
        container.setPort(port);
    }
}
