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

package org.talend.dataprep.transformation.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
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
    public static final String SEPARATOR_PARAM_NAME = ExportFormat.PREFIX + CSVFormat.ParametersCSV.FIELDS_DELIMITER;

    /** Escape character argument name. */
    public static final String ESCAPE_CHARACTER_PARAM_NAME = ExportFormat.PREFIX + CSVFormat.ParametersCSV.ESCAPE_CHAR;

    /** Enclosure character argument name. */
    public static final String ENCLOSURE_CHARACTER_PARAM_NAME = ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCLOSURE_CHAR;

    /** Enclosure character argument name. */
    public static final String ENCLOSURE_MODE_PARAM_NAME = ExportFormat.PREFIX + CSVFormat.ParametersCSV.ENCLOSURE_MODE;

    /** Where the writer should... write! */
    private OutputStream outputStream;

    @Before
    public void init() {
        outputStream = new ByteArrayOutputStream();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SEPARATOR_PARAM_NAME, ";");
        writer = (CSVWriter) context.getBean("writer#CSV", outputStream, parameters);
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-722
     */
    @Test
    public void should_write_with_tab_separator() throws Exception {

        // given
        final ByteArrayOutputStream temp = new ByteArrayOutputStream();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SEPARATOR_PARAM_NAME, "\t");
        final CSVWriter tabWriter = (CSVWriter) context.getBean("writer#CSV", temp, parameters);

        final ColumnMetadata column1 = ColumnMetadata.Builder.column().id(1).name("song").type(Type.STRING).build();
        final ColumnMetadata column2 = ColumnMetadata.Builder.column().id(2).name("band").type(Type.STRING).build();
        final List<ColumnMetadata> columns = Arrays.asList(column1, column2);
        final RowMetadata rowMetadata = new RowMetadata(columns);

        Map<String, String> values = new HashMap<>();
        values.put("0001", "last nite");
        values.put("0002", "the Strokes");
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        // when
        tabWriter.write(row);
        tabWriter.write(rowMetadata);
        tabWriter.flush();

        // then

        final String expectedCsv = "\"song\"\t\"band\"\n" + "\"last nite\"\t\"the Strokes\"\n";
        assertThat(temp.toString()).isEqualTo(expectedCsv);
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

        final ColumnMetadata column1 = ColumnMetadata.Builder.column().id(1).name("song").type(Type.STRING).build();
        final ColumnMetadata column2 = ColumnMetadata.Builder.column().id(2).name("band").type(Type.STRING).build();
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

        final String expectedCsv = "\"song\",\"band\"\n" + "\"last #\"nite\",\"the Strokes\"\n";
        assertThat(temp.toString()).isEqualTo(expectedCsv);
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

        final ColumnMetadata column1 = ColumnMetadata.Builder.column().id(1).name("song").type(Type.STRING).build();
        final ColumnMetadata column2 = ColumnMetadata.Builder.column().id(2).name("band").type(Type.STRING).build();
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

        final String expectedCsv = "\"song\",\"band\"\n" + "\"last \"\"nite\",\"the Strokes\"\n";
        assertThat(temp.toString()).isEqualTo(expectedCsv);
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

        final ColumnMetadata column1 = ColumnMetadata.Builder.column().id(1).name("song").type(Type.STRING).build();
        final ColumnMetadata column2 = ColumnMetadata.Builder.column().id(2).name("band").type(Type.STRING).build();
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
        final String expectedCsv = "+song+,+band+\n" + "+last \"\"nite+,+the Strokes+\n";
        assertThat(temp.toString()).isEqualTo(expectedCsv);
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

        final ColumnMetadata column1 = ColumnMetadata.Builder.column().id(1).name("song").type(Type.STRING).build();
        final ColumnMetadata column2 = ColumnMetadata.Builder.column().id(2).name("members").type(Type.INTEGER).build();
        final ColumnMetadata column3 = ColumnMetadata.Builder.column().id(3).name("band").type(Type.STRING).build();
        final ColumnMetadata column4 = ColumnMetadata.Builder.column().id(4).name("date").type(Type.DATE).build();
        final List<ColumnMetadata> columns = Arrays.asList(column1, column2, column3, column4);
        final RowMetadata rowMetadata = new RowMetadata(columns);

        Map<String, String> values = new HashMap<>();
        values.put("0001", "last \"nite");
        values.put("0002", "5");
        values.put("0003", "the Strokes");
        values.put("0004", "1998");
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        // when
        tabWriter.write(row);
        tabWriter.write(rowMetadata);
        tabWriter.flush();

        // then
        final String expectedCsv = "%song%,members,%band%,date\n" + "%last \"\"nite%,5,%the Strokes%,1998\n";
        assertThat(temp.toString()).isEqualTo(expectedCsv);
    }

    @Test
    public void write_should_write_columns() throws Exception {
        // given
        List<ColumnMetadata> columns = new ArrayList<>(2);
        columns.add(ColumnMetadata.Builder.column().id(1).name("id").type(Type.STRING).build());
        columns.add(ColumnMetadata.Builder.column().id(2).name("firstname").type(Type.STRING).build());

        // when
        writer.write(new RowMetadata(columns));
        writer.flush();

        // then
        assertThat(outputStream.toString()).isEqualTo("\"id\";\"firstname\"\n");
    }

    @Test
    public void write_should_not_throw_exception_when_write_columns_have_not_been_called() throws Exception {
        // given
        final DataSetRow row = new DataSetRow(Collections.emptyMap());

        // when
        writer.write(row);
    }

    @Test
    public void write_should_write_row() throws Exception {
        // given
        final ColumnMetadata column1 = ColumnMetadata.Builder.column().id(1).name("id").type(Type.STRING).build();
        final ColumnMetadata column2 = ColumnMetadata.Builder.column().id(2).name("firstname").type(Type.STRING).build();
        final List<ColumnMetadata> columns = Arrays.asList(column1, column2);

        Map<String, String> values = new HashMap<>();
        values.put("0001", "64a5456ac148b64524ef165");
        values.put("0002", "Superman");
        final DataSetRow row = new DataSetRow(new RowMetadata(columns), values);

        final String expectedCsv = "\"id\";\"firstname\"\n" + "\"64a5456ac148b64524ef165\";\"Superman\"\n";

        // when
        writer.write(row);
        writer.write(new RowMetadata(columns));
        writer.flush();

        // then
        assertThat(outputStream.toString()).isEqualTo(expectedCsv);
    }

}
