package org.talend.dataprep.qa.step;

import static junit.framework.TestCase.assertTrue;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.ContentMetadata;
import org.talend.dataprep.qa.dto.Statistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ResponseBody;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Step dealing with dataset.
 */
public class DatasetStep extends DataPrepStep {

    private static final String NB_ROW = "nbRow";

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetStep.class);

    @Value("${metadata.timeout.sec}")
    private int metadataTimeout;

    @Value("${metadata.wait.time.sec}")
    private int metadataTimeToWait;

    @Given("^I upload the dataset \"(.*)\" with name \"(.*)\"$") //
    public void givenIUploadTheDataSet(String fileName, String name) throws IOException {
        String suffixedName = suffixName(name);
        createDataSet(fileName, suffixedName);
    }

    @Given("^A dataset with the following parameters exists :$") //
    public void existDataset(Map<String, String> params) throws IOException {
        List<ContentMetadata> datasetMetas = listDatasetMeta();
        assertEquals(1, countFilteredDatasetList(datasetMetas, params.get(DATASET_NAME_KEY), params.get(NB_ROW)));
    }

    @Given("^It doesn't exist any dataset with the following parameters :$") //
    public void notExistDataset(Map<String, String> params) throws IOException {
        List<ContentMetadata> datasetMetas = listDatasetMeta();
        assertEquals(0, countFilteredDatasetList(datasetMetas, params.get(DATASET_NAME_KEY), params.get(NB_ROW)));
    }

    @Given("^It doesn't exist any dataset with the following name \"(.*)\"$") //
    public void notExistDataset(String name) throws IOException {
        List<ContentMetadata> datasetMetas = listDatasetMeta();
        assertEquals(0, countFilteredDatasetList(datasetMetas, name));
    }

    /**
     * Count how many {@link ContentMetadata} corresponding to the specified name & row number exists in the given
     * {@link List}.
     *
     * @param datasetMetas the {@link List} of {@link ContentMetadata} to filter.
     * @param datasetName the searched dataset name.
     * @param nbRows the searched number of row [optional].
     * @return the number of corresponding {@link ContentMetadata}.
     */
    private long countFilteredDatasetList(List<ContentMetadata> datasetMetas, String datasetName, String nbRows) {
        Stream<ContentMetadata> stream = datasetMetas.stream() //
                .filter(d -> suffixName(datasetName).equals(d.name));

        // nbRows not provided in catalog mode
        if (isNotEmpty(nbRows)) {
            stream = stream.filter(contentMetadata -> nbRows.equals(contentMetadata.records));
        }

        return stream.count();
    }

    /**
     * Count how many {@link ContentMetadata} corresponding to the specified name exists in the given
     * {@link List}.
     *
     * @param datasetMetas the {@link List} of {@link ContentMetadata} to filter.
     * @param datasetName the searched dataset name.
     * @return the number of corresponding {@link ContentMetadata}.
     */
    private long countFilteredDatasetList(List<ContentMetadata> datasetMetas, String datasetName) {
        return datasetMetas //
                .stream() //
                .filter(d -> (suffixName(datasetName).equals(d.name))) //
                .count();
    }

    /**
     * List all accessible datasets.
     *
     * @return a {@link List} of {@link ContentMetadata}.
     * @throws IOException in cas of exception.
     */
    private List<ContentMetadata> listDatasetMeta() throws IOException {
        Response response = api.listDatasetDetails();
        response.then().statusCode(OK.value());
        return Arrays.asList(response.as(ContentMetadata[].class));
    }

    @When("^I update the dataset named \"(.*)\" with data \"(.*)\"$") //
    public void givenIUpdateTheDatasetNamedWithData(String datasetName, String fileName) throws Throwable {
        String suffixedDatasetName = suffixName(datasetName);
        LOGGER.debug("I update the dataset named {} with data {}.", suffixedDatasetName, fileName);
        String datasetId = context.getDatasetId(suffixedDatasetName);
        Response response = api.updateDataset(fileName, suffixedDatasetName, datasetId);
        response.then().statusCode(OK.value());
    }

    @When("^I delete the dataset called \"(.*)\"$") //
    public void iDeleteTheDataset(String datasetName) {
        String suffixedDatasetName = suffixName(datasetName);
        String datasetId = context.getDatasetId(suffixedDatasetName);
        api.deleteDataset(datasetId).then().statusCode(OK.value());
        context.removeDatasetRef(suffixedDatasetName);
    }

    @When("^I load the existing dataset called \"(.*)\"$")
    public void registerExistingDataset(String datasetName) throws IOException {
        final List<ContentMetadata> datasetMetas = listDatasetMeta()
                .stream() //
                .filter(meta -> meta.name.equals(datasetName)) //
                .collect(Collectors.toList());
        assertEquals("More (or less) than one dataset with " + datasetName + " name.", 1, datasetMetas.size());
        ContentMetadata datasetMeta = datasetMetas.get(0);
        context.storeExistingDatasetRef(datasetMeta.id, datasetMeta.name);
    }

    private void createDataSet(String fileName, String suffixedName) throws IOException {
        LOGGER.debug("I upload the dataset {} with name {}.", fileName, suffixedName);
        String datasetId;
        switch (util.getFilenameExtension(fileName)) {
        case "xls":
        case "xlsx":
            datasetId = api //
                    .uploadBinaryDataset(fileName, suffixedName) //
                    .then() //
                    .statusCode(OK.value()) //
                    .extract() //
                    .body() //
                    .asString();
            break;
        case "csv":
        default:
            datasetId = api
                    .uploadTextDataset(fileName, suffixedName) //
                    .then() //
                    .statusCode(OK.value()) //
                    .extract() //
                    .body() //
                    .asString();
            break;

        }
        context.storeDatasetRef(datasetId, suffixedName);
    }

    @Given("^I have a dataset with parameters:$")
    public void iHaveADatasetWithParameters(Map<String, String> parameters) throws Throwable {
        // in case of only name parameter, we should use a suffixed dataSet name
        if (parameters.containsKey(DATASET_NAME_KEY) && parameters.size() == 1) {
            parameters.put(DATASET_NAME_KEY, suffixName(parameters.get(DATASET_NAME_KEY)));
        }

        // wait for DataSet creation from previous step
        JsonNode response = null;
        boolean stop = false;
        int nbLoop = 0;
        while (!stop) { // TODO use awaitability library
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
        checkColumnNames(datasetName, columns, actual);
    }

    @Then("^I check that the dataSet \"(.*)\" has \"(.*)\" records$")
    public void thenICheckTheDataSetRecordsNumber(String datasetName, String recordNumber) throws IOException {
        Response response = api.getDataSetMetaData(context.getDatasetId(suffixName(datasetName)));
        response.then().statusCode(OK.value());

        final JsonPath jsonPath = response.body().jsonPath();
        assertEquals(recordNumber, jsonPath.get("records").toString());
    }

    @And("^I wait for the dataset \"(.*)\" metadata to be computed$")
    public void iWaitForTheDatasetMetadataToBeComputed(String datasetName) throws Throwable {
        api
                .waitResponse("Dataset metadata", metadataTimeout, metadataTimeToWait) //
                .until(checkDatasetMetadataStatus(datasetName));
    }

    /**
     * get dataset metadata status.
     */
    private Callable<Boolean> checkDatasetMetadataStatus(String datasetName) {
        return () -> {
            Response response = api.getDataSetMetaData(context.getDatasetId(suffixName(datasetName)));
            response.then().statusCode(OK.value());

            final ContentMetadata actual = response.body().as(ContentMetadata.class);
            Statistics columnStatistics = actual.columns.get(0).statistics;
            return !columnStatistics.frequencyTable.isEmpty() && !columnStatistics.patternFrequencyTable.isEmpty();
        };
    }
}
