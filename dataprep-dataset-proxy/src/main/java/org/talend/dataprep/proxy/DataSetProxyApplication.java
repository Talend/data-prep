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

package org.talend.dataprep.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.talend.dataprep.proxy.properties.DatasetProperties;

/**
 * Service that provides the same operations as Talend Catalog DataSet.
 * <p>
 *     Implement routes and/or adapt requests to either DataSet (TDP legacy) or TDC DataSet depending on configuration
 * </p>
 */
@SpringBootApplication
@EnableConfigurationProperties(DatasetProperties.class)
public class DataSetProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataSetProxyApplication.class, args);
	}

}
