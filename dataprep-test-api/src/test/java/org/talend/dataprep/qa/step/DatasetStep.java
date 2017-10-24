package org.talend.dataprep.qa.step;

import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.dto.DatasetMeta;
import org.talend.dataprep.qa.dto.PreparationDetails;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Step dealing with dataset.
 */
public class DatasetStep extends DataPrepStep {

    public static final String DATASET_NAME = "name";

    public static final String NB_ROW = "nbRow";

    public static final String ACTION_NAME = "actionName";

    public static final String COLUMN_NAME = "columnName";

    public static final String COLUMN_ID = "columnId";

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
        Response response = api.listDataset();
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        DatasetMeta datasetMeta = objectMapper.readValue(content, DatasetMeta.class);
        Assert.assertEquals(params.get(DATASET_NAME), datasetMeta.name);
        Assert.assertEquals(params.get(NB_ROW), datasetMeta.records);
    }

    @Given("^A step with the following parameters exists on the preparation \"(.*)\" :$") //
    public void existDataset(String preparationName, DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String preparationId = context.getPreparationId(preparationName);
        Response response = api.getPreparationDetails(preparationId);
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        PreparationDetails preparationDetails = objectMapper.readValue(content, PreparationDetails.class);
        List<PreparationDetails.Action> actions = preparationDetails.actions.stream() //
                .filter(action -> action.action.equals(params.get(ACTION_NAME))) //
                .filter(action -> action.parameters.column_id.equals(params.get(COLUMN_ID))) //
                .filter(action -> action.parameters.column_name.equals(params.get(COLUMN_NAME))) //
                .collect(Collectors.toList());
        Assert.assertEquals(1, actions.size());
    }
}
