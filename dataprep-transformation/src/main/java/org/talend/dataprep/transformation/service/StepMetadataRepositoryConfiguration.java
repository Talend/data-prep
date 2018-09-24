package org.talend.dataprep.transformation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.dataprep.processor.Wrapper;
import org.talend.dataprep.security.SecurityProxy;

/**
 * A configuration to wrap existing {@link StepMetadataRepository} using a {@link ReactiveStepMetadataRepository}.
 */
@Configuration
public class StepMetadataRepositoryConfiguration {

    @Autowired
    private SecurityProxy proxy;

    @Bean
    public Wrapper<StepMetadataRepository> stepMetadataRepositoryWrapper() {
        return new Wrapper<StepMetadataRepository>() {

            @Override
            public Class<StepMetadataRepository> wrapped() {
                return StepMetadataRepository.class;
            }

            @Override
            public StepMetadataRepository doWith(StepMetadataRepository instance, String beanName,
                    ApplicationContext applicationContext) {
                return new ReactiveStepMetadataRepository(instance, proxy);
            }
        };
    }
}
