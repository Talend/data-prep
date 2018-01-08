/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.dataprep.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Specify an internal property source to serve as default. Any property it contains is overriden by internal or
 * external application.properties file. See
 * http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-external-config
 */
@Configuration
@PropertySource("classpath:/org/talend/dataprep/configuration/default.properties")
public class DefaultConfiguration {}
