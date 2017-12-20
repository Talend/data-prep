package org.talend.dataprep.qa.step;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;
import static org.talend.dataprep.qa.config.FeatureContext.JVM_HASH_IDENT;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.dto.FolderContent;
import org.talend.dataprep.qa.dto.PreparationContent;
import org.talend.dataprep.qa.dto.PreparationDetails;

import com.jayway.restassured.response.Response;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

/**
 * Step dealing with preparation
 */
public class PreparationStep extends DataPrepStep {

    public static final String DATASET_NAME = "dataSetName";

    public static final String NB_STEPS = "nbSteps";

    public static final String DESTINATION = "destination";

    /** {@link cucumber.api.DataTable} key for new preparationName value. */
    public static final String NEW_PREPARATION_NAME = "newPreparationName";

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationStep.class);

    @Given("^I create a preparation with name \"(.*)\", based on \"(.*)\" dataset$")
    public void givenICreateAPreparation(String preparationName, String datasetName) {
        LOGGER.debug("I create a preparation with name {}", preparationName);
        preparationName += JVM_HASH_IDENT;
        datasetName += JVM_HASH_IDENT;
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
        String prepId = context.getPreparationId(params.get(PREPARATION_NAME) + JVM_HASH_IDENT);
        PreparationDetails prepDet = getPreparationDetails(prepId);
        Assert.assertNotNull(prepDet);
        Assert.assertEquals(prepDet.dataset.dataSetName, params.get(DATASET_NAME) + JVM_HASH_IDENT);
        Assert.assertEquals(Integer.toString(prepDet.steps.size() - 1), params.get(NB_STEPS));
    }

    @Then("^I move the preparation \"(.*)\" with the following parameters :$")
    public void movePreparation(String preparationName, DataTable dataTable) throws IOException {
        preparationName += JVM_HASH_IDENT;
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        List<Folder> folders = folderUtil.listFolders();
        Folder originFolder = folderUtil.extractFolder(params.get(ORIGIN), folders);
        Folder destFolder = folderUtil.extractFolder(params.get(DESTINATION), folders);
        String prepId = context.getPreparationId(preparationName);
        Response response = api.movePreparation(prepId, originFolder.id, destFolder.id,
                params.get(NEW_PREPARATION_NAME) + JVM_HASH_IDENT);
        response.then().statusCode(200);
    }

    @And("^I check that the preparation \"(.*)\" exists under the folder \"(.*)\"$")
    public void checkExistPrep(String preparationName, String folder) throws IOException {
        String prepId = context.getPreparationId(preparationName + JVM_HASH_IDENT);
        FolderContent folderContent = folderUtil.listPreparation(folder);

        long nb = folderContent.preparations.stream() //
                .filter(p -> p.id.equals(prepId) //
                        && p.name.equals(preparationName + JVM_HASH_IDENT)) //
                .count();
        Assert.assertEquals(1, nb);
    }

    @Then("^I check that the content of preparation \"(.*)\" equals \"(.*)\" file which have \"(.*)\" as delimiter$")
    public void iCheckThatTheContentOfPreparationEqualsFile(String preparationName, String fileName, String delimiter)
            throws Throwable {
        preparationName += JVM_HASH_IDENT;
        String prepId = context.getPreparationId(preparationName);
        Response response = api.getPreparationContent(prepId, "head", "HEAD");
        response.then().statusCode(200);

        String content = IOUtils.toString(response.getBody().asInputStream(), UTF_8);
        PreparationContent pCont = objectMapper.readValue(content, PreparationContent.class);

        CSVParser csvData = CSVParser.parse(this.getClass().getResource(fileName), Charset.defaultCharset(),
                CSVFormat.RFC4180.withHeader());

        // we check that all data are similaire
        int index = 0;
        for (CSVRecord csvRecord : csvData.getRecords()) {

            // we split the csv on the delimiter
            String[] splitCSVLine = csvRecord.get(0).split(delimiter);

            // we get the same index data on the preparation content
            LinkedHashMap<String, String> preparationData = (LinkedHashMap<String, String>) pCont.records.get(index);
            Object[] preparationDataAsArray = preparationData.values().toArray();

            // we check that csv column and prepartion column are the same (minus 1 for the tdpId)
            Assert.assertEquals(splitCSVLine.length, preparationData.size() - 1);

            // we check that both column on each line are similar
            for (int indexCol = 0; indexCol < splitCSVLine.length; indexCol++) {
                Assert.assertEquals(splitCSVLine[indexCol], preparationDataAsArray[indexCol]);
            }

            index++;
        }

        // we check number of record. We remove 1 for the header
        Assert.assertEquals(csvData.getRecordNumber() - 1, pCont.records.size());

    }
}
