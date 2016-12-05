package org.talend.dataprep.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.api.filter.SimpleFilterService;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.actions.date.DateParser;
import org.talend.dataprep.transformation.api.action.ActionParser;
import org.talend.dataprep.transformation.api.action.validation.ActionMetadataValidation;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@Import(ActionsImport.class)
public class Actions {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ApplicationContext context;

    public Actions() {
        Providers.setProvider(new SpringProvider());
    }

    @Bean
    public DateParser dateParser(AnalyzerService analyzerService) {
        return new DateParser(analyzerService);
    }

    @Bean
    public ActionMetadataValidation actionMetadataValidation() {
        return new ActionMetadataValidation();
    }

    @Bean
    public FilterService filterService() {
        return new SimpleFilterService();
    }

    @Bean
    public ActionFactory actionFactory() {
        return new ActionFactory();
    }

    @Bean
    public ActionParser actionParser(ActionFactory actionFactory, ActionRegistry actionRegistry) {
        return new ActionParser(actionFactory, actionRegistry, objectMapper);
    }

    private class SpringProvider implements Providers.Provider {

        @Override
        public <T> T get(Class<T> clazz, Object... args) {
            return context.getBean(clazz, args);
        }

        @Override
        public void clear() {
        }
    }
}
