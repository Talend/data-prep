package org.talend.dataprep.qa.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.helper.OSDataPrepAPIHelper;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.dto.FolderContent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;

@Component
public class OSFolderUtil implements FolderUtil {

    @Autowired
    protected OSDataPrepAPIHelper api;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FolderContent listPreparation(String folderName) throws IOException {
        Response response = api.listPreparation(folderName);
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        return objectMapper.readValue(content, FolderContent.class);
    }

    @Override
    public List<Folder> listFolders() throws IOException {
        Response response = api.listFolders();
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        List<Folder> folders = objectMapper.readValue(content, new TypeReference<List<Folder>>() {
        });
        return folders;
    }

    @Override
    public Folder extractFolder(String folderPath, List<Folder> folders) throws IOException {
        Optional<Folder> folderOpt = folders.stream().filter(f -> f.path.equals(folderPath)).findFirst();
        Assert.assertTrue(folderOpt.isPresent());
        return folderOpt.get();
    }
}
