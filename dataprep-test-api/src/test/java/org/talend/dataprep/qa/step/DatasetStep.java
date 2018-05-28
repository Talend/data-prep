package org.talend.dataprep.qa.step;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.DatasetMeta;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ResponseBody;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Step dealing with dataset.
 */
public class DatasetStep extends DataPrepStep {

    public static final String DATASET_NAME = "name";

    public static final String NB_ROW = "nbRow";

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetStep.class);

    @Given("^I upload the dataset \"(.*)\" with name \"(.*)\"$") //
    public void givenIUploadTheDataSet(String fileName, String name) throws IOException {
        String suffixedName = suffixName(name);
        createDataSet(fileName, suffixedName);
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
                        .filter(d -> (suffixName(params.get(DATASET_NAME))).equals(d.name) //
                                && params.get(NB_ROW).equals(d.records)) //
                        .count());
    }

    @When("^I update the dataset named \"(.*)\" with data \"(.*)\"$") //
    public void givenIUpdateTheDatasetNamedWithData(String datasetName, String fileName) throws Throwable {
        String suffixedDatasetName = suffixName(datasetName);
        LOGGER.debug("I update the dataset named {} with data {}.", suffixedDatasetName, fileName);
        String datasetId = context.getDatasetId(suffixedDatasetName);
        Response response = api.updateDataset(fileName, suffixedDatasetName, datasetId);
        response.then().statusCode(200);
    }

    @Then("^I check that the semantic type \"(.*)\" exists the types list of the column \"(.*)\" of the dataset$")
    @Deprecated
    public void thenICheckSemanticTypeExist(String semanticTypeId, String columnId)
            throws IOException, InterruptedException {
        String dataSetId = context.getObject("dataSetId").toString();

        getDatasetsColumnSemanticTypes(semanticTypeId, columnId, dataSetId);
    }

    @Then("^I check that the semantic type \"(.*)\" exists the types list of the column \"(.*)\" of the dataset \"(.*)\"$")
    public void thenICheckSemanticTypeExist(String semanticTypeId, String columnId, String dataSetName)
            throws IOException, InterruptedException {
        String dataSetId = context.getDatasetId(suffixName(dataSetName));
        getDatasetsColumnSemanticTypes(semanticTypeId, columnId, dataSetId);
    }

    private void getDatasetsColumnSemanticTypes(String semanticTypeId, String columnId, String dataSetId) {
        Response response = api.getDatasetsColumnSemanticTypes(columnId, dataSetId);
        response.then().statusCode(200);

        assertEquals(1, response.body()
                .jsonPath()
                .getList("findAll { semanticType -> semanticType.id == '" + suffixName(semanticTypeId) + "'  }")
                .size());
    }

    private void createDataSet(String fileName, String suffixedName) throws IOException {
        LOGGER.debug("I upload the dataset {} with name {}.", fileName, suffixedName);
        String datasetId;
        switch (util.getFilenameExtension(fileName)) {
        case "xls":
        case "xlsx":
            datasetId = api.uploadBinaryDataset(fileName, suffixedName) //
                    .then().statusCode(200) //
                    .extract().body().asString();
            break;
        case "csv":
        default:
            datasetId = api.uploadTextDataset(fileName, suffixedName) //
                    .then().statusCode(200) //
                    .extract().body().asString();
            break;

        }
        context.storeDatasetRef(datasetId, suffixedName);
    }

    @Given("^I have a dataset with parameters:$")
    public void iHaveADatasetWithParameters(DataTable dataTable) throws Throwable {
        Map<String, String> parameters = new HashMap<>(dataTable.asMap(String.class, String.class));

        // in case of only name parameter, we should use a suffixed dataSet name
        if (parameters.containsKey("name") && parameters.size() == 1) {
            parameters.put("name", suffixName(parameters.get("name")));
        }

        //wait for DataSet creation from previous step
        JsonNode response = null;
        boolean stop = false;
        int nbLoop = 0;
        while (!stop) {
            nbLoop++;
            if (nbLoop > 10) {
                fail("Dataset creation is so slow");
            }

            ResponseBody responseBody = api.getDatasets(parameters).body();
            response = objectMapper.readTree(responseBody.asInputStream());
            LOGGER.info("DataSet with parameters [{}]: {}", parameters, response);

            stop = response.size() == 1;
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException iexception) {
                LOGGER.error("Interrupted sleep (not the expected behaviour...", iexception);
                fail("cannot sleep");
            }
        }

        assertTrue(response.isArray());
        JsonNode dataset = response.get(0);

        parameters.forEach((key, value) -> assertEquals(value, dataset.get(key).asText()));

        context.storeDatasetRef(dataset.get("id").asText(), dataset.get("name").asText());
    }

}
