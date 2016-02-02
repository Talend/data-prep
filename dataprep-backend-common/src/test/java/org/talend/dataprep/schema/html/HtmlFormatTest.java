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

package org.talend.dataprep.schema.html;

import java.io.InputStream;
import java.util.*;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.SchemaParserResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

public class HtmlFormatTest extends AbstractSchemaTestUtils {

    private final static Logger logger = LoggerFactory.getLogger(HtmlFormatTest.class);

    @Inject
    private HtmlSchemaParser parser;

    @Inject
    private HtmlSerializer serializer;

    @Test
    public void read_html_TDP_1136() throws Exception {

        String fileName = "sales-force.xls";

        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata();

        datasetMetadata.setEncoding("UTF-16");

        Map<String, String> parameters = new HashMap<>(2);
        parameters.put(HtmlFormatGuesser.HEADER_SELECTOR_KEY, "table tr th");
        parameters.put(HtmlFormatGuesser.VALUES_SELECTOR_KEY, "table tr td");

        datasetMetadata.getContent().setParameters(parameters);

        SchemaParserResult result = parser
                .parse(new SchemaParser.Request(this.getClass().getResourceAsStream(fileName), datasetMetadata));

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSheetContents()).isNotNull().isNotEmpty().hasSize(1);
        List<ColumnMetadata> columnMetadatas = result.getSheetContents().get(0).getColumnMetadatas();
        Assertions.assertThat(columnMetadatas).isNotNull().isNotEmpty().hasSize(7);

        Assertions.assertThat(columnMetadatas.get(0)) //
                .isEqualToComparingOnlyGivenFields(
                        ColumnMetadata.Builder.column() //
                                .type(Type.STRING).id(0).name("UID").build(), //
                        "id", "name", "type");

        Assertions.assertThat(columnMetadatas.get(1)) //
                .isEqualToComparingOnlyGivenFields(
                        ColumnMetadata.Builder.column() //
                                .type(Type.STRING).id(1).name("Team Member: Name").build(), //
                        "id", "name", "type");

        Assertions.assertThat(columnMetadatas.get(2)) //
                .isEqualToComparingOnlyGivenFields(
                        ColumnMetadata.Builder.column() //
                                .type(Type.STRING).id(2).name("Country").build(), //
                        "id", "name", "type");
    }

    @Test
    public void html_serializer() throws Exception {

        String fileName = "sales-force.xls";

        DataSetMetadata datasetMetadata = ioTestUtils.getSimpleDataSetMetadata();

        datasetMetadata.setEncoding("UTF-16");

        Map<String, String> parameters = new HashMap<>(2);
        parameters.put(HtmlFormatGuesser.HEADER_SELECTOR_KEY, "table tr th");
        parameters.put(HtmlFormatGuesser.VALUES_SELECTOR_KEY, "table tr td");

        datasetMetadata.getContent().setParameters(parameters);

        SchemaParserResult result = parser
                .parse(new SchemaParser.Request(this.getClass().getResourceAsStream(fileName), datasetMetadata));

        datasetMetadata.getRowMetadata().setColumns(result.getSheetContents().get(0).getColumnMetadatas());

        InputStream jsonStream = serializer.serialize(this.getClass().getResourceAsStream(fileName), datasetMetadata);

        String json = IOUtils.toString(jsonStream);

        logger.debug("json: {}", json);

        ObjectMapper mapper = new ObjectMapper();

        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, TreeMap.class);

        List<Map<String, String>> values = mapper.readValue(json, collectionType);

        logger.debug("values: {}", values);

        Map<String, String> row0 = values.get(0);

        Assertions.assertThat(row0).contains(MapEntry.entry("0000", "000001"), //
                MapEntry.entry("0001", "Jennifer BOS"), //
                MapEntry.entry("0002", "France"), //
                MapEntry.entry("0003", "jbos@talend.com"));
    }

}
