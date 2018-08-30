package org.talend.dataprep.qa.step;

import static org.junit.Assert.*;
import static org.springframework.http.HttpStatus.*;
import static org.talend.dataprep.qa.config.FeatureContext.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import javax.validation.constraints.*;

import org.apache.commons.lang.*;
import org.junit.*;
import org.slf4j.*;
import org.talend.dataprep.qa.config.*;
import org.talend.dataprep.qa.dto.*;

import com.jayway.restassured.response.*;

import cucumber.api.*;
import cucumber.api.java.en.*;

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

    @Given("^A preparation with the following parameters exists :$")
    public void checkPreparation(DataTable dataTable) {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String suffixedPrepName = getSuffixedPrepName(params.get(PREPARATION_NAME));
        String prepPath = util.extractPathFromFullName(params.get(PREPARATION_NAME));
        String prepId = context.getPreparationId(suffixedPrepName, prepPath);

        PreparationDetails prepDet = getPreparationDetails(prepId);
        Assert.assertNotNull(prepDet);
        Assert.assertEquals(prepDet.dataSetId, context.getDatasetId(suffixName(params.get(DATASET_NAME))));
        Assert.assertEquals(Integer.toString(prepDet.steps.size() - 1), params.get(NB_STEPS));

    }

    public void existStepMigration(String prepFullName)throws IOException {
        String prepPath = util.extractPathFromFullName(prepFullName);
        String prepId = context.getPreparationId(prepFullName, prepPath);
        PreparationDetails prepDet = getPreparationDetails(prepId);
        long debug = prepDet.actions
                .stream() //
                .filter(action -> action.action.equals("blabla"))
                .count();
        System.out.println("*****debug****"+debug );
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
            assertEquals("More than one preparation with \"" + preparationFullname + "\" name founded.",
                    preparations.size(), 1);
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
        Assert.assertTrue(doesPrepExistsInFolder(prepFullName));
    }

    @Then("^I check that I can load \"(.*)\" times the preparation with name \"(.*)\"$")
    public void loadPreparationMultipleTimes(Integer nbTime, String prepFullName) throws IOException {
        String prepId = context.getPreparationId(suffixName(prepFullName));
        for (int i = 0; i < nbTime; i++) {
            Response response = api.getPreparationContent(prepId, "head", "HEAD", "");
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

    @Then("^The preparation \"(.*)\" should contain the following columns:$")
    public void thePreparationShouldContainTheFollowingColumns(String preparationName, List<String> columns)
            throws Exception {
        Response response =
                api.getPreparationContent(context.getPreparationId(suffixName(preparationName)), "head", "HEAD", "");
        response.then().statusCode(OK.value());

        checkColumnNames(preparationName, columns, response.jsonPath().getList("metadata.columns.name", String.class));
    }

    public String getId(String name) throws Throwable {
        String prepPath = util.extractPathFromFullName(name);
        FolderContent folderContent = folderUtil.listPreparation(prepPath);
        PreparationDetails prepaDetails = folderContent.preparations
                .stream() //
                .filter(p -> p.name.equals(name)) //
                .findAny()
                .orElse(null);
        return prepaDetails.id;
    }

    @Given("^I had the existing preparation \"(.*)\"") //
    public void givenExistingPrepa(String name) throws Throwable {
        String prepPath = util.extractPathFromFullName(name);
        context.storePreparationRef(getId(name), name, prepPath);
    }

    @Then("^The folder \"(.*)\" has \"(.*)\" preparations") //
    public void checkFolderPrepas(String path, int number) throws Throwable {
        path = util.extractPathFromFullName(path);
        assertEquals(number, folderUtil.listPreparation(path).preparations.size());
    }
}
