package org.talend.dataprep.qa.step;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.response.Response;

import cucumber.api.java.en.Then;

/**
 * Store steps related to folders.
 */
public class FolderStep extends DataPrepStep {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FolderStep.class);

    @Then("^I create a folder \"(.*)\" under the root folder$")
    public void createFolder(@NotNull final String folder) throws IOException {
        Set<String> existingFolders = listFolders();
        Response response = api.createFolder(api.getHomeFolder(), folder);
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        Folder createdFolder = objectMapper.readValue(content, Folder.class);
        Assert.assertEquals(createdFolder.path, "/" + folder);

        Set<String> splittedFolders = util.splitFolder(folder);
        splittedFolders.stream() //
                .filter(sf -> !existingFolders.contains(sf)) //
                .forEach(sf -> context.storeFolder(sf));
    }

    /**
     * List all folders in Data-prep OS.
     *
     * @return a {@link Set} of folders.
     */
    public Set<String> listFolders() throws IOException {
        Response response = api.listFolders();
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        List<Folder> folders = objectMapper.readValue(content, new TypeReference<List<Folder>>() {
        });
        return folders.stream() //
                .map(f -> f.path.substring(1)) //
                .filter(f -> !f.isEmpty()) //
                .collect(Collectors.toSet());
    }

}
