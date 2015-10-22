package org.talend.dataprep.folder.store.file;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
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

@Component("folderRepository#file") @ConditionalOnProperty(name = "folder.store", havingValue = "file")
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
            Path idPath = Paths.get(foldersLocation, ".id");
            createIdFile(idPath);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    /**
     * create or read .id file corresponding of folder id
     * @param path
     * @return
     * @throws IOException
     */
    protected String createIdFile(Path path) throws IOException {
        if (Files.isDirectory(path)){
            File idFile = new File( path.toFile(), ".id");
            path = idFile.toPath();
        }

        if  (!Files.exists(path)){
            Path idFile = Files.createFile(path);
            String uuid = UUID.randomUUID().toString();
            Files.write(idFile, uuid.getBytes(), StandardOpenOption.CREATE);
            return uuid;
        } else {
            return new String(Files.readAllBytes(path));
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
                try {
                    if (Files.isDirectory( path )) {
                        childs.add(Folder.Builder.folder() //
                                .name(path.getFileName().toString()) //
                                .pathParts(pathParts(path)) //
                                .id(createIdFile(path)) //
                                .build());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
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

            child.setId(createIdFile(path));

            return child;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    @Override
    public FolderEntry addFolderEntry(Folder parent, FolderEntry folderEntry) {

        // we store the FolderEntry bean content as properties the file name is the name

        try {
            List<String> pathParts = Lists.newArrayList(parent.getPathParts());
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
    public void removeFolderEntry(Folder parent, FolderEntry folderEntry) {

        try {
            List<String> pathParts = Lists.newArrayList(parent.getPathParts());
            pathParts.add(folderEntry.getName());
            Path path = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));
            // we delete it if exists
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
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
    public Folder find(String folderId) {

        final Folder result = new Folder();

        try {
            Files.walkFileTree(getRootFolder(), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (StringUtils.equalsIgnoreCase(createIdFile(dir), folderId)){
                        result.setId(folderId);
                        result.setName(dir.getFileName().toString());
                        result.setPathParts(pathParts(dir));
                        // we found the directory with id so terminate
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return result;
    }




    @Override
    public Folder rootFolder() {
        return Folder.Builder.folder().name("").build();
    }

    @Override
    public Iterable<FolderEntry> entries(Folder folder, String contentType) {
        List<String> pathParts = Lists.newArrayList(folder.getPathParts());

        Path path = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));

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
