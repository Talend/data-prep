package org.talend.dataprep.qa.util.folder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.helper.OSDataPrepAPIHelper;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.dto.FolderContent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Component
public class OSFolderUtil implements FolderUtil {

    @Autowired
    protected OSDataPrepAPIHelper api;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FolderContent listPreparation(String folderName) throws IOException {
        Response response = api.listPreparations(folderName);
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        return objectMapper.readValue(content, FolderContent.class);
    }

    @Override
    public List<Folder> listFolders() throws IOException {
        Response response = api.listFolders();
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        return objectMapper.readValue(content, new TypeReference<List<Folder>>() {
        });
    }

    @Override
    public Folder extractFolder(String folderPath, Collection<Folder> folders) throws IOException {
        Optional<Folder> folderOpt = folders.stream().filter(f -> f.path.equals(folderPath)).findFirst();
        return folderOpt.orElse(null);
    }

    @Override
    public Folder searchFolder(String folderPath) throws IOException {
        List<Folder> folders = listFolders();
        return extractFolder(folderPath, folders);
    }

    @Override
    public Response deleteFolder(Folder folder) {
        String folderPath = api.encode64(folder.path);
        return api.deleteFolder(folderPath);
    }

    @Override
    public SortedSet<Folder> sortFolders(Set<Folder> folders) {
        SortedSet<Folder> sortedFolders = new TreeSet<>((o1, o2) -> {
            if (o1 == null && o2 == null)
                return 0;
            if (o1 == null || o1.path == null)
                return 1;
            if (o2 == null || o2.path == null)
                return -1;

            if (o1.path.length() == o2.path.length())
                return o1.path.compareTo(o2.path);

            return ((Integer) o1.path.length()).compareTo(o2.path.length());
        });
        sortedFolders.addAll(folders);
        return sortedFolders;
    }

    @Override
    public SortedSet<Folder> getEmptyReverseSortedSet() {
        return new TreeSet<>((o1, o2) -> {
            // reverse order : the longer string is the first one.
            if (o1 == null && o2 == null)
                return 0;
            if (o1 == null || o1.path == null)
                return 1;
            if (o2 == null || o2.path == null)
                return -1;

            if (o1.path.length() == o2.path.length())
                return o2.path.compareTo(o1.path);

            return ((Integer) o2.path.length()).compareTo(o1.path.length());
        });
    }

    @Override
    public Set<Folder> splitFolder(Folder folder, List<Folder> folders) {
        Set<Folder> ret = new HashSet<>();
        if (folder == null || folder.getPath().equals("/"))
            return ret;

        final Map<String, Folder> folderByPath = new HashMap<>(folders.size());
        folders.forEach(f -> folderByPath.put(f.getPath().substring(1), f));

        String[] folderPaths = folder.getPath().split("/");
        StringBuilder folderBuilder = new StringBuilder();
        Arrays
                .stream(folderPaths) //
                .filter(f -> !f.isEmpty() && !"/".equals(f)) //
                .forEach(f -> { //
                    if (folderBuilder.length() > 0) {
                        folderBuilder.append("/");
                    }
                    folderBuilder.append(f);
                    Folder tmpF = folderByPath.get(folderBuilder.toString());
                    if (tmpF != null) {
                        ret.add(tmpF);
                    }
                });
        return ret;
    }

    @Override
    public String getAPIFolderRepresentation(Folder folder) {
        return api.encode64(folder.getPath());
    }

}
