package org.talend.dataprep.qa.step;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;

/**
 * Store steps related to folders.
 */
public class FolderStep extends DataPrepStep {

    public static final String FOLDER_NAME = "folderName";

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FolderStep.class);

    @Then("^I create a folder with the following parameters :$")
    public void createFolder(@NotNull DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String parentFolderName = params.get(ORIGIN);
        String folder = params.get(FOLDER_NAME);

        List<Folder> folders = listFolders();
        Folder parentFolder = extractFolder(parentFolderName, folders);

        Response response = api.createFolder(parentFolder.id, folder);
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        Folder createdFolder = objectMapper.readValue(content, Folder.class);
        Assert.assertEquals(createdFolder.path, "/" + folder);

        Set<String> existingFolders = folders.stream() //
                .map(f -> f.path.substring(1)) //
                .filter(f -> !f.isEmpty()) //
                .collect(Collectors.toSet());

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
    public List<Folder> listFolders() throws IOException {
        Response response = api.listFolders();
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        List<Folder> folders = objectMapper.readValue(content, new TypeReference<List<Folder>>() {
        });
        return folders;
    }

    /**
     * Search a {@link Folder} full path in a {@link List} of {@link Folder} and return the corresponding {@link Folder}.
     *
     * @param folderPath the folder full path.
     * @param folders the {@link List} of {@link Folder}
     * @return a {@link Folder} or <code>null</code> if the folder doesn't exist.
     */
    public Folder extractFolder(String folderPath, List<Folder> folders) throws IOException {
        Optional<Folder> folderOpt = folders.stream().filter(f -> f.path.equals(folderPath)).findFirst();
        Assert.assertTrue(folderOpt.isPresent());
        return folderOpt.get();
    }
}
