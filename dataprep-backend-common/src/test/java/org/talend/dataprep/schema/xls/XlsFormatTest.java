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

import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.InputStream;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.FormatGuess;
import org.talend.dataprep.schema.FormatGuesser;
import org.talend.dataprep.schema.SchemaParserResult;
import org.talend.dataprep.schema.unsupported.UnsupportedFormatGuess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

public class XlsFormatTest extends AbstractSchemaTestUtils {

    private final static Logger logger = LoggerFactory.getLogger(XlsFormatTest.class);

    @Autowired
    private DataSetMetadataBuilder metadataBuilder;

    @Qualifier("formatGuesser#xls")
    @Autowired
    private FormatGuesser formatGuesser;

    @Test
    public void read_bad_xls_file() throws Exception {
        try (InputStream inputStream = this.getClass().getResourceAsStream("fake.xls")) {
            FormatGuess formatGuess = formatGuesser.guess(getRequest(inputStream, "#1"), "UTF-8").getFormatGuess();
            Assert.assertNotNull(formatGuess);
            Assert.assertTrue(formatGuess instanceof UnsupportedFormatGuess);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void read_null_xls_file() throws Exception {
        formatGuesser.guess(null, "UTF-8").getFormatGuess();
    }

    protected List<Map<String, String>> getValuesFromFile(String fileName, FormatGuess formatGuess,
            DataSetMetadata dataSetMetadata) throws Exception {

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            InputStream jsonStream = formatGuess.getSerializer().serialize(inputStream, dataSetMetadata);

            String json = IOUtils.toString(jsonStream);

            logger.debug("json: {}", json);

            ObjectMapper mapper = new ObjectMapper();

            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, TreeMap.class);

            List<Map<String, String>> values = mapper.readValue(json, collectionType);

            logger.debug("values: {}", values);

            return values;
        }

    }

    @Test
    public void read_xls_file() throws Exception {

        String fileName = "test.xls";

        FormatGuess formatGuess;

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            formatGuess = formatGuesser.guess(getRequest(inputStream, "#2"), "UTF-8").getFormatGuess();
            Assert.assertNotNull(formatGuess);
            Assert.assertTrue(formatGuess instanceof XlsFormatGuess);
            Assert.assertEquals(XlsFormatGuess.MEDIA_TYPE, formatGuess.getMediaType());
        }

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            List<ColumnMetadata> columnMetadatas = formatGuess.getSchemaParser().parse(getRequest(inputStream, "#456"))
                    .getSheetContents().get(0).getColumnMetadatas();

            logger.debug("columnMetadatas: {}", columnMetadatas);
            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(4);

            ColumnMetadata columnMetadataFound = columnMetadatas.stream()
                    .filter(columnMetadata -> StringUtils.equals(columnMetadata.getName(), "country")).findFirst().get();

            logger.debug("columnMetadataFound: {}", columnMetadataFound);

            Assertions.assertThat(columnMetadataFound.getType()).isEqualTo(Type.STRING.getName());

            columnMetadataFound = columnMetadatas.stream()
                    .filter(columnMetadata -> StringUtils.equals(columnMetadata.getName(), "note")).findFirst().get();

            logger.debug("columnMetadataFound: {}", columnMetadataFound);

