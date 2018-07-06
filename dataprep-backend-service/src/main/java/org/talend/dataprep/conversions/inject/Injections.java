package org.talend.dataprep.conversions.inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Injections {

    @Bean
    @Scope("singleton")
    public DataSetNameInjection dataSetNameInjection()  {
        return new DataSetNameInjection();
    }

    @Bean
    @Scope("prototype")
    public OwnerInjection ownerInjection() {
        return new OwnerInjection();
    }

}
