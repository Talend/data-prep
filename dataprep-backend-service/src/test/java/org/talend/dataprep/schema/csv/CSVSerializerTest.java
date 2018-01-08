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

package org.talend.dataprep.schema.csv;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.talend.dataprep.schema.csv.CSVFormatFamily.*;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;

/**
 * Unit test for the CSVSerializer test.
 *
 * @see CSVSerializer
 */
public class CSVSerializerTest {

    /** The Serializer to test. */
    private CSVSerializer serializer;

    public CSVSerializerTest() {
        this.serializer = new CSVSerializer();
        TaskExecutor executor = new SimpleAsyncTaskExecutor();
        ReflectionTestUtils.setField(serializer, "executor", executor);
        ReflectionTestUtils.setField(serializer, "defaultTextEnclosure", "\"");
        ReflectionTestUtils.setField(serializer, "defaultEscapeChar", "\u0000");

    }

    @Test
    public void should_serialize_standard_csv() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("simple.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("first name", "last name");

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        InputStream expected = this.getClass().getResourceAsStream("simple.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }

    @Test
    public void should_serialize_csv_with_missing_values() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("missing_values.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("character", "actor", "active");

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        InputStream expected = this.getClass().getResourceAsStream("missing_values.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }

    @Test
    public void should_serialize_csv_with_additional_values() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("additional_values.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("name", "email");

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        InputStream expected = this.getClass().getResourceAsStream("additional_values.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }

    @Test
    public void should_serialize_csv_with_two_lines_header() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("two_lines_header.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("first name", "last name");
        datasetMetadata.getContent().setNbLinesInHeader(2);

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        InputStream expected = this.getClass().getResourceAsStream("simple.csv_expected.json");
        Assert.assertThat(actual, sameJSONAsFile(expected));
    }

    @Test
    public void should_serialize_csv_with_the_specified_encoding() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("x_mac_roman.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("Titre", "Pr�nom");
        datasetMetadata.setEncoding("x-MacRoman");

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        // strange json because schema has not been detected
        String expected = "[{\"0000\":\",\\\"GÈrard\",\"0001\":null}]";
        Assert.assertEquals(expected, actual);
    }

    /**
     * Please have a look at <a href="https://jira.talendforge.org/browse/TDP-1623>TDP-1623</a>
     */
    @Test
    public void should_serialize_csv_with_backslash() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("tdp-1623_backslash_not_imported.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("City");

        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        String expected = "[{\"0000\":\"Carson City\\\\Seine\"}]";
        Assert.assertEquals(expected, actual);
    }

    /**
     * Please, have a look at <a href="https://jira.talendforge.org/browse/TDP-2366">TDP-2366</a>
     */
    @Test
    public void shouldManageSpecificTextEnclosureChar() throws IOException {
        // given (text enclosing separator is ¤ so that " in original CSV are ignored)
        InputStream inputStream = this.getClass().getResourceAsStream("with_quote_in_text.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("City", "code", "Description");
        datasetMetadata.getContent().addParameter(SEPARATOR_PARAMETER, ",");
        datasetMetadata.getContent().addParameter(TEXT_ENCLOSURE_CHAR, "¤");

        // when
        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        // then
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("with_quote_in_text.json"), UTF_8);
        JSONAssert.assertEquals(expected, actual, false);
    }

    /**
     * Please, have a look at <a href="https://jira.talendforge.org/browse/TDP-2366">TDP-2366</a>
     */
    @Test
    public void shouldManageSpecificEscapeChar() throws IOException {
        // given (text escape char is |)
        InputStream inputStream = this.getClass().getResourceAsStream("with_another_escape_char.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("City", "code", "Description");
        datasetMetadata.getContent().addParameter(SEPARATOR_PARAMETER, ",");
        datasetMetadata.getContent().addParameter(ESCAPE_CHAR, "|");

        // when
        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        // then
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("with_another_escape_char.json"), UTF_8);
        JSONAssert.assertEquals(expected, actual, false);
    }

    // https://jira.talendforge.org/browse/TDP-4602
    @Test
    public void should_use_custom_import_parameters_double_quote_escape() throws IOException {

        // given (text escape char is |)
        InputStream inputStream = this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("column1", "column2");
        datasetMetadata.getContent().addParameter(SEPARATOR_PARAMETER, ",");
        datasetMetadata.getContent().addParameter(ESCAPE_CHAR, "\"");
        datasetMetadata.getContent().addParameter(TEXT_ENCLOSURE_CHAR, "+");

        // when
        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        // then
        final String expected = IOUtils.toString(
                this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import_double_quote_escape.json"), UTF_8);
        JSONAssert.assertEquals(expected, actual, false);
    }

    // https://jira.talendforge.org/browse/TDP-4602
    @Test
    public void should_use_custom_import_parameters_backslash_escape() throws IOException {

        // given (text escape char is |)
        InputStream inputStream = this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("column1", "column2");
        datasetMetadata.getContent().addParameter(SEPARATOR_PARAMETER, ",");
        datasetMetadata.getContent().addParameter(ESCAPE_CHAR, "\\");
        datasetMetadata.getContent().addParameter(TEXT_ENCLOSURE_CHAR, "+");

        // when
        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        // then
        final String expected = IOUtils
                .toString(this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import_backslash_escape.json"), UTF_8);
        JSONAssert.assertEquals(expected, actual, false);
    }

    // https://jira.talendforge.org/browse/TDP-4602
    @Test
    public void should_use_custom_import_parameters_default_empty_escape() throws IOException {

        // given (text escape char is |)
        InputStream inputStream = this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("column1", "column2");
        datasetMetadata.getContent().addParameter(SEPARATOR_PARAMETER, ",");
        datasetMetadata.getContent().addParameter(TEXT_ENCLOSURE_CHAR, "+");

        // when
        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        // then
        final String expected = IOUtils
                .toString(this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import_default_escape.json"), UTF_8);
        JSONAssert.assertEquals(expected, actual, false);
    }

    // https://jira.talendforge.org/browse/TDP-4602
    @Test
    public void should_use_custom_import_parameters_empty_escape() throws IOException {

        // given (text escape char is |)
        InputStream inputStream = this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("column1", "column2");
        datasetMetadata.getContent().addParameter(SEPARATOR_PARAMETER, ",");
        datasetMetadata.getContent().addParameter(ESCAPE_CHAR, "");
        datasetMetadata.getContent().addParameter(TEXT_ENCLOSURE_CHAR, "+");

        // when
        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        // then
        final String expected = IOUtils
                .toString(this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import_default_escape.json"), UTF_8);
        JSONAssert.assertEquals(expected, actual, false);
    }

    // https://jira.talendforge.org/browse/TDP-4602
    @Test
    public void should_use_custom_import_parameters() throws IOException {

        // given (text escape char is |)
        InputStream inputStream = this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("column1", "column2");
        datasetMetadata.getContent().addParameter(SEPARATOR_PARAMETER, ",");
        datasetMetadata.getContent().addParameter(ESCAPE_CHAR, "'");
        datasetMetadata.getContent().addParameter(TEXT_ENCLOSURE_CHAR, "+");

        // when
        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        // then
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import.json"),
                UTF_8);
        JSONAssert.assertEquals(expected, actual, false);
    }

    // https://jira.talendforge.org/browse/TDP-4602
    @Test
    public void should_use_custom_import_parameters_empty_enclosure() throws IOException {

        // given (text escape char is |)
        InputStream inputStream = this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("column1", "column2");
        datasetMetadata.getContent().addParameter(SEPARATOR_PARAMETER, ",");
        datasetMetadata.getContent().addParameter(ESCAPE_CHAR, "\"");
        datasetMetadata.getContent().addParameter(TEXT_ENCLOSURE_CHAR, "");

        // when
        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        // then
        final String expected = IOUtils
                .toString(this.getClass().getResourceAsStream("tdp-4602_custom_param_csv_import_empty_enclosure.json"), UTF_8);
        JSONAssert.assertEquals(expected, actual, false);
    }

    // https://jira.talendforge.org/browse/TDP-4579
    @Test
    public void should_use_custom_import_parameters_double_quote() throws IOException {

        // given (text escape char is |)
        InputStream inputStream = this.getClass().getResourceAsStream("test_4579_doublequote_import.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("City", "code","Description");
        datasetMetadata.getContent().addParameter(SEPARATOR_PARAMETER, ",");
        datasetMetadata.getContent().addParameter(ESCAPE_CHAR, "");
        datasetMetadata.getContent().addParameter(TEXT_ENCLOSURE_CHAR, "");

        // when
        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        // then
        final String expected = IOUtils
                .toString(this.getClass().getResourceAsStream("test_4579_doublequote_import.json"), UTF_8);
        JSONAssert.assertEquals(expected, actual, false);
    }

    // https://jira.talendforge.org/browse/TDP-4579
    @Test
    public void should_use_custom_import_parameters_pb_double_quote() throws IOException {

        // given (text escape char is |)
        InputStream inputStream = this.getClass().getResourceAsStream("test_4579_doublequote_import.csv");
        DataSetMetadata datasetMetadata = getSimpleDataSetMetadata("City", "code","Description");
        datasetMetadata.getContent().addParameter(SEPARATOR_PARAMETER, ",");

        // when
        InputStream input = serializer.serialize(inputStream, datasetMetadata, -1);
        String actual = IOUtils.toString(input, UTF_8);

        // then
        final String expected = IOUtils
                .toString(this.getClass().getResourceAsStream("test_4579_doublequote_import_pb.json"), UTF_8);
        JSONAssert.assertEquals(expected, actual, false);
    }

    private DataSetMetadata getSimpleDataSetMetadata(String... columnsName) {
        List<ColumnMetadata> columns = new ArrayList<>(columnsName.length);
        for (int i = 0; i < columnsName.length; i++) {
            columns.add(ColumnMetadata.Builder.column().id(i).name(columnsName[i]).type(Type.STRING).build());
        }

        DataSetMetadata datasetMetadata = new DataSetMetadata();
        datasetMetadata.setId(UUID.randomUUID().toString());
        datasetMetadata.getContent().addParameter(SEPARATOR_PARAMETER, ";");
        datasetMetadata.getContent().setNbLinesInHeader(1);
        datasetMetadata.setRowMetadata(new RowMetadata((columns)));

        return datasetMetadata;
    }

}
