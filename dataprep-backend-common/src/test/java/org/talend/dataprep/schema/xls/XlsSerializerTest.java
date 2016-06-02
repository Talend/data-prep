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

package org.talend.dataprep.schema.xls;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.data.MapEntry.entry;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.CompositeFormatDetector;
import org.talend.dataprep.schema.Format;
import org.talend.dataprep.schema.Schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

public class XlsSerializerTest extends AbstractSchemaTestUtils {

    private final static Logger logger = LoggerFactory.getLogger(XlsSerializerTest.class);

    @Autowired
    private DataSetMetadataBuilder metadataBuilder;

    /** The format guesser to test. */
    @Autowired
    CompositeFormatDetector formatDetector;

    @Autowired
    private XlsSchemaParser xlsSchemaParser;

    @Autowired
    private XlsSerializer xlsSerializer;

    @Autowired
    private ObjectMapper mapper;

    private Locale previousLocale;

    @Before
    public void setUp() throws Exception {
        previousLocale = Locale.getDefault();
        Locale.setDefault(ENGLISH);
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(previousLocale);
    }

    private List<Map<String, String>> getValuesFromFile(String fileName, DataSetMetadata dataSetMetadata) throws Exception {

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            return getValuesFromInputStream( inputStream, dataSetMetadata );
        }
    }

    private List<Map<String, String>> getValuesFromInputStream(InputStream inputStream, DataSetMetadata dataSetMetadata) throws Exception {

        InputStream jsonStream = xlsSerializer.serialize(inputStream, dataSetMetadata);
        //String json = IOUtils.toString(jsonStream);

        //logger.debug("json: {}", json);

        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, TreeMap.class);
        List<Map<String, String>> values = mapper.readValue(jsonStream, collectionType);
        logger.debug("values: {}", values);

        return values;

    }

    private Format assertFormat(String fileName) throws Exception {
        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            return assertFormat( inputStream );
        }

    }

    private Format assertFormat(InputStream inputStream) throws Exception {
        Format format = formatDetector.detect(inputStream);
        Assert.assertNotNull(format);
        Assert.assertTrue(format.getFormatFamily() instanceof XlsFormatFamily);
        Assert.assertEquals(XlsFormatFamily.MEDIA_TYPE, format.getFormatFamily().getMediaType());

        return format;
    }

    public void assert_on_simple_file(String fileName, boolean newFormat) throws Exception {

        assertFormat(fileName);

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            List<ColumnMetadata> columnMetadatas = xlsSchemaParser.parse(getRequest(inputStream, "#456")).getSheetContents()
                    .get(0).getColumnMetadatas();

            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(4);

            ColumnMetadata columnMetadataFound = columnMetadatas.stream()
                    .filter(columnMetadata -> StringUtils.equals(columnMetadata.getName(), "country")).findFirst().get();

            Assertions.assertThat(columnMetadataFound.getType()).isEqualTo(Type.STRING.getName());

            columnMetadataFound = columnMetadatas.stream()
                    .filter(columnMetadata -> StringUtils.equals(columnMetadata.getName(), "note")).findFirst().get();

            Assertions.assertThat(columnMetadataFound.getType())
                    .isEqualTo(newFormat ? Type.STRING.getName() : Type.NUMERIC.getName());

        }

    }

    @Test
    public void read_xls_TDP_143() throws Exception {

        String fileName = "state_table.xls";

        Format format;

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            format = formatDetector.detect(inputStream);
            Assert.assertNotNull(format);
            Assert.assertTrue(format.getFormatFamily() instanceof XlsFormatFamily);
            Assert.assertEquals(XlsFormatFamily.MEDIA_TYPE, format.getFormatFamily().getMediaType());
        }

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            List<ColumnMetadata> columnsMetadata = xlsSchemaParser.parse(getRequest(inputStream, "#852")).getSheetContents()
                    .get(0).getColumnMetadatas();
            logger.debug("columnsMetadata: {}", columnsMetadata);
            Assertions.assertThat(columnsMetadata).isNotNull().isNotEmpty().hasSize(17);
        }

    }

    @Test
    public void read_xls_file_then_serialize() throws Exception {

        String fileName = "test.xls";

        Format format;

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").build();

        assertFormat(fileName);

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            List<ColumnMetadata> columnMetadatas = xlsSchemaParser //
                    .parse(getRequest(inputStream, "#123")) //
                    .getSheetContents().get(0).getColumnMetadatas();

            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);

        }

        List<Map<String, String>> values = getValuesFromFile(fileName, dataSetMetadata);

        logger.debug("values: {}", values);

        // expected*
        // {country=Australie, note=10.0, beer name =Little Creatures, quality=Awesome}
        // {country=France , note=, beer name =Heinekein, quality=crappy}
        // {country=Australie, note=6.0, beer name =Foo, quality=10.0}
        // {country=France , note=2.0, beer name =Bar, quality=crappy}

        Assertions.assertThat(values).isNotEmpty().hasSize(4);

        Assertions.assertThat(values.get(0)) //
                .contains(entry("0000", "Little Creatures"), //
                        entry("0001", "Australie"), //
                        entry("0002", "Awesome"), //
                        entry("0003", "10")); //

        Assertions.assertThat(values.get(1)) //
                .contains(entry("0000", "Heinekein"), //
                        entry("0001", "France"), //
                        entry("0002", "crappy"), //
                        entry("0003", "")); //

        Assertions.assertThat(values.get(2)) //
                .contains(entry("0000", "Foo"), //
                        entry("0001", "Australie"), //
                        entry("0002", "10"), //
                        entry("0003", "6"));

        Assertions.assertThat(values.get(3)) //
                .contains(entry("0000", "Bar"), //
                        entry("0001", "France"), //
                        entry("0002", "crappy"), //
                        entry("0003", "2"));

    }

    @Test
    public void read_xls_cinema_then_serialize() throws Exception {

        String fileName = "EXPLOITATION-ListeEtabActifs_Adresse2012.xlsx";

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").build();

        assertFormat(fileName);

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            List<ColumnMetadata> columnMetadatas = xlsSchemaParser //
                    .parse(getRequest(inputStream, "#7563")) //
                    .getSheetContents().get(0).getColumnMetadatas();

            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);

            logger.debug("columnMetadatas: {}", columnMetadatas);
            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(8);

            ColumnMetadata columnMetadata = columnMetadatas.get(2);

            Assertions.assertThat(columnMetadata.getType()).isEqualTo(Type.STRING.getName());

            Assertions.assertThat(columnMetadata.getHeaderSize()).isEqualTo(1);

            Assertions.assertThat(columnMetadata.getName()).isEqualTo("NoAuto");

        }

        List<Map<String, String>> values = getValuesFromFile(fileName, dataSetMetadata);

        logger.debug("values: {}", values);

        Assertions.assertThat(values.get(0)) //
                .contains(entry("0000", "ILE-DE-FRANCE"), //
                        entry("0001", "PARIS 8ME"), //
                        entry("0002", "12"), //
                        entry("0003", "GEORGE V"));

        Assertions.assertThat(values.get(3)) //
                .contains(entry("0000", "ILE-DE-FRANCE"), //
                        entry("0001", "PARIS 8ME"), //
                        entry("0002", "52"), //
                        entry("0003", "GAUMONT CHAMPS ELYSEES AMBASSADE"));

    }

    @Test
    public void read_xls_musee_then_serialize() throws Exception {

        String fileName = "liste-musees-de-france-2012.xls";

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").build();

        assertFormat(fileName);

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            List<ColumnMetadata> columnMetadatas = xlsSchemaParser.parse(getRequest(inputStream, "#951")).getSheetContents()
                    .get(0).getColumnMetadatas();

            logger.debug("columnMetadatas: {}", columnMetadatas);
            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(13);

            ColumnMetadata columnMetadata = columnMetadatas.get(7);

            Assertions.assertThat(columnMetadata.getHeaderSize()).isEqualTo(1);

            Assertions.assertThat(columnMetadata.getName()).isEqualTo("CP");

            Assertions.assertThat(columnMetadata.getType()).isEqualTo(Type.STRING.getName());

            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);

        }

        List<Map<String, String>> values = getValuesFromFile(fileName, dataSetMetadata);

        logger.debug("values: {}", values);

        Assertions.assertThat(values.get(0)) //
                .contains(entry("0000", "ALSACE"), //
                        entry("0001", "BAS-RHIN"), //
                        entry("0002", "NON"));

        Assertions.assertThat(values.get(2)) //
                .contains(entry("0000", "ALSACE"), //
                        entry("0001", "BAS-RHIN"), //
                        entry("0002", "OUI"), //
                        entry("0003", "29 juin 2013"));

    }

    @Test
    public void test_second_sheet_parsing() throws Exception {
        String fileName = "Talend_Desk-Tableau-Bord-011214.xls";

        assertFormat(fileName);

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").sheetName("Leads").build();

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            List<Schema.SheetContent> sheetContents = xlsSchemaParser.parseAllSheets(getRequest(inputStream, "#8"));

            List<ColumnMetadata> columnsMetadata = sheetContents.stream()
                    .filter(sheetContent -> "Leads".equals(sheetContent.getName())).findFirst().get().getColumnMetadatas();

            logger.debug("columnsMetadata: {}", columnsMetadata);

            Assertions.assertThat(columnsMetadata).isNotNull().isNotEmpty().hasSize(6);

            ColumnMetadata columnMetadata = columnsMetadata.get(4);
            Assertions.assertThat(columnMetadata.getHeaderSize()).isEqualTo(1);
            Assertions.assertThat(columnMetadata.getName()).isEqualTo("age");
            Assertions.assertThat(columnMetadata.getType()).isEqualTo(Type.STRING.getName());
            dataSetMetadata.getRowMetadata().setColumns(columnsMetadata);
        }

        List<Map<String, String>> values = getValuesFromFile(fileName, dataSetMetadata);

        logger.trace("values: {}", values);

        Assertions.assertThat(values).isNotEmpty().hasSize(3);

        Assertions.assertThat(values.get(0)) //
                .contains(entry("0000", "301638"), //
                        entry("0001", "foo@foo.com"), //
                        entry("0004", "23"));

        Assertions.assertThat(values.get(1)) //
                .contains(entry("0000", "12349383"), //
                        entry("0001", "beer@gof.org"), //
                        entry("0004", "23"));

        Assertions.assertThat(values.get(2)) //
                .contains(entry("0000", "73801093"), //
                        entry("0001", "wine@go.com"), //
                        entry("0003", "Jean"));

    }

    /**
     * <p>
     * See <a href="https://jira.talendforge.org/browse/TDP-222">https://jira.talendforge.org/browse/TDP-222</a>.
     * </p>
     * <p>
     * XlsSerializer should follow the data format as set in the Excel file. This test ensures XlsSerializer follows the
     * data format as defined and don't directly use {@link Cell#getNumericCellValue()}.
     * </p>
     *
     */
    @Test
    public void testGeneralNumberFormat_TDP_222() throws Exception {
        final DataSetMetadata metadata = metadataBuilder.metadata().id("1234")
                .row(column().name("id").id(0).type(Type.INTEGER), column().name("value1").id(1).type(Type.INTEGER)).build();
        Format format = assertFormat("excel_numbers.xls");
        // Test number serialization in XLS type guess
        InputStream input = this.getClass().getResourceAsStream("excel_numbers.xls");
        final String result = IOUtils.toString(format.getFormatFamily().getSerializer().serialize(input, metadata));
        final String expected = "[{\"0000\":\"1\",\"0001\":\"123\"},{\"0000\":\"2\",\"0001\":\"123.1\"},{\"0000\":\"3\",\"0001\":\"209.9\"}]";
        assertThat(result, sameJSONAs(expected));
    }

    @Test
    public void read_xls_TDP_332() throws Exception {

        String fileName = "customersDate.xls";

        assertFormat(fileName);

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            List<ColumnMetadata> columnMetadatas = xlsSchemaParser.parse(getRequest(inputStream, "#0267")).getSheetContents()
                    .get(0).getColumnMetadatas();
            logger.debug("columnMetadatas: {}", columnMetadatas);
            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(10);

            ColumnMetadata columnMetadataDate = columnMetadatas.stream() //
                    .filter(columnMetadata -> columnMetadata.getName().equalsIgnoreCase("date")) //
                    .findFirst().get();

            Assertions.assertThat(columnMetadataDate.getType()).isEqualTo("date");

        }
    }

    @Test
    public void read_evaluate_formulas() throws Exception {
        String fileName = "000_DTA_DailyTimeLog.xlsm";
        String sheetName = "WEEK SUMMARY";

        checkExcelFile(fileName);

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").sheetName(sheetName).build();

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            List<Schema.SheetContent> sheetContents = xlsSchemaParser.parseAllSheets(getRequest(inputStream, "#8"));
            List<ColumnMetadata> columnMetadatas = sheetContents.stream()
                    .filter(sheetContent -> sheetName.equals(sheetContent.getName())).findFirst().get().getColumnMetadatas();
            logger.debug("columnMetadatas: {}", columnMetadatas);

            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(34);

            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);
        }

        List<Map<String, String>> values = getValuesFromFile(fileName, dataSetMetadata);
        logger.debug("values: {}", values);

        Assertions.assertThat(values.get(4).get("0003")).isNotEmpty().isEqualTo("10/26/2015");
        Assertions.assertThat(values.get(5).get("0003")).isNotEmpty().isEqualTo("MONDAY");
        Assertions.assertThat(values.get(7).get("0003")).isNotEmpty().isEqualTo("8.00");
        Assertions.assertThat(values.get(30).get("0003")).isNotEmpty().isEqualTo("6.00");
        Assertions.assertThat(values.get(31).get("0003")).isNotEmpty().isEqualTo("18.50");
    }

    @Test
    public void not_fail_on_bad_formulas() throws Exception {

        String fileName = "bad_formulas.xlsx";
        String sheetName = "Sheet1";

        checkExcelFile(fileName);

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").sheetName(sheetName).build();

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            List<Schema.SheetContent> sheetContents = xlsSchemaParser.parseAllSheets(getRequest(inputStream, "#8"));

            List<ColumnMetadata> columnMetadatas = sheetContents.stream()
                    .filter(sheetContent -> sheetName.equals(sheetContent.getName())).findFirst().get().getColumnMetadatas();

            logger.debug("columnMetadatas: {}", columnMetadatas);

            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(2);

            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);
        }

        List<Map<String, String>> values = getValuesFromFile(fileName, dataSetMetadata);

        logger.debug("values: {}", values);

        Assertions.assertThat(values.get(0)) //
                .contains(entry("0000", "Zoo"), //
                        entry("0001", ""));

        Assertions.assertThat(values.get(1)) //
                .contains(entry("0000", "Boo"), //
                        entry("0001", ""));
    }

    /**
     * test for TDP-1660 and TDP-1648
     */
    @Test
    public void more_than_52_columns() throws Exception {
        String fileName = "TDP_Epics.xlsx";
        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("epics").build();

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            dataSetMetadata.getRowMetadata() //
                    .setColumns(xlsSchemaParser.parse(getRequest(inputStream, "#456")) //
                            .getSheetContents() //
                            .get(0) //
                            .getColumnMetadatas());
        }

        Assertions.assertThat(dataSetMetadata.getRowMetadata().getColumns()) //
                .isNotNull() //
                .isNotEmpty() //
                .hasSize(98);

        List<Map<String, String>> values = getValuesFromFile(fileName, dataSetMetadata);

        logger.debug("values: {}", values);

        Assertions.assertThat(values.get(3).get("0090")).isEqualTo("Small");
    }

    /**
     * TDP-1656
     */
    @Test
    public void date_format() throws Exception {

        // given
        String fileName = "dates.xlsx";
        checkExcelFile(fileName);

        // when
        DataSetMetadata dataSetMetadata = getDataSetMetadataFromExcelFile(fileName, "Feuil1");
        List<Map<String, String>> values = getValuesFromFile(fileName, dataSetMetadata);

        logger.debug("values: {}", values);

        // then
        Assertions.assertThat(values.get(0)).contains(entry("0000", "1/1/2016"), entry("0001", "2/10/2016"));
        Assertions.assertThat(values.get(1)).contains(entry("0000", "3/31/1972"), entry("0001", "3/31/2016"));
        Assertions.assertThat(values.get(2)).contains(entry("0000", "10/10/2003"), entry("0001", "4/6/2016"));
        Assertions.assertThat(values.get(3)).contains(entry("0000", "02/03/1932"), entry("0001", "03/02/1956"));
        Assertions.assertThat(values.get(4)).contains(entry("0000", "04/05/2025"), entry("0001", "06/07/2029"));
    }

    /**
     * TDP-1656
     */
    @Test
    public void date_format_with_time() throws Exception {

        // given
        String fileName = "dates_test.xlsx";
        checkExcelFile(fileName);

        // when
        DataSetMetadata dataSetMetadata = getDataSetMetadataFromExcelFile(fileName, "Sheet2");
        List<Map<String, String>> values = getValuesFromFile(fileName, dataSetMetadata);

        logger.debug("values: {}", values);

        // then
        Assertions.assertThat(values.get(0)).contains(entry("0003", "1/26/2015 18:03"), entry("0004", "1/29/2015 0:30"));
        Assertions.assertThat(values.get(1)).contains(entry("0003", "12/4/2015 1:01"), entry("0004", "1/15/2016 13:56"));
    }

    /**
     * Return the dataset metadata out of the given file name.
     *
     * @param fileName the excel file to open.
     * @param sheetName name of the excel sheet.
     * @return the dataset metadata.
     * @throws IOException s**y happens.
     */
    private DataSetMetadata getDataSetMetadataFromExcelFile(String fileName, String sheetName) throws IOException {
        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("ff").sheetName(sheetName).build();

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            List<Schema.SheetContent> sheetContents = xlsSchemaParser.parseAllSheets(getRequest(inputStream, "#8"));

            List<ColumnMetadata> columnsMetadata = sheetContents.get(0).getColumnMetadatas();

            logger.debug("columnsMetadata: {}", columnsMetadata);

            Assertions.assertThat(columnsMetadata).isNotNull().isNotEmpty();

            dataSetMetadata.getRowMetadata().setColumns(columnsMetadata);
        }
        return dataSetMetadata;
    }

    /**
     * Make sure the given file name is recognized as an excel file.
     * 
     * @param fileName the excel file name to open.
     */
    private void checkExcelFile(String fileName) throws IOException {
        Format format;
        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            format = formatDetector.detect(inputStream);
            Assert.assertNotNull(format);
            Assert.assertTrue(format.getFormatFamily() instanceof XlsFormatFamily);
            Assert.assertEquals(XlsFormatFamily.MEDIA_TYPE, format.getFormatFamily().getMediaType());
        }
    }


    @Test
    public void read_huge_xls_file_then_serialize() throws Exception {

        String fileName = "veryhuge.xlsx";
        Path path = Paths.get( fileName);

        if (!Files.exists( path )){
            logger.info( "file {} not available so skip the test" );
            return;
        }

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").build();

        try (InputStream inputStream = Files.newInputStream(path)) {
            assertFormat( inputStream );
        }

        try (InputStream inputStream = Files.newInputStream(path)) {

            List<ColumnMetadata> columnMetadatas = xlsSchemaParser //
                .parse(getRequest(inputStream, "#123")) //
                .getSheetContents().get(0).getColumnMetadatas();

            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);

        }
        try (InputStream inputStream = Files.newInputStream(path)) {
            List<Map<String, String>> values = getValuesFromInputStream( inputStream, dataSetMetadata );
            logger.debug("values: {}", values);
            System.out.println( "values:" + values.size() + ";" + values.get( 0 ).size() );
        }

    }

}
