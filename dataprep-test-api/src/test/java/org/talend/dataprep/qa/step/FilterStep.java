package org.talend.dataprep.qa.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.response.Response;
import cucumber.api.java.en.Then;

import org.junit.Assert;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.DatasetContent;
import org.talend.dataprep.qa.dto.PreparationContent;

import java.io.*;
import java.util.List;

import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

public class FilterStep extends DataPrepStep {

    @Then("^I check the records of the dataset \"(.*)\" after applying a filter using the following TQL \"(.*)\" equals \"(.*)\" file$")
    public void iCheckTheRecordsOfTheDataset(String datasetName, String tql, String expectedResult) throws Exception {
        String datasetId = context.getDatasetId(suffixName(datasetName));
        Response response = api.getDataset(datasetId, tql);
        response.then().statusCode(200);

        List<Object> datasetFilteredRecords = response.as(DatasetContent.class).records;

        InputStream expectedFileStream = DataPrepStep.class.getResourceAsStream(expectedResult);
        List<Object> expected = objectMapper.readValue(expectedFileStream, new TypeReference<List<Object>>() {});

        Assert.assertTrue(datasetFilteredRecords.containsAll(expected));
    }

    @Then("^I check the records of the preparation \"(.*)\" after applying a filter using the following TQL \"(.*)\" equals \"(.*)\" file$")
    public void iCheckTheRecordsOfThePreparation(String prepName, String tql, String expectedResult) throws Exception {
        String preparationId = context.getPreparationId(suffixName(prepName));
        Response response = api.getPreparationContent(preparationId, "head", "HEAD", tql);
        response.then().statusCode(200);

        List<Object> prepFilteredRecords = response.as(PreparationContent.class).records;

        InputStream expectedFileStream = DataPrepStep.class.getResourceAsStream(expectedResult);
        List<Object> expected = objectMapper.readValue(expectedFileStream, new TypeReference<List<Object>>() {});

        Assert.assertTrue(prepFilteredRecords.containsAll(expected));
    }
}
