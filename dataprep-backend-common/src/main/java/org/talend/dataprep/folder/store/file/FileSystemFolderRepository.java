package org.talend.dataprep.folder.store.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.folder.store.FolderRepositoryAdapter;

@Component
@ConditionalOnProperty(name = "folder.store", havingValue = "file")
public class FileSystemFolderRepository extends FolderRepositoryAdapter implements FolderRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemFolderRepository.class);

    /** Where to store the folders */
    @Value("${folder.store.file.location}")
    private String foldersLocation;

    /**
     * Make sure the root folder is there.
     */
    @PostConstruct
    private void init() {
        try {
            Files.createDirectories(getRootFolder(),
                    PosixFilePermissions.asFileAttribute( //
                            Sets.newHashSet(PosixFilePermission.GROUP_READ,
                                    //
                                    PosixFilePermission.GROUP_WRITE)));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    /**
     * Return the root folder where the preparations are stored.
     *
     * @return the root folder.
     */
    private Path getRootFolder() {
        return Paths.get(foldersLocation);
    }

    @Override
    public Iterable<Folder> childs(Folder folder) {
        List<String> pathParts = folder.getPathParts();
        Path folderPath = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));
        Stream<Path> childStream = Files.list(folderPath);
        List<Folder> childs = new ArrayList<>();
        childStream.forEach(path -> {
            childs.add(Folder.Builder.folder().name(path.getFileName().toString()).pathParts(path.iterator()).build());
        });
        return childs;
    }

    protected List<>

    @Override
    public Folder addFolder(Folder parent, Folder child) {
        return null;
    }

    @Override
    public void addFolderEntry(Folder parent, FolderEntry folderEntry) {

    }

    @Override
    public void removeFolderEntry(Folder parent, FolderEntry folderEntry) {

    }

    @Override
    public void removeFolder(Folder folder) {

    }

    @Override
    public Folder rootFolder() {
        return null;
    }

    @Override
    public Iterable<FolderEntry> entries(Folder folder, String contentType) {
        return null;
    }

    @Override
    public void clear() {
        try {
            FileUtils.deleteDirectory(getRootFolder().toFile());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public int size() {
        return 0;
    }
}
