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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.SchemaParserResult;

/**
 * Unit test for the XLSSchemaParser class.
 * 
 * @see XlsSchemaParser
 */
public class XlsSchemaParserTest extends AbstractSchemaTestUtils {

    /** The parser to test. */
    @Autowired
    private XlsSchemaParser parser;


    @Test
    public void should_parse_xls() throws IOException {
        final List<String> expected = Arrays.asList("Film", "Producer");
        checkColumnsName("simple.xls", expected);
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-827
     */
    @Test
    public void shouldParseFileWithHeader() throws Exception {
        final List<String> expected = Arrays.asList("col0", "col1", "col2", "col3", "col4");
        checkColumnsName("file_with_header.xlsx", expected);
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-830
     */
    @Test
    public void shouldParseFileWithEmptyColumn() throws Exception {
        final List<String> expected = Arrays.asList("First Name", "Last Name", "Company", "Email Address", "col_5",
                "Current Product",
                "Product to send");
        checkColumnsName("empty_column.xlsx", expected);
    }

    /**
     * Load the excel file and check the parsed columns name against the given ones.
     *
     * @param sourceFileName the excel file name to load.
     * @param expectedColsName the expected columns name.
     * @throws IOException if an error occurs while reading the excel file.
     */
    private void checkColumnsName(String sourceFileName, List<String> expectedColsName) throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream(sourceFileName)) {

            DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata();

            SchemaParserResult result = parser.parse(new SchemaParser.Request(inputStream, datasetMetadata));
            List<ColumnMetadata> columns = result.getSheetContents().get(0).getColumnMetadatas();
            final List<String> actual = columns.stream().map(ColumnMetadata::getName).collect(Collectors.toList());

            Assert.assertEquals(expectedColsName, actual);
        }
    }
}
