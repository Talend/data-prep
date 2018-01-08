// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.talend.dataprep.configuration.DataPrepComponentScanConfiguration;

import com.netflix.hystrix.Hystrix;

@SpringBootApplication
@Configuration("org.talend.dataprep.api.Application")
@Profile("standalone")
@Import(DataPrepComponentScanConfiguration.class)
public class Application implements DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void destroy() throws Exception {
        LOG.info("Shutting down Hystrix...");
        // shutdown all thread pools; waiting a little time for shutdown
        Hystrix.reset(1, TimeUnit.SECONDS);
        LOG.info("Shut down Hystrix done.");
    }
}
