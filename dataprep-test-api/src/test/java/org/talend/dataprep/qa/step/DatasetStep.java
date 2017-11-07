package org.talend.dataprep.qa.step;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.dto.DatasetMeta;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.response.Response;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;

/**
 * Step dealing with dataset.
 */
public class DatasetStep extends DataPrepStep {

    public static final String DATASET_NAME = "name";

    public static final String NB_ROW = "nbRow";

    public static final String ACTION_NAME = "actionName";

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetStep.class);

    @Given("^I upload the dataset \"(.*)\" with name \"(.*)\"$") //
    public void givenIUploadTheDataSet(String fileName, String name) throws IOException {
        LOGGER.debug("I upload the dataset {} with name {}.", fileName, name);
        String datasetId = api.uploadDataset(fileName, name) //
                .then().statusCode(200) //
                .extract().body().asString();
        context.storeDatasetRef(datasetId, name);
    }

    @Given("^A dataset with the following parameters exists :$") //
    public void existDataset(DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        Response response = api.listDatasetDetails();
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        List<DatasetMeta> datasetMetas = objectMapper.readValue(content, new TypeReference<List<DatasetMeta>>() {
        });

        Assert.assertEquals(1, //
                datasetMetas.stream() //
                        .filter(d -> params.get(DATASET_NAME).equals(d.name) //
                                && params.get(NB_ROW).equals(d.records)) //
                        .count());
    }
}
