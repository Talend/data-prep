//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.parameters;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.dataprep.test.MockTestApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for all parameter tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MockTestApplication.class)
@ComponentScan(basePackages = "org.talend.dataprep")
public abstract class ParameterBaseTest {

    @Autowired
    protected ObjectMapper mapper;

}
