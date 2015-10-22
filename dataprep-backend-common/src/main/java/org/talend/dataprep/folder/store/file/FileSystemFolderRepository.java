package org.talend.dataprep.folder.store.file;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
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

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.stream.Stream;

@Component("folderRepository#file")
@ConditionalOnProperty(name = "folder.store", havingValue = "file", matchIfMissing = false)
public class FileSystemFolderRepository  extends FolderRepositoryAdapter implements FolderRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemFolderRepository.class);

    /**
     * Where to store the folders
     */
    @Value("${folder.store.file.location}")
    private String foldersLocation;

    private FileAttribute<Set<PosixFilePermission>> defaultFilePermissions = //
            PosixFilePermissions.asFileAttribute( //
                    Sets.newHashSet(PosixFilePermission.OWNER_EXECUTE, //
                            PosixFilePermission.OWNER_READ, //
                            PosixFilePermission.OWNER_WRITE));

    /**
     * Make sure the root folder is there.
     */
    @PostConstruct
    private void init() {
        try {
            if (!Files.exists(getRootFolder())) {
                Files.createDirectories(getRootFolder(), defaultFilePermissions);
            }
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
    public Iterable<Folder> childs(String parentPath) {
        try {
            Path folderPath = null;
            if (StringUtils.isNotEmpty(parentPath)) {
                folderPath = Paths.get(getRootFolder().toString(), StringUtils.split(parentPath, PATH_SEPARATOR));
            } else {
                folderPath = getRootFolder();
            }
            Stream<Path> childStream = Files.list(folderPath);
            List<Folder> childs = new ArrayList<>();
            childStream.forEach(path -> {
                    if (Files.isDirectory( path )) {
                        childs.add(Folder.Builder.folder() //
                                .name(path.getFileName().toString()) //
                                .path(pathAsString(path)) //
                                .build());
                    }
            });
            return childs;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     *
     * @param path
     * @return a path using {@link FolderRepository#PATH_SEPARATOR}
     */
    protected String pathAsString(Path path) {
        Path relativePath = path.subpath(getRootFolder().getNameCount(), path.getNameCount());
        final StringBuilder stringBuilder = new StringBuilder(PATH_SEPARATOR);
        relativePath.iterator().forEachRemaining(thePath -> stringBuilder.append(thePath.toString()).append(PATH_SEPARATOR));
        return stringBuilder.toString();
    }

    @Override
    public Folder addFolder(String parentPath, String child) {
        try {

            List<String> pathParts = Lists.newArrayList(StringUtils.split(parentPath, PATH_SEPARATOR));
            pathParts.addAll(Lists.newArrayList(StringUtils.split(child, PATH_SEPARATOR)));

            Path path = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));

            if (!Files.exists(path)) {
                Files.createDirectories(path, defaultFilePermissions);
            }
            return Folder.Builder.folder() //
                    .name(path.getFileName().toString()) //
                    .path(pathAsString(path)) //
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    @Override
    public FolderEntry addFolderEntry(String parent, FolderEntry folderEntry) {

        // we store the FolderEntry bean content as properties the file name is the name

        try {
            List<String> pathParts = Lists.newArrayList(StringUtils.split(parent, PATH_SEPARATOR));
            pathParts.add(folderEntry.getName());
            Path path = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));
            // we delete it if exists
            Files.deleteIfExists(path);

            path = Files.createFile(path);

            Properties properties = new Properties();

            properties.setProperty("contentClass", folderEntry.getContentClass());
            properties.setProperty("contentId", folderEntry.getContentId());
            properties.setProperty("id", UUID.randomUUID().toString());

            try (OutputStream outputStream = Files.newOutputStream(path)) {
                properties.store(outputStream, "saved");
            }
            folderEntry.setId(properties.getProperty("id"));
            return folderEntry;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void removeFolderEntry(String parent, FolderEntry folderEntry) {

        try {
            List<String> pathParts = Lists.newArrayList(StringUtils.split(parent, PATH_SEPARATOR));
            pathParts.add( folderEntry.getName());
            Path path = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));
            // we delete it if exists
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void removeFolder(String folder) {

        Path path = Paths.get(getRootFolder().toString(), StringUtils.split(folder, PATH_SEPARATOR));

        try {
            FileUtils.deleteDirectory(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @Override
    public Iterable<FolderEntry> entries(String folder, String contentType) {

        Path path = Paths.get(getRootFolder().toString(), StringUtils.split(folder, PATH_SEPARATOR));

        try {
            List<FolderEntry> folderEntries = new ArrayList<>();
            Files.list(path) //
                    .filter(pathFound -> !Files.isDirectory(pathFound)) //
                    .parallel() //
                    .forEach(pathFile -> {
                        {
                            try {
                                try (InputStream inputStream = Files.newInputStream(pathFile)) {
                                    Properties properties = new Properties();
                                    properties.load(inputStream);
                                    if (StringUtils.equalsIgnoreCase(properties.getProperty("contentClass"), //
                                            contentType)) {
                                        folderEntries.add(new FolderEntry(
                                                properties.getProperty("id"), //
                                                pathFile.getFileName().toString(), //
                                                contentType, //
                                                properties.getProperty("contentType")));
                                    }
                                }

                            } catch (IOException e) {
                                throw new RuntimeException(e.getMessage(), e);
                            }
                        }
                    });

            return folderEntries;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
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
