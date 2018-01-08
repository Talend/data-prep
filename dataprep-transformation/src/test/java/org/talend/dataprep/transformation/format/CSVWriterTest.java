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

package org.talend.dataprep.transformation.format;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.AbstractTransformerWriterTest;

/**
 * Unit test for the CSVWriter.
 *
 * @see CSVWriter
 */
public class CSVWriterTest extends AbstractTransformerWriterTest {

    /** Separator argument name. */
    static final String SEPARATOR_PARAM_NAME = ExportFormat.PREFIX + CSVFormat.ParametersCSV.FIELDS_DELIMITER;

    /** Escape character argument name. */
    private static final String ESCAPE_CHARACTER_PARAM_NAME = ExportFormat.PREFIX + CSVFormat.ParametersCSV.ESCAPE_CHAR;

    /** Enclosure character argument name. */
    private static final String ENCLOSURE_CHARACTER_PARAM_NAME = ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCLOSURE_CHAR;

    /** Enclosure character argument name. */
    private static final String ENCLOSURE_MODE_PARAM_NAME = ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCLOSURE_MODE;

    private static final String NON_ASCII_TEST_CHARS = "ñóǹ äŝçíì 汉语/漢語  华语/華語 Huáyǔ; 中文 Zhōngwén 漢字仮名交じり文 Lech Wałęsa æøå";

    /** Enclosure character argument name. */
    private static final String DELIMITER_CHAR_PARAM_NAME = ExportFormat.PREFIX + CSVFormat.ParametersCSV.FIELDS_DELIMITER;

