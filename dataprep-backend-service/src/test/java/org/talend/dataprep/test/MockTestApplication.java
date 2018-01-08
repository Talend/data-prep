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

package org.talend.dataprep.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Sadly needed application to be able to web integration tests.
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.talend.dataprep")
public class MockTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockTestApplication.class, args);
    }

}
