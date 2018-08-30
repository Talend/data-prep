package org.talend.dataprep.conversions.inject;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Injections {

    @Bean
    @Scope(SCOPE_SINGLETON)
    public DataSetNameInjection dataSetNameInjection() {
        return new DataSetNameInjection();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public OwnerInjection ownerInjection() {
        return new DefaultOwnerInjection();
    }

}
