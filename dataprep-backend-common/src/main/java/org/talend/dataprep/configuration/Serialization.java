package org.talend.dataprep.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

@Configuration
@SuppressWarnings("InsufficientBranchCoverage")
public class Serialization {

    @Autowired
    List<Module> modules;

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.featuresToDisable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        builder.indentOutput(false);
        modules.add(new Jdk8Module()); // needed to [de]serialize java8 Optional (among other things)
        builder.modules(modules);
        return builder;
    }
}