    @Before
    public void init() {
        // to avoid breaking stranger abstract test "should_only_write_values_in_columns_order_TDP_3188" final
        // ByteArrayOutputStream temp = new ByteArrayOutputStream();
        writer = (CSVWriter) context.getBean("writer#CSV", new ByteArrayOutputStream(), emptyMap());
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-722
     */
    @Test
    public void should_write_with_tab_separator() throws Exception {

        // given
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SEPARATOR_PARAM_NAME, "\t");

        final ColumnMetadata column1 = column().id(1).name("song").type(Type.STRING).build();
        final ColumnMetadata column2 = column().id(2).name("band").type(Type.STRING).build();
        final List<ColumnMetadata> columns = Arrays.asList(column1, column2);
        final RowMetadata rowMetadata = new RowMetadata(columns);

        Map<String, String> values = new HashMap<>();
        values.put("0001", "last nite");
        values.put("0002", "the Strokes");
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        final ByteArrayOutputStream temp = writeCsv(parameters, rowMetadata, singletonList(row));

        // then
        final String expectedCsv = "\"song\"\t\"band\"\n" + "\"last nite\"\t\"the Strokes\"\n";
        assertThat(temp.toString(UTF_8.name())).isEqualTo(expectedCsv);
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-4390
     */
    @Test
    public void should_write_with_any_escape_character() throws Exception {
        // given
        final ByteArrayOutputStream temp = new ByteArrayOutputStream();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ESCAPE_CHARACTER_PARAM_NAME, "#");
        final CSVWriter tabWriter = (CSVWriter) context.getBean("writer#CSV", temp, parameters);

        final ColumnMetadata column1 = column().id(1).name("song").type(Type.STRING).build();
        final ColumnMetadata column2 = column().id(2).name("band").type(Type.STRING).build();
        final List<ColumnMetadata> columns = Arrays.asList(column1, column2);
        final RowMetadata rowMetadata = new RowMetadata(columns);

        Map<String, String> values = new HashMap<>();
        values.put("0001", "last \"nite");
        values.put("0002", "the Strokes");
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        // when
        tabWriter.write(row);
        tabWriter.write(rowMetadata);
        tabWriter.flush();

        // then

        final String expectedCsv = "\"song\";\"band\"\n" + "\"last #\"nite\";\"the Strokes\"\n";
        assertThat(temp.toString(UTF_8.name())).isEqualTo(expectedCsv);
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-4390
     */
    @Test
    public void should_use_quote_as_default_escape_character() throws Exception {
        // given
        final ByteArrayOutputStream temp = new ByteArrayOutputStream();
        Map<String, Object> parameters = new HashMap<>();
        final CSVWriter tabWriter = (CSVWriter) context.getBean("writer#CSV", temp, parameters);

        final ColumnMetadata column1 = column().id(1).name("song").type(Type.STRING).build();
        final ColumnMetadata column2 = column().id(2).name("band").type(Type.STRING).build();
        final List<ColumnMetadata> columns = Arrays.asList(column1, column2);
        final RowMetadata rowMetadata = new RowMetadata(columns);

        Map<String, String> values = new HashMap<>();
        values.put("0001", "last \"nite");
        values.put("0002", "the Strokes");
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        // when
        tabWriter.write(row);
        tabWriter.write(rowMetadata);
        tabWriter.flush();

        // then

        final String expectedCsv = "\"song\";\"band\"\n" + "\"last \"\"nite\";\"the Strokes\"\n";
        assertThat(temp.toString(UTF_8.name())).isEqualTo(expectedCsv);
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-4389
     */
    @Test
    public void should_enclose_all_columns() throws Exception {
        // given
        final ByteArrayOutputStream temp = new ByteArrayOutputStream();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ENCLOSURE_CHARACTER_PARAM_NAME, "+");
        final CSVWriter tabWriter = (CSVWriter) context.getBean("writer#CSV", temp, parameters);

        final ColumnMetadata column1 = column().id(1).name("song").type(Type.STRING).build();
        final ColumnMetadata column2 = column().id(2).name("band").type(Type.STRING).build();
        final List<ColumnMetadata> columns = Arrays.asList(column1, column2);
        final RowMetadata rowMetadata = new RowMetadata(columns);

        Map<String, String> values = new HashMap<>();
        values.put("0001", "last \"nite");
        values.put("0002", "the Strokes");
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        // when
        tabWriter.write(row);
        tabWriter.write(rowMetadata);
        tabWriter.flush();

        // then
        final String expectedCsv = "+song+;+band+\n" + "+last \"\"nite+;+the Strokes+\n";
        assertThat(temp.toString(UTF_8.name())).isEqualTo(expectedCsv);
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-4389
     */
    @Test
    public void should_enclose_only_text() throws Exception {
        // given
        final ByteArrayOutputStream temp = new ByteArrayOutputStream();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ENCLOSURE_CHARACTER_PARAM_NAME, "%");
        parameters.put(ENCLOSURE_MODE_PARAM_NAME, "text_only");
        final CSVWriter tabWriter = (CSVWriter) context.getBean("writer#CSV", temp, parameters);

        final DataSetRow row = getDataSetRow();

        // when
        tabWriter.write(row);
        tabWriter.write(row.getRowMetadata());
        tabWriter.flush();

        // then
        final String header = "%song%;%members%;%band%;%date%;%alive%;%old%;%any%\n";
        final String rowValues = "%last \"\"nite%;5;%the Strokes%;1998;true;2.5;2.5\"%\n";
        final String expectedCsv = header + rowValues;
        assertThat(temp.toString(UTF_8.name())).isEqualTo(expectedCsv);
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-4389
     */
    @Test
    public void should_neither_enclose_nor_escape_with_empty_char() throws Exception {
        // given
        final ByteArrayOutputStream temp = new ByteArrayOutputStream();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ENCLOSURE_CHARACTER_PARAM_NAME, "");
        parameters.put(ESCAPE_CHARACTER_PARAM_NAME, "");
        parameters.put(ENCLOSURE_MODE_PARAM_NAME, "all_fields");
        final CSVWriter tabWriter = (CSVWriter) context.getBean("writer#CSV", temp, parameters);

        final DataSetRow row = getDataSetRow();

        // when
        tabWriter.write(row);
        tabWriter.write(row.getRowMetadata());
        tabWriter.flush();

        // then
        final String header = "song;members;band;date;alive;old;any\n";
        final String rowValues = "last \"nite;5;the Strokes;1998;true;2.5;2.5%\n";
        final String expectedCsv = header + rowValues;
        assertThat(temp.toString(UTF_8.name())).isEqualTo(expectedCsv);
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-4389
     */
    @Test
    public void should_use_custom_params() throws Exception {
        // given
        final ByteArrayOutputStream temp = new ByteArrayOutputStream();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ENCLOSURE_CHARACTER_PARAM_NAME, "+");
        parameters.put(ENCLOSURE_MODE_PARAM_NAME, "all_fields");
        parameters.put(ESCAPE_CHARACTER_PARAM_NAME, "#");
        parameters.put(DELIMITER_CHAR_PARAM_NAME, "-");
        final CSVWriter tabWriter = (CSVWriter) context.getBean("writer#CSV", temp, parameters);

        final ColumnMetadata column1 = ColumnMetadata.Builder.column().id(1).name("id").type(Type.INTEGER).build();
        final ColumnMetadata column2 = ColumnMetadata.Builder.column().id(2).name("lastname").type(Type.STRING).build();
        final ColumnMetadata column3 = ColumnMetadata.Builder.column().id(3).name("firstname").type(Type.STRING).build();
        final List<ColumnMetadata> columns = Arrays.asList(column1, column2, column3);
        final RowMetadata rowMetadata = new RowMetadata(columns);

        Map<String, String> values = new HashMap<>();
        values.put("0001", "1");
        values.put("0002", "DUPONT#");
        values.put("0003", "Jaques + test");
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        // when
        tabWriter.write(row);
        tabWriter.write(rowMetadata);
        tabWriter.flush();

        // then
        final String expectedCsv = "+id+-+lastname+-+firstname+\n+1+-+DUPONT##+-+Jaques #+ test+\n";
        assertThat(temp.toString()).isEqualTo(expectedCsv);
    }

    @Test
    public void write_should_write_columns() throws Exception {
        // given
        List<ColumnMetadata> columns = new ArrayList<>(2);
        columns.add(column().id(1).name("id").type(Type.STRING).build());
        columns.add(column().id(2).name("firstname").type(Type.STRING).build());

        ByteArrayOutputStream out = writeCsv(emptyMap(), new RowMetadata(columns), Collections.emptyList());

        // then
        assertThat(out.toString()).isEqualTo("\"id\";\"firstname\"\n");
    }

    @Test
    public void write_should_not_throw_exception_when_write_columns_have_not_been_called() throws Exception {
        // given
        final DataSetRow row = new DataSetRow(emptyMap());

        // when
        writeCsv(emptyMap(), null, singletonList(row));
    }

    @Test
    public void write_should_write_row() throws Exception {
        // given
        final DataSetRow row = buildSimpleRow();

        final String expectedCsv = "\"id\";\"firstname\"\n" + "\"64a5456ac148b64524ef165\";\"Superman\"\n";

        Map<String, String> parameters = new HashMap<>();
        parameters.put(SEPARATOR_PARAM_NAME, ";");

        ByteArrayOutputStream out = writeCsv(parameters, row.getRowMetadata(), singletonList(row));

        // then
        assertThat(out.toString(UTF_8.name())).isEqualTo(expectedCsv);
    }

    @Test
    public void write_shouldWriteIso885_1() throws Exception {
        // given
        final DataSetRow row = buildSimpleRow();
        row.set("0002", NON_ASCII_TEST_CHARS);

        Map<String, String> parameters = new HashMap<>();
        parameters.put(ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCODING, StandardCharsets.ISO_8859_1.name());

        // when
        ByteArrayOutputStream out = writeCsv(parameters, row.getRowMetadata(), singletonList(row));

        // then
        assertThat(out.toByteArray()).isEqualTo(
                ("\"id\";\"firstname\"\n\"64a5456ac148b64524ef165\";\"" + NON_ASCII_TEST_CHARS + "\"\n").getBytes(ISO_8859_1));
        assertThat(out.toByteArray()).isNotEqualTo(
                ("\"id\";\"firstname\"\n\"64a5456ac148b64524ef165\";\"" + NON_ASCII_TEST_CHARS + "\"\n").getBytes(UTF_8));
    }

    @Test
    public void write_shouldDefaultToUtf8() throws Exception {
        // given
        final DataSetRow row = buildSimpleRow();
        row.set("0002", NON_ASCII_TEST_CHARS);

        Map<String, String> parameters = new HashMap<>();
        String invalidCharsetName = "ISO-8859-560";
        parameters.put(ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCODING, invalidCharsetName);

        // when
        ByteArrayOutputStream out = writeCsv(parameters, row.getRowMetadata(), singletonList(row));

        // then
        assertThat(out.toByteArray()).isEqualTo(
                ("\"id\";\"firstname\"\n\"64a5456ac148b64524ef165\";\"" + NON_ASCII_TEST_CHARS + "\"\n").getBytes(UTF_8));
    }

    @Test
    public void write_shouldEncloseAllFields() throws Exception {
        // given
        final DataSetRow row = buildComplexRow();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCLOSURE_MODE,
                CSVFormat.ParametersCSV.ENCLOSURE_ALL_FIELDS);

        // when
        ByteArrayOutputStream out = writeCsv(parameters, row.getRowMetadata(), singletonList(row));

        // then
        assertThat(out.toString(UTF_8.name()))
                .isEqualTo("\"id\";\"firstname\";\"age\"\n\"64a5456ac148b64524ef165\";\"Superman\";\"10\"\n");
    }

