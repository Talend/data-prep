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

package org.talend.dataprep.schema.csv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.SchemaParserResult;

/**
 * Unit test for the CSVSchemaParser class.
 * 
 * @see CSVSchemaParser
 */
public class CSVSchemaParserTest extends AbstractSchemaTestUtils {

    /** The parser to test. */
    @Autowired
    private CSVSchemaParser parser;

    @Autowired
    CSVFormatUtils csvFormatUtils;

    @Test
    public void should_parse_csv() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("simple.csv")) {

            final String[] columns = {"first name", "last name"};
            DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata(columns);
            resetParameters(datasetMetadata, ";", Arrays.asList(columns), 1, false);

            SchemaParserResult result = parser.parse(new SchemaParser.Request(inputStream, datasetMetadata));
            List<ColumnMetadata> actual = result.getSheetContents().get(0).getColumnMetadatas();

            Assert.assertEquals(datasetMetadata.getRowMetadata().getColumns(), actual);
        }
    }

    /**
     * When trying to guess the columns data type an IndexOutOfBoundsException should not be thrown.
     * @throws IOException
     */
    @Test
    public void TDP_898() throws IOException {
        String str = "c1;c2"+System.lineSeparator()+"1;2;false";
        try (InputStream inputStream = new ByteArrayInputStream(str.getBytes())) {
            final String[] columns = {"c1", "c2"};
            DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata(columns);
            resetParameters(datasetMetadata, ";", Arrays.asList(columns), 1, false);
            try {
                parser.parse(new SchemaParser.Request(inputStream, datasetMetadata));
            }
            catch(IndexOutOfBoundsException exc){
                Assert.fail("Should not throw an IndexOutOfBoundsException, when parsing!");
            }
        }
    }

    /**
     * Uses the <tt>separator</tt>, <tt>headers</tt>, and <tt>headerNbLines</tt> to reset the parameters of a given
     * metadata.
     *
     * @param dataSetMetadata the specified dataset metadata
     * @param separator the specified separator
     * @param headers the specified headers
     * @param headerNbLines the specified number of lines spanned by the headers
     * @param isFirstLineHeader true if the first line of the dataset is a headers
     */
    private void resetParameters(DataSetMetadata dataSetMetadata, String separator, List<String> headers, int headerNbLines, boolean isFirstLineHeader) {
        dataSetMetadata.getContent().setNbLinesInHeader(headerNbLines);
        Separator newSeparator = new Separator(separator.charAt(0));
        final Map<String, Type> columns = new LinkedHashMap<>();
        headers.stream().forEach(h -> columns.put(h, Type.STRING));
        newSeparator.setHeaders(columns); // default type to string
        newSeparator.setFirstLineAHeader(isFirstLineHeader);
        dataSetMetadata.getContent().setParameters(csvFormatUtils.compileSeparatorProperties(newSeparator));
    }
}
