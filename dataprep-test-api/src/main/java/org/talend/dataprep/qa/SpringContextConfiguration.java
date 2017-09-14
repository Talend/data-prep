package org.talend.dataprep.qa;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@Configurable
@ComponentScan(basePackages = {"org.talend.dataprep.qa", "org.talend.dataprep.helper"})
@PropertySource("classpath:application.properties")
public class SpringContextConfiguration {

}
