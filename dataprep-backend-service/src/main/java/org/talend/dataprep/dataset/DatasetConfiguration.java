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

package org.talend.dataprep.dataset;

import static org.slf4j.LoggerFactory.getLogger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dataset")
public class DatasetConfiguration {

    private static final Logger LOGGER = getLogger(DatasetConfiguration.class);

    private final Service service = new Service();

    @PostConstruct
    public void init() {
        LOGGER.info("Dataset configuration is retrieved from properties");
    }

    public static class Service {

        private Provider provider = Provider.LEGACY;

        private String url;

        public Provider getProvider() {
            return provider;
        }

        public void setProvider(Provider provider) {
            this.provider = provider;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public enum Provider {LEGACY, CATALOG}
    }
    public Service getService() {
        return service;
    }

    public String getProvider() {
        return service.getProvider().name().toLowerCase();
    }

    public boolean isLegacy() {
        return getService().getProvider() == Service.Provider.LEGACY;
    }
}
