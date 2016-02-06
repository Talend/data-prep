// ============================================================================
//
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

package org.talend.dataprep.transformation.api.action.metadata;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.transformation.api.action.metadata.common.ReplaceOnValueHelper;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Base class for all related unit tests that deal with metadata
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AbstractMetadataBaseTest.class)
@Configuration
@ComponentScan(basePackages = "org.talend.dataprep")
public abstract class AbstractMetadataBaseTest {

    /** The dataprep ready jackson builder. */
    @Autowired
    public Jackson2ObjectMapperBuilder builder;

    protected String generateJson(String token, String operator) {
        ReplaceOnValueHelper r = new ReplaceOnValueHelper(token, operator);
        try {
            return builder.build().writeValueAsString(r);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
