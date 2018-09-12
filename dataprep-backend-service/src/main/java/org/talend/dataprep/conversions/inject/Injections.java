package org.talend.dataprep.conversions.inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Configuration
public class Injections {

    @Bean
    @Scope(SCOPE_SINGLETON)
    public DataSetNameInjection dataSetNameInjection() {
        return new DataSetNameInjection();
    }

    @Bean
    @Scope(SCOPE_SINGLETON)
    public DatasetInjection datasetInjection() {
        return new DatasetInjection();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public OwnerInjection ownerInjection() {
        return new DefaultOwnerInjection();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public SharedInjection sharedInjection() {
        return new DefaultSharedInjection();
    }

}
