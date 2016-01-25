package org.talend.dataprep.configuration;

import static com.google.common.base.Predicates.or;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.google.common.base.Predicate;

/**
 * Enable embedded rest documentation based on swagger.
 *
 * see http://springfox.github.io/springfox/
 */
@Configuration
@ConditionalOnProperty(name = "service.documentation", havingValue = "true", matchIfMissing = true)
@EnableSwagger2
@SuppressWarnings("InsufficientBranchCoverage")
public class Documentation {

    @Value("${service.documentation.name}")
    private String serviceDisplayName;

    @Value("${service.documentation.description}")
    private String serviceDescription;

    @Value("#{'${service.documentation.path}'.split(',')}")
    private String[] servicePaths;

    /**
     * @return The swagger documentation.
     */
    @Bean
    public Docket customImplementation() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select().paths(paths()).build();
    }

    /**
     * @return the swagger API info.
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title(serviceDisplayName).description(serviceDescription).build();
    }

    /**
     * @return where to look for controllers to document them.
     */
    private Predicate<String> paths() {
        return or(Arrays.stream(servicePaths).map(PathSelectors::regex).collect(Collectors.toList()));
    }

}
