// ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.transformation.api.transformer.AbstractTransformerWriterTest;
import org.talend.dataprep.transformation.api.transformer.Transformer;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;

import com.fasterxml.jackson.core.JsonParser;

/**
 * Unit test for the XlsWriter.
 *
 * @see XlsWriter
 */
public class XlsWriterTest extends AbstractTransformerWriterTest {

    @Autowired
    private TransformerFactory factory;

    /**
     * Where the writer should... write!
     */
    private OutputStream outputStream;

    @Before
    public void init() {
        outputStream = new ByteArrayOutputStream();
        Map<String, Object> parameters = new HashMap<>();
        writer = (XlsWriter) context.getBean("writer#XLSX", outputStream, parameters);
    }

    @Test
    public void write_simple_xls_file() throws Exception {
        // given
        SchemaParser.Request request = createSchemaParser("export_dataset.json");

        Workbook workbook = WorkbookFactory.create(request.getContent());
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
     */
    @Test
    public void TDP_1528_export_of_backslash() throws Exception {

        // given
        SchemaParser.Request request = createSchemaParser("tdp_1528_backslash_not_exported.json");

        Workbook workbook = WorkbookFactory.create(request.getContent());
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

    /**
     * Please have a look at <a href="https://jira.talendforge.org/browse/TDP-4571">TDP-4571</a>.
     */
    @Test
    public void export_bugfix() throws Exception {
        // given
        SchemaParser.Request request = createSchemaParser("export_bug_fix_xlsx.json");

        Workbook workbook = WorkbookFactory.create(request.getContent());
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
         * "alive", "type": "boolean" }, { "id": "city", "type": "string" }, { "id": "7", "type": "float" } ]
         */
        assertThat(row.getCell(0).getRichStringCellValue().getString()).isEqualTo("id");
        assertThat(row.getCell(1).getRichStringCellValue().getString()).isEqualTo("firstname");
        assertThat(row.getCell(2).getRichStringCellValue().getString()).isEqualTo("lastname");
        assertThat(row.getCell(3).getRichStringCellValue().getString()).isEqualTo("age");
        assertThat(row.getCell(4).getRichStringCellValue().getString()).isEqualTo("date-of-birth");
        assertThat(row.getCell(5).getRichStringCellValue().getString()).isEqualTo("alive");
        assertThat(row.getCell(6).getRichStringCellValue().getString()).isEqualTo("city");
        assertThat(row.getCell(7).getRichStringCellValue().getString()).isEqualTo("phone-number");

        // assert first content
        row = sheet.getRow(1);
        /*
         * { "id" : "1", "firstname" : "Clark", "lastname" : "Kent", "age" : "42", "date-of-birth" : "10/09/1940",
         * "alive" : "false", "city" : "", "phone-number" : "" }
         */

        assertRowValues(row, 1, "Clark", "Kent", 42, "10/09/1940", //
                false, "Smallville", "");

        // assert second row content
        row = sheet.getRow(2);
        /*
         * { "id" : "2", "firstname" : "Bruce", "lastname" : "Wayne", "age" : "50", "date-of-birth" : "01/01/1947",
         * "alive" : "true", "city" : "Gotham city", "phone-number" : "null" }
         */
        assertRowValues(row, 2, "Bruce", "Wayne", 50, "01/01/1947", //
                true, "Gotham city", "null");

        // assert third row content
        row = sheet.getRow(3);
        /*
         * { "id" : "3", "firstname" : "Barry", "lastname" : "Allen", "age" : "67", "date-of-birth" : "01/02/1948",
         * "alive" : "true", "city" : "Central city", "phone-number" : "+33 6 89 46 55 34" }
         */
        assertRowValues(row, 3, "Barry", "Allen", 67, "01/02/1948", //
                true, "Central city", "+33 6 89 46 55 34");

        // assert last content
        row = sheet.getRow(sheet.getLastRowNum());
        /*
         * { "id" : "6", "firstname" : "Ray", "lastname" : "Palmer", "age" : "93", "date-of-birth" : "01/05/1951",
         * "alive" : "true", "city" : "Star city" }
         */
        assertRowValues(row, 6, "Ray", "Palmer", 93, "01/05/1951", //
                true, "Star city", "+33-6-89-46-55-34");
    }

    /**
     * utility function
     */
    public SchemaParser.Request createSchemaParser(String inputFileName) throws Exception {
        Path path = Files.createTempFile("datarep-foo", "xlsx");
        Files.deleteIfExists(path);
        try (final OutputStream outputStream = Files.newOutputStream(path)) {
            final Configuration configuration = Configuration.builder() //
                    .format(XlsFormat.XLSX) //
                    .output(outputStream) //
                    .actions("") //
                    .build();
            final Transformer exporter = factory.get(configuration);
            final InputStream inputStream = XlsWriterTest.class.getResourceAsStream(inputFileName);
            try (JsonParser parser = mapper.getFactory().createParser(inputStream)) {
                final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
                exporter.buildExecutable(dataSet, configuration).execute();
            }
        }
        DataSetMetadata metadata = metadataBuilder.metadata().id("123").build();
        return new SchemaParser.Request(Files.newInputStream(path), metadata);
    }

    /**
     * utility function
     */
    public void assertHeadValues(Row row, String[] outputTabValues) {
        for (int i = 0; i < outputTabValues.length; i++) {
            assertThat(row.getCell(i).getRichStringCellValue().getString()).isEqualTo(outputTabValues[i]);
        }
    }

    /**
     * utility function
     */
    public void assertRowValues(Row row, int idRow, String firstname, String lastname, int age, String date, Boolean alive,
                                String city, String phone) {
        assertThat(row.getCell(0).getNumericCellValue()).isEqualTo(idRow);
        assertThat(row.getCell(1).getStringCellValue()).isEqualTo(firstname);
        assertThat(row.getCell(2).getStringCellValue()).isEqualTo(lastname);
        assertThat(row.getCell(3).getNumericCellValue()).isEqualTo((double) age);
        assertThat(row.getCell(4).getStringCellValue()).isEqualTo(date);
        assertThat(row.getCell(5).getBooleanCellValue()).isEqualTo(alive);
        assertThat(row.getCell(6).getStringCellValue()).isEqualTo(city);
        assertThat(row.getCell(7).getStringCellValue()).isEqualTo(phone);
    }
}
