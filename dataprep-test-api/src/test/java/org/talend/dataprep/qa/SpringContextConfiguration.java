// ============================================================================
//
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

package org.talend.dataprep.qa;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.talend.dataprep.qa.config.BackendProperties;

@Configurable
@ComponentScan(basePackages = { "org.talend.dataprep.qa", "org.talend.dataprep.helper" })
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties(BackendProperties.class)
public class SpringContextConfiguration {

}
