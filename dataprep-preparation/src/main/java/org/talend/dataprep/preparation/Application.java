// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.preparation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.talend.daikon.documentation.DocumentationController;

@SpringBootApplication
@Configuration("org.talend.dataprep.preparation.Application")
@Profile("standalone")
@ComponentScan( //
        value = { "org.talend.dataprep", "org.talend.daikon" }, //
        excludeFilters = @ComponentScan.Filter(value = DocumentationController.class, type = FilterType.ASSIGNABLE_TYPE) //
)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
