package org.talend.dataprep.folder.store.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Translates dataprep {@link FolderPath} to and from {@link java.nio.file.Path file system Path}.
 */
@Component
@ConditionalOnBean(FileSystemFolderRepository.class)
public class PathsConverter {

    /**
     * Where to store the folders.
     */
    @Value("${folder.store.file.location}")
    private String foldersLocation;

    /**
     * Return the root folder where the preparations are stored.
     *
     * @return the root folder.
     */
    Path getRootFolder() {
        return Paths.get(foldersLocation);
    }

    /**
     * Transforms the absolute path of the {@Folder} storage file to its dataprep path.
     * TODO: the root part extraction should be centralized elsewhere and all the Path manipulated should be relative to the root folder.
     *
     * @param path the path to convert in String.
     * @return a path using {@link org.talend.dataprep.folder.store.FoldersRepositoriesConstants#PATH_SEPARATOR}
     */
    FolderPath toFolderPath(Path path) {
        Path relativePath = getRootFolder().relativize(path);

        String[] pathArray;
        if (StringUtils.isEmpty(relativePath.toString())) {
            pathArray = new String[] {};
        } else {
            List<String> pathNames = new ArrayList<>(relativePath.getNameCount());
            for (Path pathElement : relativePath) {
                pathNames.add(pathElement.getFileName().toString());
            }
            pathArray = pathNames.toArray(new String[pathNames.size()]);
        }
        return new FolderPath(pathArray);
    }

    Path toPath(FolderPath path) {
        return Paths.get(getRootFolder().toString(), path.serializeAsString());
    }

}
