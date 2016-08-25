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

import java.util.Collection;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JettyCustom {

    @Bean
    public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory(
            @Value("${server.port:8080}") final String port, @Value("${jetty.threadPool.maxThreads:200}") final String maxThreads,
            @Value("${jetty.threadPool.minThreads:8}") final String minThreads,
            @Value("${jetty.threadPool.idleTimeout:-1}") final String idleTimeout) {

        JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory(Integer.valueOf(port)) {

            // olamy: factory.addServerCustomizers(); cannot be used as getServerCustomizers is called before (IMHO a spring-boot
            // bug...)
            @Override
            public Collection<JettyServerCustomizer> getServerCustomizers() {
                Collection<JettyServerCustomizer> jettyServerCustomizers = super.getServerCustomizers();

                jettyServerCustomizers.add(new JettyServerCustomizer() {

                    @Override
                    public void customize(final Server server) {
                        // Customize the connection pool used by Jetty for very large dataset
                        // which takes more than 30s to be rendered
                        QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
                        threadPool.setMaxThreads(Integer.valueOf(maxThreads));
                        threadPool.setMinThreads(Integer.valueOf(minThreads));
                        threadPool.setIdleTimeout(Integer.valueOf(idleTimeout));

                        for (Connector connector : server.getConnectors()) {
                            if (connector instanceof ServerConnector) {
                                ((ServerConnector) connector).setIdleTimeout(Long.parseLong(idleTimeout));
                            }
                        }

                    }
                });

                return jettyServerCustomizers;
            }

            @Override
            protected void postProcessWebAppContext(WebAppContext webAppContext) {
                super.postProcessWebAppContext(webAppContext);
            }
        };

        return factory;
    }

}
