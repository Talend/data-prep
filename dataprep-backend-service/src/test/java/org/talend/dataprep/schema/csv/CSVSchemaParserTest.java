// ============================================================================
//
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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.util.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.Schema;
import org.talend.dataprep.schema.SchemaParser;

/**
 * Unit test for the CSVSchemaParser class.
 * 
 * @see CSVSchemaParser
 */
public class CSVSchemaParserTest extends AbstractSchemaTestUtils {

    /** The csvSchemaParser to test. */
    @Autowired
    private CSVSchemaParser csvSchemaParser;

    /** The format guesser to test. */
    @Autowired
    CSVFormatFamily csvFormatFamily;

    @Autowired
    CSVFormatUtils csvFormatUtils;

    @Test
    public void should_parse_csv() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("simple.csv")) {

            final String[] columns = { "first name", "last name" };
            DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata(columns);
            resetParameters(datasetMetadata, ";", Arrays.asList(columns), 1, false);

            Schema result = csvSchemaParser.parse(new SchemaParser.Request(inputStream, datasetMetadata));
            List<ColumnMetadata> actual = result.getSheetContents().get(0).getColumnMetadatas();

            Assert.assertEquals(datasetMetadata.getRowMetadata().getColumns(), actual);
        }
    }

    /**
     * When trying to guess the columns data type an IndexOutOfBoundsException should not be thrown.
     * 
     * @throws IOException
     */
    @Test
    public void TDP_898() throws IOException {
        String str = "c1;c2" + System.lineSeparator() + "1;2;false";
        try (InputStream inputStream = new ByteArrayInputStream(str.getBytes())) {
            final String[] columns = { "c1", "c2" };
            DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata(columns);
            resetParameters(datasetMetadata, ";", Arrays.asList(columns), 1, false);
            try {
                csvSchemaParser.parse(new SchemaParser.Request(inputStream, datasetMetadata));
            } catch (IndexOutOfBoundsException exc) {
                Assert.fail("Should not throw an IndexOutOfBoundsException, when parsing!");
            }
        }
    }

    /**
     * When trying to guess an input with | as separator separator should be found.
     *
     * @throws IOException
     */
    @Test
    public void shouldFindPipeSeparator() throws IOException {
        String str = "c1|c2|c3" + System.lineSeparator() + "1|2|false";
        try (InputStream inputStream = new ByteArrayInputStream(str.getBytes())) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#3");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals('|', actual);
        }
    }

    /**
     * csv file with 2 possible separators : ';' or '/', ';' should be selected
     */
    @Test
    public void should_guess_best_separator() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("mixed_separators.csv")) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#1");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);

            assertEquals(';', actual);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-181
     */
    @Test
    public void should_guess_best_separator_out_of_two() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-181.csv")) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#2");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', actual);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-258
     */
    @Test
    public void should_guess_separator_with_ISO_8859_1_encoded_file() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("iso-8859-1.csv")) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#3");
            request.getMetadata().setEncoding("ISO-8859-1");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', actual);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-863
     */
    @Test
    public void should_guess_valid_separator_when_most_likely_separator_is_not_valid() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-863.csv")) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#4");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', actual);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-832
     */
    @Test
    public void should_guess_valid_separator_from_access_log_file() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-832.csv")) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#5");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(' ', actual);
        }
    }

    @Test
    public void should_not_detect_char_or_digit_separator_candidate() {
        Map<Character, Separator> separatorMap = new HashMap<>();
        char[] cases = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        for (char candidate : cases) {
            csvSchemaParser.processCharAsSeparatorCandidate(candidate, separatorMap, CSVSchemaParser.DEFAULT_VALID_SEPARATORS, 0);
        }
        assertTrue(separatorMap.isEmpty());
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1060
     */
    @Test
    public void TDP_1060() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-1060.csv")) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#6");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(',', actual);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1060
     */
    @Test
    public void consistency_test() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("consistency_example.csv")) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#7");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(',', actual);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1240 Randomly separators were not detected for files with
     * wrong characters.
     *
     */
    @Test
    public void TDP_1240_should_detect_separator_if_wrong_characters_do_not_exceed_the_threshold() throws IOException {

        char[] chars = new char[50];
        for (int i = 0; i < 48; i++) {
            chars[i] = 0;
        }
        chars[48] = ';';
        chars[49] = '\n';
        try (InputStream inputStream = IOUtils.toInputStream(new String(chars))) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#8");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', actual);
        }

        chars = new char[50];
        for (int i = 0; i < 48; i++) {
            chars[i] = 65533;
        }
        chars[48] = ';';
        chars[49] = '\n';
        try (InputStream inputStream = IOUtils.toInputStream(new String(chars))) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#9");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', actual);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1240 Randomly separators were not detected for files with
     * wrong characters.
     *
     */
    @Test
    public void TDP_1240_should_detect_separator_if_wrong_characters_exceed_threshold() throws IOException {

        // null character
        char[] chars = new char[51];
        for (int i = 0; i < 49; i++) {
            chars[i] = 0;
        }
        chars[49] = ';';
        chars[50] = '\n';
        try (InputStream inputStream = IOUtils.toInputStream(new String(chars))) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#10");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', actual);
        }

        // wrong character
        chars = new char[51];
        for (int i = 0; i < 49; i++) {
            chars[i] = 65533;
        }
        chars[49] = ';';
        chars[50] = '\n';
        try (InputStream inputStream = IOUtils.toInputStream(new String(chars))) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#11");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', actual);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1240 Randomly separators were not detected for files with
     * wrong characters.
     *
     */
    @Test
    public void should_detect_comma_separator_when_no_separator_detected() throws IOException {

        char[] chars = new char[5];
        for (int i = 0; i < 5; i++) {
            chars[i] = 'A';
        }
        try (InputStream inputStream = IOUtils.toInputStream(new String(chars))) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#12");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(',', actual);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1259 We should import whole header of csv files even if
     * some fields are duplicated.
     *
     */
    @Test
    public void TDP_1259() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-1259.csv")) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#1");
            request.getMetadata().setEncoding("UTF-8");

            csvSchemaParser.parse(request);
            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);

            List<String> header = csvFormatUtils.retrieveHeader(parameters);
            assertEquals(';', actual);
            List<String> expected = Arrays.asList("id", "first_name", "last_name", "email", "job_title", "company", "city",
                    "state", "country", "date", "campaign_id", "lead_score", "registration", "city", "birth", "nbCommands", "id",
                    "first_name", "last_name", "email", "job_title", "company", "city", "state", "country", "date", "campaign_id",
                    "lead_score", "registration", "city", "birth", "nbCommands");
            assertEquals(expected, header);
        }
    }

    @Test
    public void TDP_2171() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("TDP-2171.csv")) {
            // We do know the format and therefore we go directly to the CSV schema guessing
            SchemaParser.Request request = getRequest(inputStream, "#1");
            request.getMetadata().setEncoding("UTF-8");

            Schema schema = csvSchemaParser.parse(request);

            final Map<String, String> parameters = request.getMetadata().getContent().getParameters();
            char actual = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);

            List<String> header = csvFormatUtils.retrieveHeader(parameters);
            assertEquals(',', actual);
            assertEquals(92, header.size());
        }
    }

    @Test
    public void should_accept_csv_update() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("toto").formatFamilyId("formatGuess#csv").build();
        assertTrue(csvSchemaParser.accept(metadata));
    }

    @Test
    public void should_not_accept_xls_update() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("tata").formatFamilyId("formatGuess#xls").build();
        assertFalse(csvSchemaParser.accept(metadata));
    }

    @Test
    public void should_not_accept_html_update() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("tata").formatFamilyId("formatGuess#html").build();
        assertFalse(csvSchemaParser.accept(metadata));
    }

    /**
     * Uses the <tt>separator</tt>, <tt>headers</tt>, and <tt>headerNbLines</tt> to reset the parameters of a given
     * metadata.
     *
     * @param dataSetMetadata the specified dataset metadata
     * @param separator the specified separator
     * @param headers the specified headers
     * @param headerNbLines the specified number of lines spanned by the headers
     * @param isFirstLineHeader true if the first line of the dataset is a headers
     */
    private void resetParameters(DataSetMetadata dataSetMetadata, String separator, List<String> headers, int headerNbLines,
            boolean isFirstLineHeader) {
        dataSetMetadata.getContent().setNbLinesInHeader(headerNbLines);
        Separator newSeparator = new Separator(separator.charAt(0));
        final List<Pair<String, Type>> columns = new ArrayList<>();
        headers.stream().forEach(h -> columns.add(new Pair<>(h, Type.STRING)));
        newSeparator.setHeaders(columns); // default type to string
        newSeparator.setFirstLineAHeader(isFirstLineHeader);
        dataSetMetadata.getContent().setParameters(
                csvFormatUtils.compileParameterProperties(newSeparator, dataSetMetadata.getContent().getParameters()));
    }
}
