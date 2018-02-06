//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@SuppressWarnings("InsufficientBranchCoverage")
public class CORSConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Add CORS header for all path in application
                registry.addMapping("/**") //
                        .allowedOrigins("*") //
                        .allowedMethods("POST", "GET", "OPTIONS", "DELETE", "PUT", "PATCH") //
                        .maxAge(3600) //
                        .allowedHeaders("x-requested-with", "Content-Type", "accept", "Authorization")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true);
            }
        };
    }

}
