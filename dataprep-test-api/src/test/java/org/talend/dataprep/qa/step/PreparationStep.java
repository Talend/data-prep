package org.talend.dataprep.qa.step;

import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.dto.FolderContent;
import org.talend.dataprep.qa.dto.PreparationDetails;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

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
    public void checkPreparation(DataTable dataTable) throws IOException {
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

    @And("^I check that the preparation \"(.*)\" exists under the folder \"(.*)\"$")
    public void checkExistPrep(String preparationName, String folder) throws IOException {
        String suffixedPreparationName = suffixName(preparationName);
        String prepId = context.getPreparationId(suffixedPreparationName);
        FolderContent folderContent = folderUtil.listPreparation(folder);

        long nb = folderContent.preparations.stream() //
                .filter(p -> p.id.equals(prepId) //
                        && p.name.equals(suffixedPreparationName)) //
                .count();
        Assert.assertEquals(1, nb);
    }
}