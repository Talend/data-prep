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

package org.talend.dataprep.dataset.adapter.configuration;

import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.adapter.DataprepDatasetClient;
import org.talend.dataprep.dataset.adapter.DatasetClient;
import org.talend.dataprep.dataset.adapter.ProxyDatasetClient;
import org.talend.dataprep.dataset.service.DataSetService;
import org.talend.dataprep.security.Security;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class DatasetClientConfiguration {

    @Bean
    @ConditionalOnProperty(value = "dataset.service.provider", havingValue = "catalog")
    public DatasetClient proxyDatasetClient(RestTemplateBuilder builder, @Value("${catalog.service.url}") URL url, Security security) {
        return new ProxyDatasetClient(builder, url, security);
    }

    @Bean
    @ConditionalOnProperty(value = "dataset.service.provider", havingValue = "legacy", matchIfMissing = true)
    public DatasetClient dataprepDatasetClient(DataSetService dataSetService, BeanConversionService beanConversionService, ObjectMapper objectMapper) {
        return new DataprepDatasetClient(dataSetService, beanConversionService, objectMapper);
    }

}
