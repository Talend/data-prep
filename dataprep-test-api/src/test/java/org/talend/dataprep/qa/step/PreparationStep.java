package org.talend.dataprep.qa.step;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.dto.FolderContent;
import org.talend.dataprep.qa.dto.PreparationDetails;
import org.talend.dataprep.qa.step.config.DataPrepStep;

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

    public static final String DATASET_NAME = "datasetName";

    public static final String NB_STEPS = "nbSteps";

    public static final String DESTINATION = "destination";

    public static final String NEW_PREPARATION_NAME = "newPreparationName";

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationStep.class);

    @Autowired
    protected FolderStep folderStep;

    @Given("^I create a preparation with name \"(.*)\", based on \"(.*)\" dataset$")
    public void givenICreateAPreparation(String preparationName, String datasetName) {
        LOGGER.debug("I create a preparation with name {}", preparationName);
        String homeFolder = api.getHomeFolder();
        final String datasetId = context.getDatasetId(datasetName);
        if (StringUtils.isBlank(datasetId)) {
            fail("could not find dataset id from name '" + datasetName + "' in the context");
        }
        String preparationId = api.createPreparation(datasetId, preparationName, homeFolder).then() //
                .statusCode(200) //
                .extract().body().asString();
        context.storePreparationRef(preparationId, preparationName);
    }

    @Given("^A preparation with the following parameters exists :$")
    public void checkPreparation(DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(params.get(PREPARATION_NAME));
        PreparationDetails prepDet = getPreparationDetails(prepId);
        Assert.assertNotNull(prepDet);
        Assert.assertEquals(prepDet.dataset.dataSetName, params.get(DATASET_NAME));
        Assert.assertEquals(Integer.toString(prepDet.steps.size() - 1), params.get(NB_STEPS));
    }

    @Then("^I move the preparation \"(.*)\" with the following parameters :$")
    public void movePreparation(String preparationName, DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        List<Folder> folders = folderStep.listFolders();
        Folder originFolder = folderStep.extractFolder(params.get(ORIGIN), folders);
        Folder destFolder = folderStep.extractFolder(params.get(DESTINATION), folders);
        String prepId = context.getPreparationId(preparationName);
        Response response = api.movePreparation(prepId, originFolder.id, destFolder.id, params.get(NEW_PREPARATION_NAME));
        response.then().statusCode(200);
    }

    @And("^I check that the preparation \"(.*)\" exists under the folder \"(.*)\"$")
    public void checkExistPrep(String preparationName, String folder) throws IOException {
        String prepId = context.getPreparationId(preparationName);
        Response response = api.listPreparation(folder);
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        FolderContent folderContent = objectMapper.readValue(content, FolderContent.class);

        long nb = folderContent.preparations.stream() //
                .filter(p -> p.id.equals(prepId) //
                        && p.name.equals(preparationName)) //
                .count();
        Assert.assertEquals(nb, 1);
    }
}
