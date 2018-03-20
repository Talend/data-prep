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

package org.talend.dataprep.transformation.api.transformer;

import static java.nio.charset.StandardCharsets.UTF_8;
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
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.TransformationBaseTest;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;
import org.talend.dataprep.transformation.format.CSVFormat;

import com.fasterxml.jackson.core.JsonParser;

public class TransformerFactoryTest extends TransformationBaseTest {

    @Autowired
    private TransformerFactory factory;

    @Test
    public void getExporter_csv_exporter_should_write_csv_format() throws Exception {
        // given
        Map<String, String> arguments = new HashMap<>();
        arguments.put(ExportFormat.PREFIX + CSVFormat.ParametersCSV.FIELDS_DELIMITER, ";");
        arguments.put(ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCLOSURE_MODE, CSVFormat.ParametersCSV.ENCLOSURE_ALL_FIELDS);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final Configuration configuration = Configuration.builder() //
                .args(arguments) //
                .format(CSV) //
                .output(outputStream) //
                .actions(IOUtils.toString(TransformerFactoryTest.class.getResourceAsStream("upper_case_firstname.json"), UTF_8)) //
                .build();
        final Transformer transformer = factory.get(configuration);
        final String expectedCsv = IOUtils.toString(
                TransformerFactoryTest.class.getResourceAsStream("expected_export_preparation_uppercase_firstname.csv"), UTF_8);

        final InputStream inputStream = TransformerFactoryTest.class.getResourceAsStream("../../format/export_dataset.json");
        try (JsonParser parser = mapper.getFactory().createParser(inputStream)) {
            final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);

            // when
            transformer.buildExecutable(dataSet, configuration).execute();
            // then
            assertThat(outputStream.toString()).isEqualTo(expectedCsv);
        }

    }
}
