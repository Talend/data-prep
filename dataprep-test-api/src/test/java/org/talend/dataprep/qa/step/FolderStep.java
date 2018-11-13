package org.talend.dataprep.qa.step;

import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.Folder;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.talend.dataprep.qa.config.FeatureContext.suffixFolderName;

/**
 * Store steps related to folders.
 */
public class FolderStep extends DataPrepStep {

    private static final String FOLDER_NAME = "folderName";

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FolderStep.class);

    @Then("^I create a folder with the following parameters :$")
    public void createFolder(@NotNull DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String parentFolderName = params.get(ORIGIN);
        String folder = suffixFolderName(params.get(FOLDER_NAME));

        Folder parentFolder = folderUtil.searchFolder(parentFolderName);
        Assert.assertNotNull(parentFolder);

        Response response = api.createFolder(parentFolder.id, folder);
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        Folder createdFolder = objectMapper.readValue(content, Folder.class);
        Assert.assertEquals(createdFolder.path, "/" + folder);

        List<Folder> folders = folderUtil.listFolders();
        Set<Folder> splittedFolders = folderUtil.splitFolder(createdFolder, folders);
        context.storeFolder(splittedFolders);
    }
}
