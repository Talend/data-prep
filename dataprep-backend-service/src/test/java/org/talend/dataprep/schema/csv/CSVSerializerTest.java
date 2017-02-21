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
    public void setUp() {
        serializer = context.getBean(CSVSerializer.class);
    }

    @Test
    public void should_serialize_standard_csv() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("simple.csv");
        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata("first name", "last name");
        datasetMetadata.getContent().setNbLinesInHeader(1);

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input);

        InputStream expected = this.getClass().getResourceAsStream("simple.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }

    @Test
    public void should_serialize_csv_with_missing_values() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("missing_values.csv");
        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata("character", "actor", "active");
        datasetMetadata.getContent().setNbLinesInHeader(1);

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input);

        InputStream expected = this.getClass().getResourceAsStream("missing_values.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }

    @Test
    public void should_serialize_csv_with_additional_values() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("additional_values.csv");
        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata("name", "email");
        datasetMetadata.getContent().setNbLinesInHeader(1);

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input);

        InputStream expected = this.getClass().getResourceAsStream("additional_values.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }

    @Test
    public void should_serialize_csv_with_two_lines_header() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("two_lines_header.csv");
        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata("first name", "last name");
        datasetMetadata.getContent().setNbLinesInHeader(2);

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input);

        InputStream expected = this.getClass().getResourceAsStream("simple.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }

    @Test
    public void should_serialize_csv_with_the_specified_encoding() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("x_mac_roman.csv");
        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata("Titre", "Pr�nom");
        datasetMetadata.getContent().setNbLinesInHeader(1);
        datasetMetadata.setEncoding("x-MacRoman");

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input);

        // strange json because schema has not been detected
        String expected = "[{\"0000\":\",\\\"GÈrard\",\"0001\":null}]";
        Assert.assertEquals(expected, actual);
    }

    /**
     * Please have a look at <a href="https://jira.talendforge.org/browse/TDP-1623>TDP-1623</a>
     * @throws IOException
     */
    @Test
    public void should_serialize_csv_with_backslash() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("tdp-1623_backslash_not_imported.csv");
        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata("City");
        datasetMetadata.getContent().setNbLinesInHeader(1);

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input);

        String expected = "[{\"0000\":\"Carson City\\\\Seine\"}]";
        Assert.assertEquals(expected, actual);
    }
}
