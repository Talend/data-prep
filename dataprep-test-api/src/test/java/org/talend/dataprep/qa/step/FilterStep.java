package org.talend.dataprep.qa.step;

import static org.talend.dataprep.qa.config.FeatureContext.suffixName;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.FILTER;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.ContentMetadataColumn;
import org.talend.dataprep.qa.dto.DatasetContent;
import org.talend.dataprep.qa.dto.PreparationContent;

import com.jayway.restassured.response.Response;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;

public class FilterStep extends DataPrepStep {

    @Then("^After applying the filter \"(.*)\", the characteristics of the dataset \"(.*)\" match:$")
    public void afterApplyingFilterThenDatasetCharacteristicsMatch(String tql, String datasetName, DataTable dataTable)
            throws Exception {
        checkDatasetCharacteristics(tql, datasetName, dataTable);
    }

    private void checkDatasetCharacteristics(String tql, String datasetName, DataTable dataTable) throws Exception {
        String datasetId = context.getDatasetId(suffixName(datasetName));
        Response response = api.getDataset(datasetId, tql);
        response.then().statusCode(200);
        DatasetContent dataset = response.as(DatasetContent.class);

        Map<String, String> expected = dataTable.asMap(String.class, String.class);
        checkRecords(dataset.records, expected.get("records"));
        checkQualityPerColumn(dataset.metadata.columns, expected.get("quality"));
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
            Map<String, Integer> expectedQuality = expectedColumn.quality;
            Map<String, Integer> quality = column.quality;
            Assert.assertEquals(expectedQuality.get("valid"), quality.get("valid"));
            Assert.assertEquals(expectedQuality.get("empty"), quality.get("empty"));
            Assert.assertEquals(expectedQuality.get("invalid"), quality.get("invalid"));
        }
    }

    @Then("^The step \"(.*)\" is applied with the filter \"(.*)\"$")
    public void theStepIsAppliedWithTheFilter(String step, String filter) {
        Action prepStep = context.getAction(step);
        Assert.assertEquals(filter, prepStep.parameters.get(FILTER.getKey()));
    }

    @Then("^After removing all filters, the characteristics of the dataset \"(.*)\" match:$")
    public void afterRemovingAllFiltersTheCharacteristicsOfTheDatasetMatch(String datasetName, DataTable dataTable)
            throws Exception {
        checkDatasetCharacteristics(null, datasetName, dataTable);
    }

    @Then("^After applying the filter \"(.*)\", the content of the preparation \"(.*)\" matches:$")
    public void afterApplyingTheFilterTheContentOfThePreparationMatches(String tql, String prepName,
            DataTable dataTable) throws Exception {
        checkPreparationContent(tql, prepName, dataTable);
    }

    private void checkPreparationContent(String tql, String prepName, DataTable dataTable) throws Exception {
        String preparationId = context.getPreparationId(suffixName(prepName));
        Response response = api.getPreparationContent(preparationId, "head", "HEAD", tql);
        response.then().statusCode(200);

        PreparationContent preparation = response.as(PreparationContent.class);

        checkContent(preparation, dataTable);
    }

    public void checkContent(PreparationContent preparation, DataTable dataTable) throws Exception {
        Map<String, String> expected = dataTable.asMap(String.class, String.class);
        checkRecords(preparation.records, expected.get("records"));
        checkQualityPerColumn(preparation.metadata.columns, expected.get("quality"));
    }

    @Then("^After removing all filters, the content of the preparation \"(.*)\" matches:$")
    public void afterRemovingAllFiltersTheContentOfThePreparationMatches(String prepName, DataTable dataTable)
            throws Exception {
        checkPreparationContent(null, prepName, dataTable);
    }

}
