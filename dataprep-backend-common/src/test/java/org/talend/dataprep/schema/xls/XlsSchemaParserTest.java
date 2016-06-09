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

package org.talend.dataprep.schema.xls;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.FormatFamily;
import org.talend.dataprep.schema.Schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.talend.dataprep.schema.SchemaParser;


/**
 * Unit test for the XLSSchemaParser class.
 *
 * @see XlsSchemaParser
 */
public class XlsSchemaParserTest extends AbstractSchemaTestUtils {

    private final static Logger logger = LoggerFactory.getLogger( XlsSerializerTest.class);

    /** The parser to test. */
    @Autowired
    private XlsSchemaParser parser;


    @Test
    public void should_parse_xls() throws IOException {
        checkColumnsName("simple.xls", "Film", "Producer");
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-827
     */
    @Test
    public void shouldParseFileWithHeader() throws Exception {
        checkColumnsName("file_with_header.xlsx", "col_0", "col_1", "col_2", "col_3", "col_4");
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-830
     */
    @Test
    public void shouldParseFileWithEmptyColumn() throws Exception {
        checkColumnsName("empty_column.xlsx", "First Name", "Last Name", "Company", "Email Address", "col_5", //
                "Current Product", //
                "Product to send"); //
    }

    /**
     * Load the excel file and check the parsed columns name against the given ones.
     *
     * @param sourceFileName   the excel file name to load.
     * @param expectedColsName the expected columns name.
     * @throws IOException if an error occurs while reading the excel file.
     */
    private void checkColumnsName(String sourceFileName, String... expectedColsName) throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream(sourceFileName)) {
            checkColumnsName(inputStream, expectedColsName);
        }
    }

    /**
     * Load the excel file and check the parsed columns name against the given ones.
     *
     * @param inputStream the excel file name as inputStream
     * @param expectedColsName the expected columns name.
     * @throws IOException if an error occurs while reading the excel file.
     */
    private void checkColumnsName(InputStream inputStream, String... expectedColsName) throws IOException {

        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata();

        Schema result = parser.parse(new SchemaParser.Request(inputStream, datasetMetadata));
        List<ColumnMetadata> columns = result.getSheetContents().get(0).getColumnMetadatas();
        final List<String> actual = columns.stream().map(ColumnMetadata::getName).collect(Collectors.toList());

        Assertions.assertThat(actual).containsExactly(expectedColsName);
    }

    @Test
    public void read_xls_TDP_143() throws Exception {

        String fileName = "state_table.xls";

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            List<ColumnMetadata> columnMetadatas = parser.parse(getRequest(inputStream, "#852"))
                    .getSheetContents().get(0).getColumnMetadatas();
            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(17);
        }

    }


    @Test
    public void read_xls_TDP_1957() throws Exception {

        String fileName = "email_with_empty_rows.xlsx";

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            List<ColumnMetadata> columnMetadatas = parser.parse(getRequest(inputStream, "#852"))
                .getSheetContents().get(0).getColumnMetadatas();
            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(2);
        }

    }


    @Test
    public void parse_should_extract_single_sheet_xls() throws Exception {
        // given
        final String fileName = "simple.xls";
        SchemaParser.Request request;
        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            request = getRequest(inputStream, "My Dataset");

            // when
            final Schema schema = parser.parse(request);

            // then
            assertThat(schema.getSheetContents(), is(notNullValue()));
            assertThat(schema.draft(), is(false));
            assertThat(schema.getSheetName(), is("Feuille1"));
        }
    }

    @Test
    public void parse_should_extract_multi_sheet_xls() throws Exception {
        // given
        final String fileName = "Talend_Desk-Tableau-Bord-011214.xls";
        SchemaParser.Request request;
        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            request = getRequest(inputStream, "My Dataset");

            // when
            final Schema schema = parser.parse(request);

            // then
            assertThat(schema.getSheetContents(), is(notNullValue()));
            assertThat(schema.draft(), is(true));
            assertThat(schema.getSheetName(), is("Sumary"));
        }
    }

    @Test
    public void should_not_accept_csv_update() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("toto").formatFamilyId("formatGuess#csv").build();
        assertFalse(parser.accept(metadata));
    }

    @Test
    public void should_not_accept_xls_update() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("tata").formatFamilyId("formatGuess#xls").build();
        assertFalse(parser.accept(metadata));
    }

    @Test
    public void should_not_accept_html_update() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("tata").formatFamilyId("formatGuess#html").build();
        assertFalse(parser.accept(metadata));
    }


    @Test
    public void very_large_import() throws Exception {

        String fileName = "veryhuge.xlsx";
        Path path = Paths.get(fileName);

        if (!Files.exists( path )){
            logger.info( "file {} not available so skip the test" );
            return;
        }

        try (InputStream inputStream = Files.newInputStream(path)) {

            String[] cols = {"id", //
                "first_name", //
                "last_name", //
                "email", //
                "job_title", //
                "company", //
                "city", //
                "state", //
                "country", //
                "date", //
                "campaign_id", //
                "lead_score", //
                "registration", //
                "city", //
                "birth", //
                "nbCommands", //
                "id", //
                "first_name", //
                "last_name", //
                "email", //
                "job_title", //
                "company", //
                "city", //
                "state", //
                "country", //
                "date", //
                "campaign_id", //
                "lead_score", //
                "registration", //
                "city", //
                "birth", //
                "nbCommands", //
                "id", //
                "first_name", //
                "last_name", //
                "email", //
                "job_title", //
                "company", //
                "city", //
                "state", //
                "country", //
                "date", //
                "campaign_id", //
                "lead_score", //
                "registration", //
                "city", //
                "birth", //
                "nbCommands", //
                "id", //
                "first_name", //
                "last_name", //
                "email", //
                "job_title", //
                "company", //
                "city", //
                "state", //
                "country", //
                "date", //
                "campaign_id", //
                "lead_score", //
                "registration", //
                "city", //
                "birth", //
                "nbCommands", //
                "id", //
                "first_name", //
                "last_name", //
                "email", //
                "job_title", //
                "company", //
                "city", //
                "state", //
                "country", //
                "date", //
                "campaign_id", //
                "lead_score", //
                "registration", //
                "city", //
                "birth", //
                "nbCommands", //
                "id", //
                "first_name", //
                "last_name", //
                "email", //
                "job_title", //
                "company", //
                "city", //
                "state", //
                "country", //
                "date", //
                "campaign_id", //
                "lead_score", //
                "registration", //
                "city", //
                "birth", //
                "nbCommands", //
                "id", //
                "first_name", //
                "last_name", //
                "email"}; //
            checkColumnsName(inputStream, cols);
        }

    }
}
