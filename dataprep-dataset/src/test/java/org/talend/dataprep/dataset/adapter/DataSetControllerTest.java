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

import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.configuration.DataPrepComponentScanConfiguration;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.service.DataSetService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(DataSetController.class)
@Import({DataPrepComponentScanConfiguration.class })
@Ignore
public class DataSetControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DataSetService dataSetService;

    @MockBean
    private BeanConversionService beanConversionService;

    @Test
    public void findSample() throws Exception {
        InputStream resourceAsStream = DataSetControllerTest.class.getResourceAsStream(
                "/org/talend/dataprep/dataset/avengers_expected_limit_2.json");

        DataSet dataSet = objectMapper.readValue(resourceAsStream, DataSet.class);

        when(dataSetService.getMetadata(anyString())).thenReturn(dataSet);

        //TODO mock the bean conversion
        when(beanConversionService.convert(any(DataSetMetadata.class), Dataset.class)).thenReturn(null);

        MvcResult result =
                mvc.perform(get("/api/v1/datasets/{datasetId}", "1234")) //
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode jsonSchema = objectMapper.readTree(result.getResponse().getContentAsString());

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
