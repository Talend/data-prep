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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.service.DataSetService;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
}
