package org.talend.dataprep.schema.html;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.IoTestUtils;
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

        DataSetMetadata datasetMetadata = IoTestUtils.getSimpleDataSetMetadata();

        datasetMetadata.setEncoding("UTF-16");

        Map<String, String> parameters = new HashMap<>(2);
        parameters.put(HtmlFormatGuesser.HEADER_SELECTOR_KEY, "table tr th");
        parameters.put(HtmlFormatGuesser.VALUES_SELECTOR_KEY, "table tr td");

        datasetMetadata.getContent().setParameters(parameters);

        SchemaParserResult result = parser
                .parse(new SchemaParser.Request(this.getClass().getResourceAsStream(fileName), datasetMetadata));

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSheetContents()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(result.getSheetContents().get(0).getColumnMetadatas()).isNotNull().isNotEmpty().hasSize(7);

    }

    @Test
    public void html_serializer() throws Exception {

        String fileName = "sales-force.xls";

        DataSetMetadata datasetMetadata = IoTestUtils.getSimpleDataSetMetadata();

        datasetMetadata.setEncoding("UTF-16");

        Map<String, String> parameters = new HashMap<>(2);
        parameters.put(HtmlFormatGuesser.HEADER_SELECTOR_KEY, "table tr th");
        parameters.put(HtmlFormatGuesser.VALUES_SELECTOR_KEY, "table tr td");

        datasetMetadata.getContent().setParameters(parameters);

        SchemaParserResult result = parser
            .parse(new SchemaParser.Request(this.getClass().getResourceAsStream(fileName), datasetMetadata));

        datasetMetadata.getRowMetadata().setColumns( result.getSheetContents().get( 0 ).getColumnMetadatas() );

        InputStream jsonStream = serializer.serialize(this.getClass().getResourceAsStream(fileName), datasetMetadata);

        String json = IOUtils.toString(jsonStream);

        logger.debug("json: {}", json);

        ObjectMapper mapper = new ObjectMapper();

        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, TreeMap.class);

        List<Map<String, String>> values = mapper.readValue(json, collectionType);

        logger.debug("values: {}", values);

    }

}
