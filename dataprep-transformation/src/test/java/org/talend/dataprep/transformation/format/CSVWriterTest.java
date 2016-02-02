//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;

/**
 * Unit test for the CSVWriter.
 * 
 * @see CSVWriter
 */
public class CSVWriterTest extends BaseFormatTest {

    /** The writer to test. */
    private CSVWriter writer;

    /** Where the writer should... write! */
    private OutputStream outputStream;

    @Before
    public void init() {
        outputStream = new ByteArrayOutputStream();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(CSVWriter.SEPARATOR_PARAM_NAME, ";");
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
        parameters.put(CSVWriter.SEPARATOR_PARAM_NAME, "\t");
        final CSVWriter tabWriter = (CSVWriter) context.getBean("writer#CSV", temp, parameters);

        final ColumnMetadata column1 = ColumnMetadata.Builder.column().id(1).name("song").type(Type.STRING).build();
        final ColumnMetadata column2 = ColumnMetadata.Builder.column().id(2).name("band").type(Type.STRING).build();
        final List<ColumnMetadata> columns = Arrays.asList(column1, column2);

        final DataSetRow row = new DataSetRow(Collections.emptyMap());
        row.set("0001", "last nite");
        row.set("0002", "the Strokes");

        // when
        tabWriter.write(row);
        tabWriter.write(new RowMetadata(columns));
        tabWriter.flush();

        // then

        final String expectedCsv = "\"song\"\t\"band\"\n" + "\"last nite\"\t\"the Strokes\"\n";
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

        final DataSetRow row = new DataSetRow(Collections.emptyMap());
        row.set("0001", "64a5456ac148b64524ef165");
        row.set("0002", "Superman");

        final String expectedCsv = "\"id\";\"firstname\"\n" + "\"64a5456ac148b64524ef165\";\"Superman\"\n";

        // when
        writer.write(row);
        writer.write(new RowMetadata(columns));
        writer.flush();

        // then
        assertThat(outputStream.toString()).isEqualTo(expectedCsv);
    }

}