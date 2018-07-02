package org.talend.dataprep.configuration;

import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// TODO : not sure if it should be only for catalog mode: frontend should not use /api/v1/datasets anyway in legacy mode
@ConditionalOnProperty(name = "dataset.service.provider", havingValue = "catalog")
public class DatasetProxyConfiguration {

    @Value("${dataset.service.url}")
    private String datasetServiceUrl;

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        ProxyServlet servlet = new ProxyServlet();
        ServletRegistrationBean servletRegistrationBean =
                new ServletRegistrationBean(servlet, "/api/v1/datasets/*");
        servletRegistrationBean.addInitParameter("targetUri", datasetServiceUrl + "/api/v1/datasets");
        servletRegistrationBean.addInitParameter(ProxyServlet.P_LOG, "true");
        return servletRegistrationBean;
    }
}
