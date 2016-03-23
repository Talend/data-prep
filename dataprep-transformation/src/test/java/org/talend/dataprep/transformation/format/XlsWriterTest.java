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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.xls.XlsUtils;
import org.talend.dataprep.transformation.api.transformer.Transformer;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for the XlsWriter.
 * 
 * @see XlsWriter
 */
public class XlsWriterTest extends BaseFormatTest {

    @Autowired
    private TransformerFactory factory;

    @Test
    public void write_simple_xls_file() throws Exception {
        // given
        Path path = Files.createTempFile("datarep-foo", "xlsx");
        Files.deleteIfExists(path);
        try (final OutputStream outputStream = Files.newOutputStream(path)) {
            final Configuration configuration = Configuration.builder() //
                    .format(XlsFormat.XLSX) //
                    .output(outputStream) //
                    .actions("") //
                    .build();
            final Transformer exporter = factory.get(configuration);
            final InputStream inputStream = XlsWriterTest.class.getResourceAsStream("export_dataset.json");
            final ObjectMapper mapper = builder.build();
            try (JsonParser parser = mapper.getFactory().createParser(inputStream)) {
                final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
                exporter.transform(dataSet, configuration);
            }
        }
        DataSetMetadata metadata = metadataBuilder.metadata().id("123").build();
        SchemaParser.Request request = new SchemaParser.Request(Files.newInputStream(path), metadata);
        Workbook workbook = WorkbookFactory.create( request.getContent() );
        assertThat(workbook).isNotNull();
        assertThat(workbook.getNumberOfSheets()).isEqualTo(1);

        Sheet sheet = workbook.getSheetAt(0);
        assertThat(sheet).isNotNull().isNotEmpty();
        assertThat(sheet.getFirstRowNum()).isEqualTo(0);
        assertThat(sheet.getLastRowNum()).isEqualTo(6);

        // assert header content
        Row row = sheet.getRow(0);
        /*
         * "columns": [ { "id": "id", "type": "string" }, { "id": "firstname", "type": "string" }, { "id": "lastname",
         * "type": "string" }, { "id": "age", "type": "integer" }, { "id": "date-of-birth", "type": "date" }, { "id":
         * "alive", "type": "boolean" }, { "id": "city", "type": "string" } ]
         */
        assertThat(row.getCell(0).getRichStringCellValue().getString()).isEqualTo("id");
        assertThat(row.getCell(1).getRichStringCellValue().getString()).isEqualTo("firstname");
        assertThat(row.getCell(2).getRichStringCellValue().getString()).isEqualTo("lastname");
        assertThat(row.getCell(3).getRichStringCellValue().getString()).isEqualTo("age");
        assertThat(row.getCell(4).getRichStringCellValue().getString()).isEqualTo("date-of-birth");
        assertThat(row.getCell(5).getRichStringCellValue().getString()).isEqualTo("alive");
        assertThat(row.getCell(6).getRichStringCellValue().getString()).isEqualTo("city");

        // assert first content
        row = sheet.getRow(1);
        /*
         * { "id" : "1", "firstname" : "Clark", "lastname" : "Kent", "age" : "42", "date-of-birth" : "10/09/1940",
         * "alive" : "false", "city" : "Smallville" }
         */

        assertThat(row.getCell(0).getNumericCellValue()).isEqualTo(1);
        assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Clark");
        assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Kent");
        assertThat(row.getCell(3).getNumericCellValue()).isEqualTo((double) 42);
        assertThat(row.getCell(4).getStringCellValue()).isEqualTo("10/09/1940");
        assertThat(row.getCell(5).getBooleanCellValue()).isFalse();
        assertThat(row.getCell(6).getStringCellValue()).isEqualTo("Smallville");

        // assert last content
        row = sheet.getRow(sheet.getLastRowNum());
        /*
         * { "id" : "6", "firstname" : "Ray", "lastname" : "Palmer", "age" : "93", "date-of-birth" : "01/05/1951",
         * "alive" : "true", "city" : "Star city" }
         */
        assertThat(row.getCell(0).getNumericCellValue()).isEqualTo(6);
        assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Ray");
        assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Palmer");
        assertThat(row.getCell(3).getNumericCellValue()).isEqualTo((double) 93);
        assertThat(row.getCell(4).getStringCellValue()).isEqualTo("01/05/1951");
        assertThat(row.getCell(5).getBooleanCellValue()).isTrue();
        assertThat(row.getCell(6).getStringCellValue()).isEqualTo("Star city");
    }


    /**
     * Please have a look at <a href="https://jira.talendforge.org/browse/TDP-1528">TDP-1528</a>.
     * @throws Exception
     */
    @Test
    public void TDP_1528_export_of_backslash() throws Exception {
        // given
        Path path = Files.createTempFile("datarep-foo", "xlsx");
        Files.deleteIfExists(path);
        try (final OutputStream outputStream = Files.newOutputStream(path)) {
            final Configuration configuration = Configuration.builder() //
                    .format(XlsFormat.XLSX) //
                    .output(outputStream) //
                    .actions("") //
                    .build();
            final Transformer exporter = factory.get(configuration);
            final InputStream inputStream = XlsWriterTest.class.getResourceAsStream("tdp_1528_backslash_not_exported.json");
            final ObjectMapper mapper = builder.build();
            try (JsonParser parser = mapper.getFactory().createParser(inputStream)) {
                final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
                exporter.transform(dataSet, configuration);
            }
        }
        DataSetMetadata metadata = metadataBuilder.metadata().id("1528").build();
        SchemaParser.Request request = new SchemaParser.Request(Files.newInputStream(path), metadata);
        Workbook workbook = WorkbookFactory.create( request.getContent() );
        assertThat(workbook).isNotNull();
        assertThat(workbook.getNumberOfSheets()).isEqualTo(1);

        Sheet sheet = workbook.getSheetAt(0);
        assertThat(sheet).isNotNull().isNotEmpty();
        assertThat(sheet.getFirstRowNum()).isEqualTo(0);
        assertThat(sheet.getLastRowNum()).isEqualTo(2);

        // assert header content
        Row row = sheet.getRow(0);
        /*
         * [ {"id": "0", "name": "column1", "type": "string"}, {"id": "1", "name": "column2", "type": "string"},
         * {"id": "2", "name": "column2", "type": "string"} ]
         */
        assertThat(row.getCell(0).getRichStringCellValue().getString()).isEqualTo("column1");
        assertThat(row.getCell(1).getRichStringCellValue().getString()).isEqualTo("column2");
        assertThat(row.getCell(2).getRichStringCellValue().getString()).isEqualTo("column3");

        // assert first content
        row = sheet.getRow(1);
        /*
         * { "0": "BEAUTIFUL ITEM DESC W\BAG", "1": "Hello", "2": "Yo" }
         */

        assertThat(row.getCell(0).getStringCellValue()).isEqualTo("BEAUTIFUL ITEM DESC W\\BAG");
        assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Hello");
        assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Yo");

        // assert last content
        row = sheet.getRow(sheet.getLastRowNum());
        /*
         * { "0": "Konishiwa", "1": "Na nga def", "2": "Hola" }
         */
        assertThat(row.getCell(0).getStringCellValue()).isEqualTo("Konishiwa");
        assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Na nga def");
        assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Hola");
    }



}
