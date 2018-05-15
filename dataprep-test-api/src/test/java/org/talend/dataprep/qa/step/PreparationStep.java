package org.talend.dataprep.qa.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

import java.io.IOException;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.config.DataPrepStep;
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

    public static final String DATASET_NAME = "dataSetName";

    private static final String NB_STEPS = "nbSteps";

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationStep.class);

    @Given("^I create a preparation with name \"(.*)\", based on \"(.*)\" dataset$")
    public void givenICreateAPreparation(String prepFullName, String datasetName) throws IOException {
        String suffixedPrepName = getSuffixedPrepName(prepFullName);
        String prepPath = util.extractPathFromFullName(prepFullName);
        Folder prepFolder = folderUtil.searchFolder(prepPath);
        String suffixedDatasetName = suffixName(datasetName);

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

    @Given("^A preparation with the following parameters exists :$")
    public void checkPreparation(DataTable dataTable) {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String suffixedPrepName = getSuffixedPrepName(params.get(PREPARATION_NAME));
        String prepPath = util.extractPathFromFullName(params.get(PREPARATION_NAME));
        String prepId = context.getPreparationId(suffixedPrepName, prepPath);

        PreparationDetails prepDet = getPreparationDetails(prepId);
        Assert.assertNotNull(prepDet);
        Assert.assertEquals(prepDet.dataset.dataSetName, suffixName(params.get(DATASET_NAME)));
        Assert.assertEquals(Integer.toString(prepDet.steps.size() - 1), params.get(NB_STEPS));
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

    @And("I check that the preparations \"(.*)\" and \"(.*)\" have the same steps$")
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
        Assert.assertTrue(doesPrepExistsInFolder(prepFullName));
    }

    @Then("^I check that I can load \"(.*)\" times the preparation with name \"(.*)\"")
    public void loadPreparationMultipleTimes(Integer nbTime, String prepFullName) throws IOException {
        String prepId = context.getPreparationId(suffixName(prepFullName));
        for (int i = 0; i < nbTime; i++) {
            Response response = api.getPreparationContent(prepId, "head", "HEAD");
            assertEquals(OK.value(), response.getStatusCode());
        }
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

}
