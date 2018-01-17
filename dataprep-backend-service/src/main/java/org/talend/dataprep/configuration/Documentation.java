// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.configuration;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.Predicate;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

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

    /** Value for regex that match all characters. */
    private static final String MATCH_ALL=".*";

    @Value("${service.documentation.name}")
    private String serviceDisplayName;

    @Value("${service.documentation.description}")
    private String serviceDescription;

    @Value("#{'${service.paths:}'.split(',')}")
    private String[] servicePaths;

    @Controller
    @ConditionalOnBean(Documentation.class)
    public static class SwaggerUIRedirection {

        @RequestMapping("/docs")
        String swaggerUI() {
            return "redirect:/docs/index.html";
        }
    }

    /**
     * @return The swagger documentation.
     */
    @Bean
    public Docket customImplementation() {
        return new Docket(SWAGGER_2).apiInfo(apiInfo()).select().paths(paths()).build();
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
        return or(Arrays.stream(servicePaths).map(this::matchAll).map(PathSelectors::regex).collect(Collectors.toList()));
    }


    /**
     * Make sure the given input will match all strings with this pattern : ".*input.*".
     * @param input the input to transform.
     * @return ".*input.*"
     */
    private String matchAll(String input) {
        String path = input;
        if (!path.startsWith(MATCH_ALL)) {
            path = MATCH_ALL + path;
        }
        if (!path.endsWith(MATCH_ALL)) {
            path = path+MATCH_ALL;
        }
        return path;
    }

}
