package org.talend.dataprep.transformation.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.io.XlsUtils;
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
        Path path = Files.createTempFile("datarep-foo", "xls");
        Files.deleteIfExists(path);
        try (final OutputStream outputStream = Files.newOutputStream(path)) {
            final Configuration configuration = Configuration.builder() //
                    .format(XlsFormat.XLS) //
                    .output(outputStream) //
                    .actions("") //
                    .build();
            final Transformer exporter = factory.get(configuration);
            final InputStream inputStream = XlsWriterTest.class.getResourceAsStream("export_dataset.json");
            final ObjectMapper mapper = builder.build();
            try (JsonParser parser = mapper.getFactory().createParser(inputStream)) {
                final DataSet dataSet = mapper.reader(DataSet.class).readValue(parser);
                exporter.transform(dataSet, configuration);
            }
        }
        DataSetMetadata metadata = DataSetMetadata.Builder.metadata().id("123").build();
        SchemaParser.Request request = new SchemaParser.Request(Files.newInputStream(path), metadata);
        Workbook workbook = XlsUtils.getWorkbook(request);
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

        assertThat(row.getCell(0).getStringCellValue()).isEqualTo("1");
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
        assertThat(row.getCell(0).getStringCellValue()).isEqualTo("6");
        assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Ray");
        assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Palmer");
        assertThat(row.getCell(3).getNumericCellValue()).isEqualTo((double) 93);
        assertThat(row.getCell(4).getStringCellValue()).isEqualTo("01/05/1951");
        assertThat(row.getCell(5).getBooleanCellValue()).isTrue();
        assertThat(row.getCell(6).getStringCellValue()).isEqualTo("Star city");
    }

}
