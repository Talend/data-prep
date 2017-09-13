package org.talend.dataprep.qa.api.step;

import io.restassured.response.Response;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.helper.DataPrepAPIHelper;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.hasItems;


@Component
public class DatasetStory extends TalendStory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetStory.class);

    @Autowired
    private DataPrepAPIHelper dpah;

    @BeforeStory
    public void initializeStory() {
        initialize();
    }

    @AfterStory
    public void disposeStory() {
        context().getDatasetById().forEach((key, value) -> deleteDataSet(value));
        dispose();
    }

    @When("I upload the dataset $filename with name $name")
    public void uploadDataset(String filename, String name) throws java.io.IOException {
        String datasetName = name + "_" + LocalDateTime.now();
        Response response = dpah.uploadDataset(filename, datasetName, environment.getProperty("run.environment.url"));
        response.then().statusCode(200);
        String datasetId = IOUtils.toString(response.getBody().asInputStream(), true);
        context().getDatasetById().put(STORY_DATASET_UPLOADED_ID, datasetId);
        context().getDatasetById().put(STORY_DATASET_UPLOADED_NAME, datasetName);
    }

    @Then("The uploaded dataset is present in datasets list")
    public void getDataSets() {
        dpah.getDatasetList(environment.getProperty("run.environment.url"))
                .then().statusCode(200)
                .body("id", hasItems(context().getDatasetById().get(STORY_DATASET_UPLOADED_ID)));
    }

    /**
     * Delete a given dataset.
     *
     * @param dataSetId the dataset to delete.
     */
    public void deleteDataSet(String dataSetId) {
        dpah.deleteDataSet(dataSetId, environment.getProperty("run.environment.url")).then().statusCode(200);
    }
}
