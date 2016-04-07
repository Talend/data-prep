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

package org.talend.dataprep.format.export.json;

import static org.junit.Assert.assertThat;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.parameters.SelectParameter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for json serialization of ExportFormat.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ExportFormatSerializerTest.class)
@Configuration
@ComponentScan(basePackages = "org.talend.dataprep")
@EnableAutoConfiguration
public class ExportFormatSerializerTest {

    /** Dataprep ready json builder. */
    @Autowired
    protected ObjectMapper mapper;

    @Test
    public void csv() throws IOException {
        StringWriter writer = new StringWriter();

        //@formatter:off
        ExportFormat format = new ExportFormat("TOTO", "text/toto", ".toto", true, false,
                Collections.singletonList(SelectParameter.Builder.builder().name("totoSeparator")
                        .item("|", "SEPARATOR_PIPE")
                        .item("\u0009", "SEPARATOR_TAB")
                        .item(":", "SEPARATOR_COLUMN")
                        .item(".", "SEPARATOR_DOT")
                        .defaultValue("|")
                        .build())) {
            @Override
            public int getOrder() {
                return 0;
            }
        };
        //@formatter:on
        mapper.writeValue(writer, format);
        assertThat(writer.toString(), sameJSONAsFile(ExportFormatSerializerTest.class.getResourceAsStream("toto.json")));
    }

}
