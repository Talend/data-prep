package org.talend.dataprep.qa;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.talend.dataprep.helper.DataPrepAPIHelper;

@Configurable
@ComponentScan(basePackages = {"org.talend.dataprep.qa", "org.talend.dataprep.helper"})
public class SpringContextConfiguration {


    public static final String DEFAULT_BACKEND_API_URL = "http://localhost:8888";

    @Bean
    public DataPrepAPIHelper dataPrepAPIHelper() {
        DataPrepAPIHelper dataPrepAPIHelper = new DataPrepAPIHelper();

        //    set properties, etc.
        String backendApiUrl = System.getProperty("backend.api.url");
        if (StringUtils.isEmpty(backendApiUrl)) {
            backendApiUrl = DEFAULT_BACKEND_API_URL;
        }

        dataPrepAPIHelper.setBaseUrl(backendApiUrl);

        return dataPrepAPIHelper;
    }


}
