package org.talend.dataprep.qa.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.path.json.exception.JsonPathException;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ResponseBody;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.awaitility.core.ConditionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.DatasetMeta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.awaitility.Awaitility.with;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

/**
 * Step dealing with dataset.
 */
public class DatasetStep extends DataPrepStep {

    private static final String NB_ROW = "nbRow";

    private static final int timeOut = 20;

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
        List<DatasetMeta> datasetMetas = listDatasetMeta();
        assertEquals(1, countFilteredDatasetList(datasetMetas, params.get(DATASET_NAME_KEY), params.get(NB_ROW)));
    }

    @Given("^It doesn't exist any dataset with the following parameters :$") //
    public void notExistDataset(DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        List<DatasetMeta> datasetMetas = listDatasetMeta();
        assertEquals(0, countFilteredDatasetList(datasetMetas, params.get(DATASET_NAME_KEY), params.get(NB_ROW)));
    }

    /**
     * Count how many {@link DatasetMeta} corresponding to the specified name & row number exists in the given
     * {@link List}.
     *
     * @param datasetMetas the {@link List} of {@link DatasetMeta} to filter.
     * @param datasetName  the searched dataset name.
     * @param nbRows       the searched number of row.
     * @return the number of corresponding {@link DatasetMeta}.
     */
    private long countFilteredDatasetList(List<DatasetMeta> datasetMetas, String datasetName, String nbRows) {
        return datasetMetas
                .stream() //
                .filter(d -> (suffixName(datasetName).equals(d.name)) //
                        && nbRows.equals(d.records)) //
                .count();
    }

    /**
     * List all accessible datasets.
     *
     * @return a {@link List} of {@link DatasetMeta}.
     * @throws IOException in cas of exception.
     */
    private List<DatasetMeta> listDatasetMeta() throws IOException {
        Response response = api.listDatasetDetails();
        response.then().statusCode(OK.value());
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        return objectMapper.readValue(content, new TypeReference<List<DatasetMeta>>() {
        });
    }

    @When("^I update the dataset named \"(.*)\" with data \"(.*)\"$") //
    public void givenIUpdateTheDatasetNamedWithData(String datasetName, String fileName) throws Throwable {
        String suffixedDatasetName = suffixName(datasetName);
        LOGGER.debug("I update the dataset named {} with data {}.", suffixedDatasetName, fileName);
        String datasetId = context.getDatasetId(suffixedDatasetName);
        Response response = api.updateDataset(fileName, suffixedDatasetName, datasetId);
        response.then().statusCode(OK.value());
    }

    @Then("^I check that the semantic type \"(.*)\" exists the types list of the column \"(.*)\" of the dataset$")
    @Deprecated
    public void thenICheckSemanticTypeExist(String semanticTypeId, String columnId)
            throws IOException, InterruptedException {
        String dataSetId = context.getObject(DATASET_ID_KEY).toString();

        checkDatasetsColumnSemanticTypes(semanticTypeId, columnId, dataSetId, true);
    }

    @Then("^I check the existence of \"(.*)\" semantic type on \"(.*)\" column for the \"(.*)\" dataset.$")
    public void thenICheckSemanticTypeExistOnDataset(String semanticTypeName, String columnId, String dataSetName)
            throws Exception {
        String dataSetId = context.getDatasetId(suffixName(dataSetName));

        waitResponse("existence of " + semanticTypeName + " semantic type on " + columnId + " column for the " + dataSetName + " dataset") //
                .until(checkDatasetsColumnSemanticTypesCallable(semanticTypeName, columnId, dataSetId, true));
    }

    @Then("^I check the existence of \"(.*)\" custom semantic type on \"(.*)\" column for the \"(.*)\" dataset.$")
    public void thenICheckCustomSemanticTypeExistOnDataset(String semanticTypeName, String columnId, String dataSetName)
            throws Exception {
        thenICheckSemanticTypeExistOnDataset(suffixName(semanticTypeName), columnId, dataSetName);
    }

    @Then("^I check the absence of \"(.*)\" semantic type on \"(.*)\" column for the \"(.*)\" dataset.$")
    public void thenICheckSemanticTypeDoesNotExistOnDataset(String semanticTypeName, String columnId,
                                                            String dataSetName) {
        String dataSetId = context.getDatasetId(suffixName(semanticTypeName));
        checkDatasetsColumnSemanticTypes(semanticTypeName, columnId, dataSetId, false);
    }

    @Then("^I check the absence of \"(.*)\" custom semantic type on \"(.*)\" column for the \"(.*)\" dataset.$")
    public void thenICheckCustomSemanticTypeDoesNotExistOnDataset(String semanticTypeName, String columnId,
                                                                  String dataSetName) {
        thenICheckSemanticTypeDoesNotExistOnDataset(dataSetName, columnId, suffixName(semanticTypeName));
    }

    private void checkDatasetsColumnSemanticTypes(String semanticTypeName, String columnId, String dataSetId,
                                                  boolean expected) {
        Response response = api.getDatasetsColumnSemanticTypes(columnId, dataSetId);
        response.then().statusCode(OK.value());

        int semanticTypeFound = countSemanticType(response, semanticTypeName);

        if (expected) {
            // we expect the semantic Type
            assertTrue(semanticTypeName + " was not found", semanticTypeFound == 1);
        } else {
            // We don't expect the semantic type, and no semantic type exist for this column
            if (!StringUtils.EMPTY.equals(response.body().print())) {
                assertTrue(response.body().print() + " not empty", semanticTypeFound == 0);

            }
        }
    }

    private ConditionFactory waitResponse(String message) {
        return with().pollInterval(2, TimeUnit.SECONDS).and() //
                .with() //
                .pollDelay(1, TimeUnit.SECONDS) //
                .await(message) //
                .atMost(timeOut, TimeUnit.SECONDS).atMost(timeOut, TimeUnit.SECONDS);
    }

    private Callable<Boolean> checkDatasetsColumnQualityCallable(int expectedValue, String qualityField, String columnId, String dataSetId) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                Response response = api.getDataSetMetaData(dataSetId);
                response.then().statusCode(OK.value());
                boolean test = columnId.equals("0000") && expectedValue == 5;
                int res = response.body().jsonPath().get("columns.findAll { c -> c.id == '" + columnId + "'}.quality[0]." + qualityField);
                return response.body().jsonPath().get("columns.findAll { c -> c.id == '" + columnId + "'}.quality[0]." + qualityField).equals(expectedValue);
            }
        };
    }

    private Callable<Boolean> checkDatasetsColumnSemanticTypesCallable(String semanticTypeName, String columnId, String dataSetId,
                                                                       boolean expected) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                Response response = api.getDatasetsColumnSemanticTypes(columnId, dataSetId);
                response.then().statusCode(OK.value());

                int semanticTypeFound = countSemanticType(response, semanticTypeName);
                if (expected) {
                    // we expect the semantic Type
                    return semanticTypeFound == 1;
                } else if (!StringUtils.EMPTY.equals(response.body().print())) {
                    // We don't expect the semantic type, and no semantic type exist for this column
                    return semanticTypeFound == 0;
                }
                return false;
            }
        };
    }

    @Then("^I check the existence of \"(.*)\" semantic type on \"(.*)\" column for the \"(.*)\" preparation.$")
    public void thenICheckSemanticTypeExistOnPreparation(String semanticTypeName, String columnId,
                                                         String preparationName) {
        String preparationId = context.getPreparationId(suffixName(preparationName));
        checkPreparationColumnSemanticTypes(semanticTypeName, columnId, preparationId, true);
    }

    @Then("^I check the absence of \"(.*)\" semantic type on \"(.*)\" column for the \"(.*)\" preparation.$")
    public void thenICheckSemanticTypeDoesNotExistOnPreparation(String semanticTypeName, String columnId,
                                                                String preparationName) {
        String preparationId = context.getPreparationId(suffixName(preparationName));
        checkPreparationColumnSemanticTypes(semanticTypeName, columnId, preparationId, false);
    }

    @Then("^I check the absence of \"(.*)\" custom semantic type on \"(.*)\" column for the \"(.*)\" preparation.$")
    public void thenICheckCustomSemanticTypeDoesNotExistOnPreparation(String semanticTypeName, String columnId,
                                                                      String preparationName) {
        thenICheckSemanticTypeDoesNotExistOnPreparation(suffixName(semanticTypeName), columnId, preparationName);
    }

    @Then("^I check that there are \"(.*)\" \"(.*)\" cells for the column \"(.*)\" of the dataset \"(.*)\"$")
    public void checkColumnQuality(Integer expectedValue, String qualityField, String columnId, String dataSetName)
            throws Exception {
        String dataSetId = context.getDatasetId(suffixName(dataSetName));
        checkQuality(expectedValue, qualityField, columnId, dataSetId);
    }

    private void checkQuality(Integer expectedValue, String qualityField, String columnId, String dataSetId)
            throws Exception {

        waitResponse("check quality of ") // TODO
                .until(checkDatasetsColumnQualityCallable(expectedValue, qualityField, columnId, dataSetId));
    }

    private void checkPreparationColumnSemanticTypes(String semanticTypeName, String columnId, String preparationId,
                                                     boolean expected) {

        Response response = api.getPreparationsColumnSemanticTypes(columnId, preparationId);
        response.then().statusCode(OK.value());

        StringBuilder errorMessage = new StringBuilder();
        if (expected) {
            errorMessage
                    .append("Expected semantic type \"") //
                    .append(semanticTypeName) //
                    .append("\" wasn't find on column \"") //
                    .append(columnId) //
                    .append("\" ") //
                    .append("of the preparation \"") //
                    .append(preparationId) //
                    .append("\".");
        } else {
            errorMessage
                    .append("The semantic type \"") //
                    .append(semanticTypeName) //
                    .append("\" is present on column \"") //
                    .append(columnId) //
                    .append("\" ") //
                    .append("of the preparation \"") //
                    .append(preparationId) //
                    .append("\".");
        }

        assertEquals(errorMessage.toString(), expected ? 1 : 0, countSemanticType(response, suffixName(semanticTypeName)));
    }

    private int countSemanticType(Response response, String semanticTypeName) {
        JsonPath jsonPath = response.body().jsonPath();
        int size = 0;
        try {
            size = jsonPath.getList("findAll { semanticType -> semanticType.label == '" + semanticTypeName + "'  }").size();
        } catch (JsonPathException jsonPathException) {
            //jsonPath.get();
            LOGGER.debug("The JSON input text should neither be null nor empty. {} is not present.", semanticTypeName, jsonPathException);
        }
        return size;
    }

    private void createDataSet(String fileName, String suffixedName) throws IOException {
        LOGGER.debug("I upload the dataset {} with name {}.", fileName, suffixedName);
        String datasetId;
        switch (util.getFilenameExtension(fileName)) {
            case "xls":
            case "xlsx":
                datasetId = api
                        .uploadBinaryDataset(fileName, suffixedName) //
                        .then()
                        .statusCode(OK.value()) //
                        .extract()
                        .body()
                        .asString();
                break;
            case "csv":
            default:
                datasetId = api
                        .uploadTextDataset(fileName, suffixedName) //
                        .then()
                        .statusCode(OK.value()) //
                        .extract()
                        .body()
                        .asString();
                break;

        }
        context.storeDatasetRef(datasetId, suffixedName);
    }

    @Given("^I have a dataset with parameters:$")
    public void iHaveADatasetWithParameters(DataTable dataTable) throws Throwable {
        Map<String, String> parameters = new HashMap<>(dataTable.asMap(String.class, String.class));

        // in case of only name parameter, we should use a suffixed dataSet name
        if (parameters.containsKey(DATASET_NAME_KEY) && parameters.size() == 1) {
            parameters.put(DATASET_NAME_KEY, suffixName(parameters.get(DATASET_NAME_KEY)));
        }

        // wait for DataSet creation from previous step
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

        context.storeDatasetRef(dataset.get("id").asText(), dataset.get(DATASET_NAME_KEY).asText());
    }

    @Then("^I check that the dataSet \"(.*)\" is created with the following columns :$")
    public void thenTheDataSetIsCreatedWithColumns(String datasetName, List<String> columns) throws IOException {
        Response response = api.getDataSetMetaData(context.getDatasetId(suffixName(datasetName)));
        response.then().statusCode(OK.value());

        final JsonPath jsonPath = response.body().jsonPath();
        final List<String> actual = jsonPath.getList("columns.name", String.class);
        assertNotNull(new StringBuilder("No columns in dataset \"").append(datasetName).append("\".").toString(),
                actual);
        assertFalse(new StringBuilder("No columns in dataset \"").append(datasetName).append("\".").toString(),
                actual.isEmpty());
        assertEquals(new StringBuilder("Not the expected number of columns in dataset \"")
                .append(datasetName)
                .append("\".")
                .toString(), columns.size(), actual.size());
        assertTrue(new StringBuilder("The \"")
                .append(datasetName)
                .append("\" dataset doesn't contains the expected columns.")
                .toString(), actual.containsAll(columns));
    }
}