    private DataSetRow buildSimpleRow() {
        final List<ColumnMetadata> columns = new ArrayList<>();
        columns.add(column().id(1).name("id").type(Type.STRING).build());
        columns.add(column().id(2).name("firstname").type(Type.STRING).build());

        Map<String, String> values = new HashMap<>();
        values.put("0001", "64a5456ac148b64524ef165");
        values.put("0002", "Superman");
        return new DataSetRow(new RowMetadata(columns), values);
    }

    private DataSetRow buildComplexRow() {
        DataSetRow dataSetRow = buildSimpleRow();
        final ColumnMetadata column3 = column().id(3).name("age").type(Type.INTEGER).build();
        dataSetRow.getRowMetadata().addColumn(column3);
        dataSetRow.set("0003", "10");
        return dataSetRow;
    }

    private ByteArrayOutputStream writeCsv(Map<String, String> parameters, RowMetadata rowMetadata, List<DataSetRow> rows)
            throws IOException {
        final ByteArrayOutputStream temp = new ByteArrayOutputStream();
        final CSVWriter writer = (CSVWriter) context.getBean("writer#CSV", temp, parameters);
        if (rows != null) {
            for (DataSetRow row : rows) {
                writer.write(row);
            }
        }
        if (rowMetadata != null) {
            writer.write(rowMetadata);
        }
        writer.flush();
        return temp;
    }

