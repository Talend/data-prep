package org.talend.dataprep.transformation.api.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.talend.dataprep.transformation.format.CSVFormat.CSV;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.TransformationBaseTest;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TransformerFactoryTest extends TransformationBaseTest {

    @Autowired
    Jackson2ObjectMapperBuilder builder;

    @Autowired
    private TransformerFactory factory;

    @Test
    public void getExporter_csv_exporter_should_write_csv_format() throws Exception {
        // given
        Map<String, String> arguments = new HashMap<>();
        arguments.put(ExportFormat.PREFIX + "csvSeparator", ";");
        final OutputStream outputStream = new ByteArrayOutputStream();
        final Configuration configuration = Configuration.builder() //
                .args(arguments) //
                .format(CSV) //
                .output(outputStream) //
                .actions(IOUtils.toString(TransformerFactoryTest.class.getResourceAsStream("upper_case_firstname.json"))) //
                .build();
        final Transformer transformer = factory.get(configuration);
        final String expectedCsv = IOUtils.toString(
                TransformerFactoryTest.class
                .getResourceAsStream("expected_export_preparation_uppercase_firstname.csv"));

        final ObjectMapper mapper = builder.build();
        final InputStream inputStream = TransformerFactoryTest.class.getResourceAsStream("../../format/export_dataset.json");
        try (JsonParser parser = mapper.getFactory().createParser(inputStream)) {
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);

            // when
            transformer.transform(dataSet, configuration);
            // then
            assertThat(outputStream.toString()).isEqualTo(expectedCsv);
        }

    }
}