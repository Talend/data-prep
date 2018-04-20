/*
 *  ============================================================================
 *
 *  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 *  This source code is available under agreement available at
 *  https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 *  You should have received a copy of the agreement
 *  along with this program; if not, write to Talend SA
 *  9 rue Pages 92150 Suresnes, France
 *
 *  ============================================================================
 */

package org.talend.dataprep.dataset.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.service.DataSetService;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class DataprepDatasetClientTest {

    DataprepDatasetClient dataprepDatasetClient;

    @Mock
    DataSetService dataSetService;

    @Mock
    BeanConversionService beanConversionService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        dataprepDatasetClient = new DataprepDatasetClient(dataSetService, beanConversionService, objectMapper);
    }

    @Test
    public void findSample() throws IOException {
        InputStream resourceAsStream = DataprepDatasetClientTest.class.getResourceAsStream(
                "/org/talend/dataprep/dataset/avengers_expected_limit_2.json");

        DataSet dataSet = objectMapper.readValue(resourceAsStream, DataSet.class);

        when(dataSetService.getMetadata(anyString())).thenReturn(dataSet);

        ObjectNode jsonSchema = dataprepDatasetClient.findSchema("toto");

        assertEquals("org.talend.dataprep", jsonSchema.get("namespace").asText());
        JsonNode fields = jsonSchema.get("fields");
        assertTrue(fields.isArray());

        assertEquals("nickname", fields.get(0).get("name").asText());
        assertEquals("secret_firstname", fields.get(1).get("name").asText());
        assertEquals("secret_lastname", fields.get(2).get("name").asText());
        assertEquals("date_of_birth", fields.get(3).get("name").asText());
        assertEquals("city", fields.get(4).get("name").asText());

    }

    // It happened that our test data was not correct. This ensure it corresponds to the specification.
    @Test
    public void testLocalTestData() throws IOException {
        InputStream resourceAsStream = getClass().getResourceAsStream(
                "api_v1_dataset-sample_8c0d7b05-ea9c-40e9-b506-cf828f255b6d_method_fetch.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(resourceAsStream);

        JsonNode schemaNode = jsonNode.get("schema");
        JsonNode dataNode = jsonNode.get("data");

        StringBuilder avroRecordsString = new StringBuilder();
        dataNode.forEach(jn -> {
            avroRecordsString.append(jn.toString());
        });


        Schema schema = new Schema.Parser().parse(schemaNode.toString());

        GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
        JsonDecoder jsonDecoder = DecoderFactory.get().jsonDecoder(schema, avroRecordsString.toString());

        List<GenericRecord> records = new ArrayList<>();
        GenericRecord nextRead;
        do {
            try {
                nextRead = reader.read(null, jsonDecoder);
                records.add(nextRead);
            } catch (EOFException eof) {
                nextRead = null;
            }
        } while (nextRead != null);
        assertFalse(records.isEmpty());
    }


}