    /**
     * @return a default dataset row.
     */
    private DataSetRow getDataSetRow() {
        final ColumnMetadata column1 = column().id(1).name("song").type(Type.STRING).build();
        final ColumnMetadata column2 = column().id(2).name("members").type(Type.INTEGER).build();
        final ColumnMetadata column3 = column().id(3).name("band").type(Type.STRING).build();
        final ColumnMetadata column4 = column().id(4).name("date").type(Type.DATE).build();
        final ColumnMetadata column5 = column().id(5).name("alive").type(Type.BOOLEAN).build();
        final ColumnMetadata column6 = column().id(6).name("old").type(Type.NUMERIC).build();
        final ColumnMetadata column7 = column().id(7).name("any").type(Type.ANY).build();
        final List<ColumnMetadata> columns = Arrays.asList(column1, column2, column3, column4, column5, column6, column7);
        final RowMetadata rowMetadata = new RowMetadata(columns);

        Map<String, String> values = new HashMap<>();
        values.put("0001", "last \"nite");
        values.put("0002", "5");
        values.put("0003", "the Strokes");
        values.put("0004", "1998");
        values.put("0005", "true");
        values.put("0006", "2.5");
        values.put("0007", "2.5%");

        return new DataSetRow(rowMetadata, values);
    }
}
