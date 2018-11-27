package org.talend.dataprep.qa.step;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.ContentMetadataColumn;
import org.talend.dataprep.qa.dto.DatasetContent;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.dto.FolderContent;
import org.talend.dataprep.qa.dto.PreparationDetails;

import com.jayway.restassured.response.Response;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Step dealing with preparation
 */
public class PreparationStep extends DataPrepStep {

    private static final String DATASET_NAME = "dataSetName";

    private static final String NB_STEPS = "nbSteps";

    private static final String TDP_INVALID_MARKER = "__tdpInvalid";

    private static final String VALID_CELL = "valid";

    private static final String INVALID_CELL = "invalid";

    private static final String EMPTY_CELL = "empty";

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationStep.class);

    @Given("^I create a preparation with name \"(.*)\", based on \"(.*)\" dataset$")
    public void givenICreateAPreparation(String prepFullName, String datasetName) throws IOException {
        String suffixedPrepName = getSuffixedPrepName(prepFullName);
        String prepPath = util.extractPathFromFullName(prepFullName);
        Folder prepFolder = folderUtil.searchFolder(prepPath);
        String suffixedDatasetName =
                (context.getDatasetId(datasetName) == null) ? suffixName(datasetName) : datasetName;

        final String datasetId = context.getDatasetId(suffixedDatasetName);
        if (StringUtils.isBlank(datasetId)) {
            fail("could not find dataset id from name '" + suffixedDatasetName + "' in the context");
        }

        LOGGER.info("I create a preparation with name {}", suffixedPrepName);
        String preparationId = api
                .createPreparation(datasetId, suffixedPrepName, folderUtil.getAPIFolderRepresentation(prepFolder))
                .then() //
                .statusCode(OK.value()) //
                .extract()
                .body()
                .asString();
        context.storePreparationRef(preparationId, suffixedPrepName, prepFolder.getPath());
    }

    /**
     * Check if an existing preparation contains the same actions as the one given in parameters.
     * Be careful ! If your preparation contains lookup actions, you'll need to load your dataset by restoring a Mongo
     * dump, else the lookup_ds_id won't be the same in actions' parameter value.
     *
     * @param dataTable step parameters.
     * @throws IOException in case of exception.
     */
    @Given("^A preparation with the following parameters exists :$")
    public void checkPreparation(DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String suffixedPrepName = getSuffixedPrepName(params.get(PREPARATION_NAME));
        String prepPath = util.extractPathFromFullName(params.get(PREPARATION_NAME));
        String prepId = context.getPreparationId(suffixedPrepName, prepPath);

        PreparationDetails prepDet = getPreparationDetails(prepId);
        Assert.assertNotNull(prepDet);
        assertEquals(prepDet.dataSetId, context.getDatasetId(suffixName(params.get(DATASET_NAME))));
        assertEquals(Integer.toString(prepDet.steps.size() - 1), params.get(NB_STEPS));

        if (params.get("actionsList") != null) {
            List<Action> actionsList = prepDet.actions;
            checkActionsListOfPrepa(actionsList, params.get("actionsList").toString());
        }
    }

    @When("^I load the existing preparation called \"(.*)\"$")
    public void registerExistingPreparation(String preparationFullname) throws IOException {
        String preparationName = util.extractNameFromFullName(preparationFullname);
        String prepPath = util.extractPathFromFullName(preparationFullname);
        Folder prepFolder = folderUtil.searchFolder(prepPath);
        FolderContent folderContent = folderUtil.listPreparation(prepPath);
        if (folderContent != null) {
            List<PreparationDetails> preparations = folderContent.preparations //
                    .stream() //
                    .filter(p -> p.name.equals(preparationName))
                    .collect(Collectors.toList());
            assertEquals("More than one preparation with \"" + preparationFullname + "\" name founded.", 1,
                    preparations.size());
            PreparationDetails preparation = preparations.get(0);
            context.storeExistingPreparationRef(preparation.id, preparation.name, prepFolder.getPath());
        }
    }

    @Then("^I move the preparation \"(.*)\" to \"(.*)\"$")
    public void movePreparation(String prepOriginFullName, String prepDestFullName) throws IOException {
        String suffixedPrepOriginName = getSuffixedPrepName(prepOriginFullName);
        String suffixedPrepOriginPath = util.extractPathFromFullName(prepOriginFullName);
        String suffixedPrepOriginId = context.getPreparationId(suffixedPrepOriginName, suffixedPrepOriginPath);
        String suffixedPrepDestName = getSuffixedPrepName(prepDestFullName);
        String prepDestPath = util.extractPathFromFullName(prepDestFullName);

        Folder originFolder = folderUtil.searchFolder(suffixedPrepOriginPath);
        Folder destFolder = folderUtil.searchFolder(prepDestPath);

        Response response = api.movePreparation( //
                suffixedPrepOriginId, originFolder.id, destFolder.id, suffixedPrepDestName);
        response.then().statusCode(OK.value());

        context.storePreparationMove(suffixedPrepOriginId, suffixedPrepOriginName, originFolder.path,
                suffixedPrepDestName, destFolder.path);
    }

    @Then("^I copy the preparation \"(.*)\" to \"(.*)\"$")
    public void copyPreparation(String prepOriginFullName, String prepDestFullName) throws IOException {
        String suffixedPrepOriginName = getSuffixedPrepName(prepOriginFullName);
        String suffixedPrepOriginPath = util.extractPathFromFullName(prepOriginFullName);
        String suffixedPrepDestName = getSuffixedPrepName(prepDestFullName);
        String suffixedPrepDestPath = util.extractPathFromFullName(prepDestFullName);

        Folder destFolder = folderUtil.searchFolder(suffixedPrepDestPath);
        String prepId = context.getPreparationId(suffixedPrepOriginName, suffixedPrepOriginPath);
        String newPreparationId = api
                .copyPreparation(prepId, destFolder.id, suffixedPrepDestName)
                .then()
                .statusCode(OK.value())
                .extract()
                .body()
                .asString();
        context.storePreparationRef(newPreparationId, suffixedPrepDestName, destFolder.path);
    }

    @When("^I remove the preparation \"(.*)\"$")
    public void removePreparation(String prepFullName) throws IOException {
        String suffixedPrepPath = util.extractPathFromFullName(prepFullName);
        String prepSuffixedName = getSuffixedPrepName(prepFullName);
        String prepId = context.getPreparationId(prepSuffixedName, suffixedPrepPath);
        api.deletePreparation(prepId).then().statusCode(OK.value());
        context.removePreparationRef(prepSuffixedName, suffixedPrepPath);
    }

    @Then("^I check that the preparation \"(.*)\" doesn't exist$")
    public void checkPreparationNotExist(String prepFullName) throws IOException {
        Assert.assertFalse(doesPrepExistsInFolder(prepFullName));
    }

    @And("^I check that the preparations \"(.*)\" and \"(.*)\" have the same steps$")
    public void checkPreparationsSteps(String prep1FullName, String prep2FullName) {
        String suffixedPrep1Name = getSuffixedPrepName(prep1FullName);
        String prep1Path = util.extractPathFromFullName(prep1FullName);
        String suffixedPrep2Name = getSuffixedPrepName(prep2FullName);
        String prep2Path = util.extractPathFromFullName(prep2FullName);

        String prepId1 = context.getPreparationId(suffixedPrep1Name, prep1Path);
        String prepId2 = context.getPreparationId(suffixedPrep2Name, prep2Path);
        PreparationDetails prepDet1 = getPreparationDetails(prepId1);
        PreparationDetails prepDet2 = getPreparationDetails(prepId2);

        assertEquals(prepDet1.actions, prepDet2.actions);
        assertEquals(prepDet1.steps.size(), prepDet2.steps.size());
        context.storeObject("copiedPrep", prepDet1);
    }

    @And("^I check that the preparation \"(.*)\" exists$")
    public void checkPrepExists(String prepFullName) throws IOException {
        Assert.assertTrue("The preparation does not exists in the Folder", doesPrepExistsInFolder(prepFullName));
    }

    @Then("^I check that I can load \"(.*)\" times the preparation with name \"(.*)\"$")
    public void loadPreparationMultipleTimes(Integer nbTime, String prepFullName) throws IOException {
        String preparationId = context.getPreparationId(suffixName(prepFullName));
        for (int i = 0; i < nbTime; i++) {
            api
                    .waitResponse("Preparation #" + preparationId + " is ready", 10, 0, 1) //
                    .until(() -> {
                        int statusCode = api.getPreparationContent(preparationId, "head", "HEAD", "").getStatusCode();
                        LOGGER.info("Ask for preparation #{} and I received status code #{}", preparationId,
                                statusCode);
                        return statusCode;
                    }, is(HttpStatus.OK.value()));
        }
    }

    /**
     * Extract a preparation name from a full preparation name (i.e. with its path) and suffix it.
     *
     * @param prepFullName the preparation full name (with its dataprep path)
     * @return the suffixed preparation name.
     */
    @NotNull
    protected String getSuffixedPrepName(@NotNull String prepFullName) {
        return suffixName(util.extractNameFromFullName(prepFullName));
    }

    @Then("^The preparation \"(.*)\" should contain the following columns:$")
    public void thePreparationShouldContainTheFollowingColumns(String preparationName, List<String> columns)
            throws Exception {
        Response response = api.getPreparationContent(context.getPreparationId(suffixName(preparationName)),
                VERSION_HEAD, HEAD_ID, StringUtils.EMPTY);
        response.then().statusCode(OK.value());

        checkColumnNames(preparationName, columns, response.jsonPath().getList("metadata.columns.name", String.class));
    }

    @Then("^The preparation \"(.*)\" should have the following quality bar characteristics on the column number \"(.*)\":$")
    public void thePreparationShouldHaveThefollowingQualityBar(String preparationName, String columnNumber,
            DataTable dataTable) throws Exception {
        Response response = api.getPreparationContent(context.getPreparationId(suffixName(preparationName)),
                VERSION_HEAD, HEAD_ID, StringUtils.EMPTY);
        response.then().statusCode(OK.value());

        DatasetContent datasetContent = response.as(DatasetContent.class);

        final Map<String, String> parameters = dataTable.asMap(String.class, String.class);
        Integer validExpected = Integer.parseInt(parameters.get(VALID_CELL));
        Integer invalidExpected = Integer.parseInt(parameters.get(INVALID_CELL));
        Integer emptyExpected = Integer.parseInt(parameters.get(EMPTY_CELL));

        ContentMetadataColumn columnMetadata = datasetContent.metadata.columns.get(Integer.parseInt(columnNumber));
        assertEquals(validExpected, columnMetadata.quality.get(VALID_CELL));
        assertEquals(invalidExpected, columnMetadata.quality.get(INVALID_CELL));
        assertEquals(emptyExpected, columnMetadata.quality.get(EMPTY_CELL));
    }

    @Then("^The preparation \"(.*)\" should have the following invalid characteristics on the row number \"(.*)\":$")
    public void thePreparationShouldHaveThefollowingInvalidCells(String preparationName, String columnNumber,
            DataTable dataTable) throws Exception {
        Response response = api.getPreparationContent(context.getPreparationId(suffixName(preparationName)),
                VERSION_HEAD, HEAD_ID, StringUtils.EMPTY);
        response.then().statusCode(OK.value());

        DatasetContent datasetContent = response.as(DatasetContent.class);

        final Map<String, String> parameters = dataTable.asMap(String.class, String.class);
        String invalidCells = parameters.get("invalidCells");

        HashMap values = (HashMap<String, String>) datasetContent.records.get(Integer.parseInt(columnNumber));
        if (!invalidCells.equals(StringUtils.EMPTY)) {
            assertEquals(invalidCells, values.get(TDP_INVALID_MARKER));
        } else {
            // there is no invalid cell
            assertNull(values.get(TDP_INVALID_MARKER));
        }
    }

    @Then("^The preparation \"(.*)\" should have the following type \"(.*)\" on the following column \"(.*)\"$")
    public void thePreparationShouldHaveThefollowingTypeOnThefollowingColumn(String preparationName, String columnType,
            String columnNumber) throws Exception {
        Response response = api.getPreparationContent(context.getPreparationId(suffixName(preparationName)),
                VERSION_HEAD, HEAD_ID, StringUtils.EMPTY);
        response.then().statusCode(OK.value());

        DatasetContent datasetContent = response.as(DatasetContent.class);

        ContentMetadataColumn columnMetadata = datasetContent.metadata.columns.get(Integer.parseInt(columnNumber));
        assertEquals(columnType, columnMetadata.type);
    }

    /**
     * Check if a preparation of a given name exist in a specified folder.
     *
     * @param prepFullName the seeked preparation.
     * @return <code>true</code> if the preparation is founded, <code>false</code> else.
     * @throws IOException if the folder preparation listing fails.
     */
    private boolean doesPrepExistsInFolder(String prepFullName) throws IOException {
        boolean isPrepPresent = false;
        String suffixedPrepName = getSuffixedPrepName(prepFullName);
        String prepPath = util.extractPathFromFullName(prepFullName);
        String prepId = context.getPreparationId(suffixedPrepName, prepPath);
        FolderContent folderContent = folderUtil.listPreparation(prepPath);
        if (folderContent != null) {
            isPrepPresent = folderContent.preparations
                    .stream() //
                    .filter(p -> p.id.equals(prepId) //
                            && p.name.equals(suffixedPrepName)) //
                    .count() == 1;
        }
        return isPrepPresent;
    }

    private void checkActionsListOfPrepa(List<Action> actionsList, String expectedActionsListFile) throws IOException {
        if (expectedActionsListFile == null) {
            return;
        }
        InputStream expectedActionsListStream = DataPrepStep.class.getResourceAsStream(expectedActionsListFile);
        List<Action> expectedActionsList =
                objectMapper.readValue(expectedActionsListStream, PreparationDetails.class).actions;
        assertEquals(expectedActionsList, actionsList);
    }
}
