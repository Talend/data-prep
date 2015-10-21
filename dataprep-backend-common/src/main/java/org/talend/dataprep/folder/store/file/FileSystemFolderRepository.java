package org.talend.dataprep.folder.store.file;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.google.common.collect.Lists;

@Component("folderRepository#file")
@ConditionalOnProperty(name = "folder.store", havingValue = "file")
public class FileSystemFolderRepository extends FolderRepositoryAdapter implements FolderRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemFolderRepository.class);

    /** Where to store the folders */
    @Value("${folder.store.file.location}")
    private String foldersLocation;

    private FileAttribute<Set<PosixFilePermission>> defaultFilePermissions = PosixFilePermissions.asFileAttribute( //
            Sets.newHashSet(PosixFilePermission.OWNER_EXECUTE, //
                    PosixFilePermission.OWNER_READ, //
                    PosixFilePermission.OWNER_WRITE));

    /**
     * Make sure the root folder is there.
     */
    @PostConstruct
    private void init() {
        try {
            Files.createDirectories(getRootFolder(), defaultFilePermissions);
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
        try {
            List<String> pathParts = folder.getPathParts();
            Path folderPath = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));
            Stream<Path> childStream = Files.list(folderPath);
            List<Folder> childs = new ArrayList<>();
            childStream.forEach(path -> {
                childs.add(Folder.Builder.folder().name(path.getFileName().toString()).pathParts(pathParts(path)).build());
            });
            return childs;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected List<String> pathParts(Path path) {

        Path relativePath = path.subpath(getRootFolder().getNameCount(), path.getNameCount());
        final List<String> parts = new ArrayList<>();
        relativePath.iterator().forEachRemaining(thePath -> parts.add(thePath.toString()));
        return parts;
    }

    @Override
    public Folder addFolder(Folder parent, Folder child) {
        try {
            List<String> pathParts = Lists.newArrayList(parent.getPathParts());
            pathParts.add(child.getName());
            Path path = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));

            child.setPathParts(pathParts);
            child.setId(child.getName());

            if (!Files.exists(path)) {
                Files.createDirectories(path, defaultFilePermissions);
            }

            return child;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    @Override
    public void addFolderEntry(Folder parent, FolderEntry folderEntry) {

        throw new NotImplementedException();

    }

    @Override
    public void removeFolderEntry(Folder parent, FolderEntry folderEntry) {
        throw new NotImplementedException();
    }

    @Override
    public void removeFolder(Folder folder) {
        List<String> pathParts = Lists.newArrayList(folder.getPathParts());

        Path path = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));

        try {
            FileUtils.deleteDirectory(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    @Override
    public Folder rootFolder() {

        return Folder.Builder.folder().name("").build();
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
        int number = foldersNumber(getRootFolder());
        return number;
    }

    /**
     * 
     * @param path
     * @return the folder number of a directory recursively
     */
    protected int foldersNumber(Path path) {
        try {
            int number = 0;

            if (!Files.isDirectory(path)) {
                return 0;
            }

            DirectoryStream<Path> stream = Files.newDirectoryStream(path);

            for (Path part : stream) {
                if (Files.isDirectory(part)) {
                    number++;
                    number += foldersNumber(part);
                }
            }

            return number;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
