/*
 *  ============================================================================
 *
 *  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 *  This source code is available under agreement available at
 *  https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 *  You should have received a copy of the agreement
 *  along with this program; if not, write to Talend SA
 *  9 rue Pages 92150 Suresnes, France
 *
 *  ============================================================================
 */

package org.talend.dataprep.dataset;

import java.net.URL;
import javax.validation.constraints.NotNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "catalog")
@ConditionalOnProperty(name = "dataset.service.provider", havingValue = "catalog")
public class CatalogConfiguration {

    private final Service service = new Service();

    public static class Service {

        @NotNull
        private URL url;

        public String getUrl() {
            return url.toString();
        }

        public void setUrl(URL url) {
            this.url = url;
        }
    }

    public Service getService() {
        return service;
    }
}
