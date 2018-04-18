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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.talend.dataprep.conversions.BeanConversionService;
import org.talend.dataprep.dataset.client.DatasetClient;
import org.talend.dataprep.dataset.domain.Dataset;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(DataSetController.class)
public class DataSetControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DatasetClient datasetClient;

    @MockBean
    private BeanConversionService conversionService;

    @Test
    public void getDatasetMetadata() throws Exception {
        String datasetId = "1251df6e-33db-42ba-8651-fdfff081390f";

        InputStream jsonStream = DataSetControllerTest.class.getResourceAsStream("dataset_payload_example.json");
        Dataset dataset = objectMapper.readValue(jsonStream, Dataset.class);

        given(datasetClient.findOne(datasetId)).willReturn(dataset);

        mvc.perform(get("/api/v1/datasets/{datasetId}", datasetId)).andExpect(status().isOk());
    }
}
