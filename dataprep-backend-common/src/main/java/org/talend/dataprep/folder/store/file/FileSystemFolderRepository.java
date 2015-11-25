package org.talend.dataprep.folder.store.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

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

import com.google.common.collect.Lists;

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
            if (Files.notExists(folderPath)){
                return Collections.emptyList();
            }
            Stream<Path> childStream = Files.list(folderPath);
            List<Folder> childs = new ArrayList<>();
            childStream.parallel() //
                    .forEach(path -> {
                        if (Files.isDirectory(path)) {
                            String pathStr = pathAsString(path);
                            try {
                                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                                childs.add(Folder.Builder.folder() //
                                        .path(pathStr) //
                                        .name(extractName(pathStr)) //
                                        .modificationDate(attr.lastModifiedTime().to(TimeUnit.MILLISECONDS))//
                                        .creationDate(attr.creationTime().to(TimeUnit.MILLISECONDS)).build());
                            } catch (IOException e) {
                                throw new RuntimeException(e.getMessage(), e);
                            }
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
        return StringUtils.removeEnd( stringBuilder.toString(), "/");
    }

    @Override
    public Folder addFolder(String path) {
        try {

            if (!StringUtils.startsWith(path, "/")) {
                path = "/" + path;
            }

            List<String> pathParts = Lists.newArrayList(StringUtils.split(path, PATH_SEPARATOR));

            Path pathToCreate = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));

            if (!Files.exists(pathToCreate)) {
                Files.createDirectories(pathToCreate, defaultFilePermissions);
            }
            return Folder.Builder.folder() //
                    .path(path) //
                    .name(extractName(path)) //
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void renameFolder( String path, String newPath )
    {
        if (StringUtils.isEmpty(path) //
                || StringUtils.isEmpty(newPath) //
                || StringUtils.containsOnly(path, "/")) {
            throw new IllegalArgumentException("path cannot be empty");
        }

        String originalPath = path;
        if (!StringUtils.startsWith(originalPath, "/")) {
            originalPath = "/" + originalPath;
        }

        List<String> pathParts = Lists.newArrayList(StringUtils.split(originalPath, PATH_SEPARATOR));

        Path folderPath = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));

        pathParts = Lists.newArrayList(StringUtils.split(newPath, PATH_SEPARATOR));

        Path newFolderPath = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));

        try {
            FileUtils.moveDirectory(folderPath.toFile(), newFolderPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    @Override
    public FolderEntry addFolderEntry( FolderEntry folderEntry) {

        // we store the FolderEntry bean content as properties the file name is the name

        try {
            List<String> pathParts = Lists.newArrayList(StringUtils.split(folderEntry.getPath(), PATH_SEPARATOR));
            pathParts.add(folderEntry.id());
            Path path = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));
            // we delete it if exists
            Files.deleteIfExists(path);

            Path parentPath = path.getParent();
            // check parent path first
            if (Files.notExists( parentPath )){
                Files.createDirectories( parentPath );
            }

            path = Files.createFile(path);

            Properties properties = new Properties();

            properties.setProperty("contentType", folderEntry.getContentType());
            properties.setProperty("contentId", folderEntry.getContentId());
            properties.setProperty("id", folderEntry.getId());

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
    public void removeFolderEntry(String folderPath, String contentId, String contentType) {

        try {
            List<String> pathParts = Lists.newArrayList(StringUtils.split(folderPath, PATH_SEPARATOR));
            Path path = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));

            Files.list( path ) //
                .filter( pathFound -> !Files.isDirectory(pathFound)) //
                .parallel() //
                .forEach( pathFile -> {
                    try {
                        try (InputStream inputStream = Files.newInputStream(pathFile)) {
                            Properties properties = new Properties();
                            properties.load(inputStream);
                            if (StringUtils.equalsIgnoreCase(properties.getProperty("contentType"), //
                                                             contentType) && //
                                StringUtils.equalsIgnoreCase(properties.getProperty("contentId"), //
                                                             contentId) ) {
                                Files.delete( pathFile );
                            }
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                });

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

        if (Files.notExists(path)){
            return Collections.emptyList();
        }

        try {
            List<FolderEntry> folderEntries = new ArrayList<>();
            Files.list(path) //
                    .filter(pathFound -> !Files.isDirectory(pathFound)) //
                    .parallel() //
                    .forEach(pathFile -> {
                            try {
                                try (InputStream inputStream = Files.newInputStream(pathFile)) {
                                    Properties properties = new Properties();
                                    properties.load(inputStream);
                                    if (StringUtils.equalsIgnoreCase(properties.getProperty("contentType"), //
                                            contentType)) {

                                        FolderEntry folderEntry =  new FolderEntry(  );
                                        folderEntry.setId( properties.getProperty("id") );
                                        folderEntry.setPath( folder );
                                        folderEntry.setContentType( contentType );
                                        folderEntry.setContentId( properties.getProperty("contentId") );
                                        folderEntries.add(folderEntry);
                                    }
                                }

                            } catch (IOException e) {
                                throw new RuntimeException(e.getMessage(), e);
                            }
                    });

            return folderEntries;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Iterable<FolderEntry> findFolderEntries(String contentId, String contentType) {
        Set<FolderEntry> folderEntries = new HashSet<>();

        try {
            Files.walkFileTree(getRootFolder(), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    final AtomicBoolean filesFound = new AtomicBoolean( false );

                    Files.list( dir ).parallel().forEach( path -> {
                        filesFound.set(true); } );

                    return filesFound.get()? FileVisitResult.CONTINUE: FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try (InputStream inputStream = Files.newInputStream(file)) {
                        Properties properties = new Properties();
                        properties.load(inputStream);
                        if (StringUtils.equals( properties.getProperty( "contentId" ), contentId ) && //
                            StringUtils.equals( properties.getProperty( "contentType" ), contentType )){
                            folderEntries.add( new FolderEntry( contentType,  contentId, //
                                                                getRootFolder().relativize( file.getParent()).toString()));
                        }
                    }
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

        return folderEntries;
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
    public Iterable<Folder> allFolder()
    {
        
        Set<Folder> folders = new HashSet<>();

        try {
            Files.walkFileTree(getRootFolder(), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    final AtomicBoolean filesFound = new AtomicBoolean(false);

                    Files.list(dir).parallel().forEach(path -> {
                        if (Files.isDirectory(path)) {
                            String pathStr = pathAsString(path);
                            folders.add(Folder.Builder.folder() //
                                    .path(pathStr) //
                                    .name(extractName(pathStr)) //
                                    .build());
                            filesFound.set(true);
                        }
                    });

                    return filesFound.get() ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
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
        
        return folders;
    }


    @Override
    public void copyFolderEntry( FolderEntry folderEntry, String destinationPath )
    {
        Path path = Paths.get(getRootFolder().toString(), StringUtils.split(destinationPath, PATH_SEPARATOR));

        if (Files.notExists( path )){
            throw new IllegalArgumentException( "destinationPath doesn't exists" );
        }

        Path entry = Paths.get( getRootFolder().toString(), StringUtils.split(folderEntry.getPath(), PATH_SEPARATOR) );
        Path originFile = Paths.get( entry.toString(), folderEntry.id() );

        if (Files.notExists( originFile )){
            throw new IllegalArgumentException( "entry doesn't exists" );
        }

        try {

            Path destinationFile = Paths.get( path.toString(), folderEntry.id() );
            Files.copy( originFile, destinationFile );
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }


    }

    @Override
    public void moveFolderEntry( FolderEntry folderEntry, String destinationPath )
    {
        Path path = Paths.get(getRootFolder().toString(), StringUtils.split(destinationPath, PATH_SEPARATOR));

        if (Files.notExists( path )){
            throw new IllegalArgumentException( "destinationPath doesn't exists" );
        }

        Path entry = Paths.get( getRootFolder().toString(), StringUtils.split(folderEntry.getPath(), PATH_SEPARATOR) );
        Path originFile = Paths.get( entry.toString(), folderEntry.id() );

        if (Files.notExists( originFile )){
            throw new IllegalArgumentException( "entry doesn't exists" );
        }

        try {

            Path destinationFile = Paths.get( path.toString(), folderEntry.id() );
            Files.move( originFile, destinationFile );
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public int size() {
        int number = foldersNumber( getRootFolder() );
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