            Assertions.assertThat(columnMetadataFound.getType()).isEqualTo(Type.NUMERIC.getName());

        }

    }

    @Test
    public void read_xls_TDP_143() throws Exception {

        String fileName = "state_table.xls";

        FormatGuess formatGuess;

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            formatGuess = formatGuesser.guess(getRequest(inputStream, "#3"), "UTF-8").getFormatGuess();
            Assert.assertNotNull(formatGuess);
            Assert.assertTrue(formatGuess instanceof XlsFormatGuess);
            Assert.assertEquals(XlsFormatGuess.MEDIA_TYPE, formatGuess.getMediaType());
        }

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            List<ColumnMetadata> columnMetadatas = formatGuess.getSchemaParser().parse(getRequest(inputStream, "#852"))
                    .getSheetContents().get(0).getColumnMetadatas();
            logger.debug("columnMetadatas: {}", columnMetadatas);
            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(17);
        }

    }

    @Test
    public void read_xls_file_then_serialize() throws Exception {

        String fileName = "test.xls";

        FormatGuess formatGuess;

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").build();

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            formatGuess = formatGuesser.guess(getRequest(inputStream, "#4"), "UTF-8").getFormatGuess();
        }

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            List<ColumnMetadata> columnMetadatas = formatGuess.getSchemaParser() //
                    .parse(getRequest(inputStream, "#123")) //
                    .getSheetContents().get(0).getColumnMetadatas();

            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);

        }

        List<Map<String, String>> values = getValuesFromFile(fileName, formatGuess, dataSetMetadata);

        logger.debug("values: {}", values);

        // expected*
        // {country=Australie, note=10.0, beer name =Little Creatures, quality=Awesome}
        // {country=France , note=, beer name =Heinekein, quality=crappy}
        // {country=Australie, note=6.0, beer name =Foo, quality=10.0}
        // {country=France , note=2.0, beer name =Bar, quality=crappy}

        Assertions.assertThat(values).isNotEmpty().hasSize(4);

        Assertions.assertThat(values.get(0)) //
                .contains(MapEntry.entry("0000", "Little Creatures"), //
                        MapEntry.entry("0001", "Australie"), //
                        MapEntry.entry("0002", "Awesome"), //
                        MapEntry.entry("0003", "10")); //

        Assertions.assertThat(values.get(1)) //
                .contains(MapEntry.entry("0000", "Heinekein"), //
                        MapEntry.entry("0001", "France"), //
                        MapEntry.entry("0002", "crappy"), //
                        MapEntry.entry("0003", "")); //

        Assertions.assertThat(values.get(2)) //
                .contains(MapEntry.entry("0000", "Foo"), //
                        MapEntry.entry("0001", "Australie"), //
                        MapEntry.entry("0002", "10"), //
                        MapEntry.entry("0003", "6"));

        Assertions.assertThat(values.get(3)) //
                .contains(MapEntry.entry("0000", "Bar"), //
                        MapEntry.entry("0001", "France"), //
                        MapEntry.entry("0002", "crappy"), //
                        MapEntry.entry("0003", "2"));

    }

    @Test
    public void read_xls_cinema_then_serialize() throws Exception {

        String fileName = "EXPLOITATION-ListeEtabActifs_Adresse2012.xlsx";

        FormatGuess formatGuess;

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").build();

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            formatGuess = formatGuesser.guess(getRequest(inputStream, "#5"), "UTF-8").getFormatGuess();
            Assert.assertNotNull(formatGuess);
            Assert.assertTrue(formatGuess instanceof XlsFormatGuess);
            Assert.assertEquals(XlsFormatGuess.MEDIA_TYPE, formatGuess.getMediaType());
        }

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            List<ColumnMetadata> columnMetadatas = formatGuess.getSchemaParser() //
                    .parse(getRequest(inputStream, "#7563")) //
                    .getSheetContents().get(0).getColumnMetadatas();

            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);

            logger.debug("columnMetadatas: {}", columnMetadatas);
            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(8);

            ColumnMetadata columnMetadata = columnMetadatas.get(2);

            Assertions.assertThat(columnMetadata.getType()).isEqualTo(Type.NUMERIC.getName());

            Assertions.assertThat(columnMetadata.getHeaderSize()).isEqualTo(1);

            Assertions.assertThat(columnMetadata.getName()).isEqualTo("NoAuto");

        }

        List<Map<String, String>> values = getValuesFromFile(fileName, formatGuess, dataSetMetadata);

        logger.debug("values: {}", values);

        Assertions.assertThat(values.get(0)) //
                .contains(MapEntry.entry("0000", "ILE-DE-FRANCE"), //
                        MapEntry.entry("0001", "PARIS 8ME"), //
                        MapEntry.entry("0002", "12"), //
                        MapEntry.entry("0003", "GEORGE V"));

        Assertions.assertThat(values.get(3)) //
                .contains(MapEntry.entry("0000", "ILE-DE-FRANCE"), //
                        MapEntry.entry("0001", "PARIS 8ME"), //
                        MapEntry.entry("0002", "52"), //
                        MapEntry.entry("0003", "GAUMONT CHAMPS ELYSEES AMBASSADE"));

    }

    @Test
    public void read_xls_musee_then_serialize() throws Exception {

        String fileName = "liste-musees-de-france-2012.xls";

        FormatGuess formatGuess;

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").build();

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            formatGuess = formatGuesser.guess(getRequest(inputStream, "#6"), "UTF-8").getFormatGuess();
            Assert.assertNotNull(formatGuess);
            Assert.assertTrue(formatGuess instanceof XlsFormatGuess);
            Assert.assertEquals(XlsFormatGuess.MEDIA_TYPE, formatGuess.getMediaType());
        }

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            List<ColumnMetadata> columnMetadatas = formatGuess.getSchemaParser().parse(getRequest(inputStream, "#951"))
                    .getSheetContents().get(0).getColumnMetadatas();

            logger.debug("columnMetadatas: {}", columnMetadatas);
            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(13);

            ColumnMetadata columnMetadata = columnMetadatas.get(7);

            Assertions.assertThat(columnMetadata.getHeaderSize()).isEqualTo(1);

            Assertions.assertThat(columnMetadata.getName()).isEqualTo("CP");

            Assertions.assertThat(columnMetadata.getType()).isEqualTo(Type.STRING.getName());

            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);

        }

        List<Map<String, String>> values = getValuesFromFile(fileName, formatGuess, dataSetMetadata);

        logger.debug("values: {}", values);

        Assertions.assertThat(values.get(0)) //
                .contains(MapEntry.entry("0000", "ALSACE"), //
                        MapEntry.entry("0001", "BAS-RHIN"), //
                        MapEntry.entry("0002", "NON"));

        Assertions.assertThat(values.get(2)) //
                .contains(MapEntry.entry("0000", "ALSACE"), //
                        MapEntry.entry("0001", "BAS-RHIN"), //
                        MapEntry.entry("0002", "OUI"), //
                        MapEntry.entry("0003", "29 juin 2013"));

    }

    @Test
    public void test_second_sheet_parsing() throws Exception {
        String fileName = "Talend_Desk-Tableau-Bord-011214.xls";

        FormatGuess formatGuess;

        XlsSchemaParser xlsSchemaParser = new XlsSchemaParser();

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            formatGuess = formatGuesser.guess(getRequest(inputStream, "#7"), "UTF-8").getFormatGuess();
            Assert.assertNotNull(formatGuess);
            Assert.assertTrue(formatGuess instanceof XlsFormatGuess);
            Assert.assertEquals(XlsFormatGuess.MEDIA_TYPE, formatGuess.getMediaType());
        }

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").sheetName("sheet-1").build();

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            List<SchemaParserResult.SheetContent> sheetContents = xlsSchemaParser.parseAllSheets(getRequest(inputStream, "#8"));

            List<ColumnMetadata> columnMetadatas = sheetContents.stream()
                    .filter(sheetContent -> "Leads".equals(sheetContent.getName())).findFirst().get().getColumnMetadatas();

            logger.debug("columnMetadatas: {}", columnMetadatas);

            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(21);

            ColumnMetadata columnMetadata = columnMetadatas.get(7);
            Assertions.assertThat(columnMetadata.getHeaderSize()).isEqualTo(1);
            Assertions.assertThat(columnMetadata.getName()).isEqualTo("telephone");
            Assertions.assertThat(columnMetadata.getType()).isEqualTo(Type.NUMERIC.getName());
            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);
        }

        List<Map<String, String>> values = getValuesFromFile(fileName, formatGuess, dataSetMetadata);

        logger.trace("values: {}", values);

        Assertions.assertThat(values).isNotEmpty().hasSize(239);

        Assertions.assertThat(values.get(0)) //
                .contains(MapEntry.entry("0000", "24-Jul-2014"), //
                        MapEntry.entry("0001", "COFACE"), //
                        MapEntry.entry("0006", "tony_fernandes@coface.com"));

        Assertions.assertThat(values.get(1)) //
                .contains(MapEntry.entry("0000", "24-Jul-2014"), //
                        MapEntry.entry("0001", "ENABLON"), //
                        MapEntry.entry("0004", "COCUD"));

        Assertions.assertThat(values.get(17)) //
                .contains(MapEntry.entry("0000", "17-Jul-2014"), //
                        MapEntry.entry("0001", "SODEBO"), //
                        MapEntry.entry("0003", "Tanguy"));

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
        FormatGuess formatGuess;
        try (InputStream inputStream = this.getClass().getResourceAsStream("excel_numbers.xls")) {
            formatGuess = formatGuesser.guess(getRequest(inputStream, "#9"), "UTF-8").getFormatGuess();
            Assert.assertNotNull(formatGuess);
            Assert.assertTrue(formatGuess instanceof XlsFormatGuess);
            Assert.assertEquals(XlsFormatGuess.MEDIA_TYPE, formatGuess.getMediaType());
        }
        // Test number serialization in XLS type guess
        InputStream input = this.getClass().getResourceAsStream("excel_numbers.xls");
        final String result = IOUtils.toString(formatGuess.getSerializer().serialize(input, metadata));
        final String expected = "[{\"0000\":\"1\",\"0001\":\"123\"},{\"0000\":\"2\",\"0001\":\"123.1\"},{\"0000\":\"3\",\"0001\":\"209.9\"}]";
        assertThat(result, sameJSONAs(expected));
    }

    @Test
    public void read_xls_TDP_332() throws Exception {

        String fileName = "customersDate.xls";

        FormatGuess formatGuess;

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            formatGuess = formatGuesser.guess(getRequest(inputStream, "#10"), "UTF-8").getFormatGuess();
            Assert.assertNotNull(formatGuess);
            Assert.assertTrue(formatGuess instanceof XlsFormatGuess);
            Assert.assertEquals(XlsFormatGuess.MEDIA_TYPE, formatGuess.getMediaType());
        }

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            List<ColumnMetadata> columnMetadatas = formatGuess.getSchemaParser().parse(getRequest(inputStream, "#0267"))
                    .getSheetContents().get(0).getColumnMetadatas();
            logger.debug("columnMetadatas: {}", columnMetadatas);
            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(10);

            ColumnMetadata columnMetadataDate = columnMetadatas.stream() //
                    .filter(columnMetadata -> columnMetadata.getName().equalsIgnoreCase("date")) //
                    .findFirst().get();

            Assertions.assertThat(columnMetadataDate.getType()).isEqualTo("date");

        }

    }

    @Test
    public void read_xls_that_can_be_parsed_as_csv_TDP_375() throws Exception {

        String fileName = "TDP-375_xsl_read_as_csv.xls";

        FormatGuess formatGuess;

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            formatGuess = formatGuesser.guess(getRequest(inputStream, "#11"), "UTF-8").getFormatGuess();
            Assert.assertNotNull(formatGuess);
            Assert.assertTrue(formatGuess instanceof XlsFormatGuess);
            Assert.assertEquals(XlsFormatGuess.MEDIA_TYPE, formatGuess.getMediaType());
        }

    }

    @Test
    public void read_evaluate_formulas() throws Exception {

        String fileName = "000_DTA_DailyTimeLog.xlsm";

        FormatGuess formatGuess;

        XlsSchemaParser xlsSchemaParser = new XlsSchemaParser();

        String sheetName = "WEEK SUMMARY";

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {
            formatGuess = formatGuesser.guess(getRequest(inputStream, UUID.randomUUID().toString()), "UTF-8").getFormatGuess();
            Assert.assertNotNull(formatGuess);
            Assert.assertTrue(formatGuess instanceof XlsFormatGuess);
            Assert.assertEquals(XlsFormatGuess.MEDIA_TYPE, formatGuess.getMediaType());
        }

        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id("beer").sheetName(sheetName).build();

        try (InputStream inputStream = this.getClass().getResourceAsStream(fileName)) {

            List<SchemaParserResult.SheetContent> sheetContents = xlsSchemaParser.parseAllSheets(getRequest(inputStream, "#8"));

            List<ColumnMetadata> columnMetadatas = sheetContents.stream()
                    .filter(sheetContent -> sheetName.equals(sheetContent.getName())).findFirst().get().getColumnMetadatas();

            logger.debug("columnMetadatas: {}", columnMetadatas);

            Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(32);

            dataSetMetadata.getRowMetadata().setColumns(columnMetadatas);
        }

        List<Map<String, String>> values = getValuesFromFile(fileName, formatGuess, dataSetMetadata);

        logger.debug("values: {}", values);

        Assertions.assertThat(values.get(4).get("0003")).isNotEmpty().isEqualTo("26-Oct-2015");

        Assertions.assertThat(values.get(5).get("0003")).isNotEmpty().isEqualTo("MONDAY");

        Assertions.assertThat(values.get(7).get("0003")).isNotEmpty().isEqualTo("8.0");

        Assertions.assertThat(values.get(30).get("0003")).isNotEmpty().isEqualTo("6.0");

        Assertions.assertThat(values.get(31).get("0003")).isNotEmpty().isEqualTo("18.5");

    }

}
