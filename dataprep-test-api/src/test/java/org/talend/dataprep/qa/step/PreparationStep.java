package org.talend.dataprep.qa.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    public static final String NB_STEPS = "nbSteps";

    public static final String DESTINATION = "destination";

    /**
     * {@link cucumber.api.DataTable} key for new preparationName value.
     */
    public static final String NEW_PREPARATION_NAME = "newPreparationName";

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationStep.class);

    @Given("^I create a preparation with name \"(.*)\", based on \"(.*)\" dataset$")
    public void givenICreateAPreparation(String preparationName, String datasetName) {
        String suffixedPreparationName = suffixName(preparationName);
        String suffixedDatasetName = suffixName(datasetName);
        LOGGER.info("I create a preparation with name {}", suffixedPreparationName);
        String homeFolder = api.getHomeFolder();
        final String datasetId = context.getDatasetId(suffixedDatasetName);
        if (StringUtils.isBlank(datasetId)) {
            fail("could not find dataset id from name '" + suffixedDatasetName + "' in the context");
        }
        String preparationId = api.createPreparation(datasetId, suffixedPreparationName, homeFolder).then() //
                .statusCode(200) //
                .extract().body().asString();

        context.storePreparationRef(preparationId, suffixedPreparationName);
    }

    @Given("^A preparation with the following parameters exists :$")
    public void checkPreparation(DataTable dataTable) {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(suffixName(params.get(PREPARATION_NAME)));
        PreparationDetails prepDet = getPreparationDetails(prepId);
        Assert.assertNotNull(prepDet);
        Assert.assertEquals(prepDet.dataset.dataSetName, suffixName(params.get(DATASET_NAME)));
        Assert.assertEquals(Integer.toString(prepDet.steps.size() - 1), params.get(NB_STEPS));
    }

    @Then("^I move the preparation \"(.*)\" with the following parameters :$")
    public void movePreparation(String preparationName, DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        List<Folder> folders = folderUtil.listFolders();
        Folder originFolder = folderUtil.extractFolder(params.get(ORIGIN), folders);
        Folder destFolder = folderUtil.extractFolder(params.get(DESTINATION), folders);
        String prepId = context.getPreparationId(suffixName(preparationName));
        Response response = api.movePreparation(prepId, originFolder.id, destFolder.id,
                suffixName(params.get(NEW_PREPARATION_NAME)));
        response.then().statusCode(200);
    }

    @Then("^I copy the preparation \"(.*)\" with the following parameters :$")
    public void copyPreparation(String preparationName, DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String suffixedPreparationName = suffixName(params.get(NEW_PREPARATION_NAME));
        List<Folder> folders = folderUtil.listFolders();
        Folder destFolder = folderUtil.extractFolder(params.get(DESTINATION), folders);
        String prepId = context.getPreparationId(suffixName(preparationName));
        String newPreparationId = api.copyPreparation(prepId, destFolder.id, suffixedPreparationName).then().statusCode(200)
                .extract().body().asString();

        context.storePreparationRef(newPreparationId, suffixedPreparationName);
    }

    @When("^I remove the preparation \"(.*)\"$")
    public void removePreparation(String preparationName) throws IOException {
        String prepId = context.getPreparationId(suffixName(preparationName));
        api.deletePreparation(prepId).then().statusCode(200);
        context.removePreparationRef(suffixName(preparationName));
    }

    @Then("^I check that the preparation \"(.*)\" doesn't exist in the folder \"(.*)\"$")
    public void checkPreparationNotExist(String preparationName, String folder) throws IOException {
        Assert.assertEquals(0, checkPrepExistsInTheFolder(preparationName, folder));
    }

    @And("I check that the preparations \"(.*)\" and \"(.*)\" have the same steps$")
    public void checkPreparationsSteps(String preparation1, String preparation2) {
        String prepId1 = context.getPreparationId(suffixName(preparation1));
        String prepId2 = context.getPreparationId(suffixName(preparation2));
        PreparationDetails prepDet1 = getPreparationDetails(prepId1);
        PreparationDetails prepDet2 = getPreparationDetails(prepId2);

        Assert.assertEquals(prepDet1.actions, prepDet2.actions);
        Assert.assertEquals(prepDet1.steps.size(), prepDet2.steps.size());
        context.storeObject("copiedPrep", prepDet1);
    }

    @And("^I check that the preparation \"(.*)\" exists under the folder \"(.*)\"$")
    public void checkPrepExists(String preparationName, String folder) throws IOException {
        Assert.assertEquals(1, checkPrepExistsInTheFolder(preparationName, folder));
    }

    private long checkPrepExistsInTheFolder(String preparationName, String folder) throws IOException {
        String suffixedPreparationName = suffixName(preparationName);
        String prepId = context.getPreparationId(suffixedPreparationName);
        FolderContent folderContent = folderUtil.listPreparation(folder);

        return folderContent.preparations.stream() //
                .filter(p -> p.id.equals(prepId) //
                        && p.name.equals(suffixedPreparationName)) //
                .count();
    }

    @And("^I check that the semantic type \"([^\"]*)\" is removed from the types list of the column \"([^\"]*)\" of the preparation \"([^\"]*)\"$")
    public void iCheckThatTheSemanticTypeIsRemoved(String semantictypeName, String columnId, String prepName) {
        String prepId = context.getPreparationId(suffixName(prepName));
        String semantictypeId = context.getSemanticTypeId(suffixName(semantictypeName));

        Response response = api.getPreparationsColumnSemanticTypes(columnId, prepId);
        response.then().statusCode(200);

        assertEquals(0, response.body()
                .jsonPath()
                .getList("findAll { semanticType -> semanticType.id == '" + suffixName(semantictypeId) + "'  }")
                .size());
    }
}
