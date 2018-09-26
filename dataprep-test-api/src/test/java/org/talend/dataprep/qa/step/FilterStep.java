package org.talend.dataprep.qa.step;

import static org.talend.dataprep.qa.config.FeatureContext.suffixName;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.FILTER;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.ContentMetadataColumn;
import org.talend.dataprep.qa.dto.DatasetContent;
import org.talend.dataprep.qa.dto.PreparationContent;
import org.talend.dataprep.qa.dto.Statistics;

import com.jayway.restassured.response.Response;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class FilterStep extends DataPrepStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterStep.class);

    @When("^I apply the filter \"(.*)\" on dataset \"(.*)\"$")
    public void applyFilterOnDataSet(String tql, String datasetName) throws Exception {
        doApplyFilterOnDataSet(tql, datasetName);
    }

    private void doApplyFilterOnDataSet(String tql, String datasetName) throws Exception {
        DatasetContent datasetContent = getDatasetContent(context.getDatasetId(suffixName(datasetName)), tql);
        context.storeObject("dataSetContent", datasetContent);
    }

    @Then("^The characteristics of the dataset \"(.*)\" match:$")
    public void checkFilterApplyedOnDataSet(String datasetName, DataTable dataTable) throws Exception {
        Map<String, String> expected = dataTable.asMap(String.class, String.class);

        DatasetContent datasetContent = (DatasetContent) context.getObject("dataSetContent");
        if (datasetContent == null) {
            datasetContent = getDatasetContent(context.getDatasetId(suffixName(datasetName)), null);
        }

        if (expected.get("records") != null) {
            checkRecords(datasetContent.records, expected.get("records"));
        }

        if (expected.get("sample_records_count") != null) {
            checkSampleRecordsCount(datasetContent.metadata.records, expected.get("sample_records_count"));
        }

        if (expected.get("quality") != null) {
            checkQualityPerColumn(datasetContent.metadata.columns, expected.get("quality"));
        }
    }

    private void checkSampleRecordsCount(String actualRecordsCount, String expectedRecordsCount) {
        if (expectedRecordsCount == null) {
            return;
        }
        Assert.assertEquals(expectedRecordsCount, actualRecordsCount);
    }

    /**
     * Returns the dataset content, once all DQ analysis are done and so all fields are up-to-date.
     *
     * @param datasetId the id of the dataset
     * @param tql       the TQL filter to apply to the dataset
     * @return the up-to-date dataset content
     */
    private DatasetContent getDatasetContent(String datasetId, String tql) throws Exception {
        Response response;
        int tries = 0;
        do {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                LOGGER.error("Thread interrupted");
            }
            response = api.getDataset(datasetId, tql);
            response.then().statusCode(200);
            tries++;
        } while (response.body().jsonPath().getList("metadata.columns[0].statistics.frequencyTable").isEmpty()
                && tries < 10);

        return response.as(DatasetContent.class);
    }

    private void checkRecords(List<Object> actualRecords, String expectedRecordsFilename) throws Exception {
        if (expectedRecordsFilename == null) {
            return;
        }
        InputStream expectedRecordsFileStream = DataPrepStep.class.getResourceAsStream(expectedRecordsFilename);
        List<Object> expectedRecords = objectMapper.readValue(expectedRecordsFileStream, DatasetContent.class).records;

        Assert.assertEquals(expectedRecords.size(), actualRecords.size());
        Assert.assertTrue(actualRecords.containsAll(expectedRecords));
    }

    private void checkQualityPerColumn(List<ContentMetadataColumn> columns, String expectedQualityFilename)
            throws Exception {
        if (expectedQualityFilename == null) {
            return;
        }
        InputStream expectedQualityFileStream = DataPrepStep.class.getResourceAsStream(expectedQualityFilename);
        List<ContentMetadataColumn> expectedQualityPerColumn =
                objectMapper.readValue(expectedQualityFileStream, DatasetContent.class).metadata.columns;

        Assert.assertEquals(expectedQualityPerColumn.size(), columns.size());
        Collections.sort(columns);
        Collections.sort(expectedQualityPerColumn);
        for (int i = 0; i < expectedQualityPerColumn.size(); i++) {
            ContentMetadataColumn expectedColumn = expectedQualityPerColumn.get(i);
            ContentMetadataColumn column = columns.get(i);
            Assert.assertEquals(expectedColumn.id, column.id);
            Assert.assertEquals(expectedColumn.name, column.name);
            Assert.assertEquals(expectedColumn.type, column.type);
            Assert.assertEquals(expectedColumn.domain, column.domain);
            Map<String, Integer> expectedQuality = expectedColumn.quality;
            Statistics expectedStatistics = expectedColumn.statistics;

            Map<String, Integer> quality = column.quality;
            Assert.assertEquals(expectedQuality.get("valid"), quality.get("valid"));
            Assert.assertEquals(expectedQuality.get("empty"), quality.get("empty"));
            Assert.assertEquals(expectedQuality.get("invalid"), quality.get("invalid"));

            Statistics statistics = column.statistics;
            if (expectedStatistics != null && statistics != null) {
                Assert.assertTrue(
                        expectedStatistics.patternFrequencyTable.containsAll(statistics.patternFrequencyTable));
                Assert.assertTrue(expectedStatistics.frequencyTable.containsAll(statistics.frequencyTable));
            }
        }
    }

    @Then("^The step \"(.*)\" is applied with the filter \"(.*)\"$")
    public void theStepIsAppliedWithTheFilter(String step, String filter) {
        Action prepStep = context.getAction(step);
        Assert.assertEquals(filter, prepStep.parameters.get(FILTER.getKey()));
    }

    @When("^I remove all filters on dataset \"(.*)\"$")
    public void removeFilter(String datasetName) throws Exception {
        doApplyFilterOnDataSet(null, datasetName);
    }

    @When("^I apply the filter \"(.*)\" on the preparation \"(.*)\"$")
    public void applyFilterOnPreparation(String tql, String preparationName) throws Exception {
        doApplyFilterOnPreparation(tql, preparationName);
    }

    private void doApplyFilterOnPreparation(String tql, String preparationName) throws IOException {
        PreparationContent preparationContent = getPreparationContent(preparationName, tql);
        context.storeObject("preparationContent", preparationContent);
    }

    private PreparationContent getPreparationContent(String preparationName, String tql) throws IOException {
        String preparationId = context.getPreparationId(suffixName(preparationName));
        Response response = api.getPreparationContent(preparationId, "head", "HEAD", tql);
        response.then().statusCode(200);

        return response.as(PreparationContent.class);
    }

    @Then("^The characteristics of the preparation \"(.*)\" match:$")
    public void checkFilterAppliedOnPreparation(String preparationName, DataTable dataTable) throws Exception {
        PreparationContent preparationContent = (PreparationContent) context.getObject("preparationContent");
        if (preparationContent == null) {
            preparationContent = getPreparationContent(preparationName, null);
        }
        checkContent(preparationContent, dataTable);
    }

    public void checkContent(PreparationContent preparation, DataTable dataTable) throws Exception {
        Map<String, String> expected = dataTable.asMap(String.class, String.class);
        checkRecords(preparation.records, expected.get("records"));
        checkQualityPerColumn(preparation.metadata.columns, expected.get("quality"));
        checkSampleRecordsCount(preparation.metadata.records, expected.get("sample_records_count"));
    }

    @Then("^I remove all filters on preparation \"(.*)\"$")
    public void removeAllFiltersOnPreparation(String preparationName) throws Exception {
        doApplyFilterOnPreparation(null, preparationName);
    }
}
