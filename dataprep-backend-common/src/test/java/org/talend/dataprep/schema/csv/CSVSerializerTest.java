package org.talend.dataprep.schema.csv;

import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.IoTestUtils;

/**
 * Unit test for the CSVSerializer test.
 * 
 * @see CSVSerializer
 */
public class CSVSerializerTest extends AbstractSchemaTestUtils {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private IoTestUtils ioTestUtils;

    /** The Serializer to test. */
    private CSVSerializer serializer;

    @Before
    public void setUp() throws Exception {
        serializer = context.getBean(CSVSerializer.class);
    }

    @Test
    public void should_serialize_standard_csv() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("simple.csv");
        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata("first name", "last name");
        datasetMetadata.getContent().setNbLinesInHeader(1);

        InputStream input = serializer.serialize(inputStream, datasetMetadata);
        String actual = IOUtils.toString(input);

        InputStream expected = this.getClass().getResourceAsStream("simple.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }

    @Test
    public void should_serialize_csv_with_missing_values() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("missing_values.csv");
        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata("character", "actor", "active");
        datasetMetadata.getContent().setNbLinesInHeader(1);

        InputStream input = serializer.serialize(inputStream, datasetMetadata);
        String actual = IOUtils.toString(input);

        InputStream expected = this.getClass().getResourceAsStream("missing_values.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }

    @Test
    public void should_serialize_csv_with_additional_values() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("additional_values.csv");
        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata("name", "email");
        datasetMetadata.getContent().setNbLinesInHeader(1);

        InputStream input = serializer.serialize(inputStream, datasetMetadata);
        String actual = IOUtils.toString(input);

        InputStream expected = this.getClass().getResourceAsStream("additional_values.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }

    @Test
    public void should_serialize_csv_with_two_lines_header() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("two_lines_header.csv");
        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata("first name", "last name");
        datasetMetadata.getContent().setNbLinesInHeader(2);

        InputStream input = serializer.serialize(inputStream, datasetMetadata);
        String actual = IOUtils.toString(input);

        InputStream expected = this.getClass().getResourceAsStream("simple.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }
}
