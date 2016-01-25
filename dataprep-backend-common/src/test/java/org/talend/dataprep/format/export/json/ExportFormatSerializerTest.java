package org.talend.dataprep.format.export.json;

import static org.junit.Assert.assertThat;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.format.export.ExportFormat;

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
    protected Jackson2ObjectMapperBuilder builder;

    @Test
    public void csv() throws IOException {
        StringWriter writer = new StringWriter();

        //@formatter:off
        ExportFormat format = new ExportFormat("TOTO", "text/toto", ".toto", true, false,
                Collections.singletonList(
                        new ExportFormat.Parameter("totoSeparator", "CHOOSE_SEPARATOR", "radio",
                        new ExportFormat.ParameterValue("|", "SEPARATOR_PIPE"),
                        Arrays.asList(new ExportFormat.ParameterValue("\u0009", "SEPARATOR_TAB"), // &#09;
                                new ExportFormat.ParameterValue(":", "SEPARATOR_COLUMN"),
                                new ExportFormat.ParameterValue(".", "SEPARATOR_DOT"))))) {
                                    @Override
                                    public int getOrder() {
                                        return 0;
                                    }
                                };
        //@formatter:on
        builder.build().writer().writeValue(writer, format);
        assertThat(writer.toString(), sameJSONAsFile(ExportFormatSerializerTest.class.getResourceAsStream("toto.json")));
    }

}
