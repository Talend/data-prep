// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.folder.store.file;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContentType;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.folder.store.FolderRepositoryAdapter;
import org.talend.dataprep.folder.store.NotEmptyFolderException;

import com.google.common.collect.Lists;

/**
 * File system folder repository implementation.
 */
@Component("folderRepository#file")
@ConditionalOnProperty(name = "folder.store", havingValue = "file")
public class FileSystemFolderRepository extends FolderRepositoryAdapter {

    /**
     * Where to store the folders.
     */
    @Value("${folder.store.file.location}")
    private String foldersLocation;

    /**
     * Make sure the root folder is there.
     */
    @PostConstruct
    private void init() {
        try {
            Path rootPath = getRootFolder();
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
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

    /**
     * @see FolderRepository#exists(String)
     */
    @Override
    public boolean exists(String path) {
        final Path folderPath = Paths.get(getRootFolder().toString(), StringUtils.split(cleanPath(path), PATH_SEPARATOR));
        return Files.exists(folderPath);
    }

    /**
     * @see FolderRepository#children(String)
     */
    @Override
    public Iterable<Folder> children(String parentPath) {
        try {
            Path folderPath;
            if (StringUtils.isNotEmpty(parentPath)) {
                folderPath = Paths.get(getRootFolder().toString(), StringUtils.split(parentPath, PATH_SEPARATOR));
            } else {
                folderPath = getRootFolder();
            }
            if (Files.notExists(folderPath)) {
                return emptyList();
            }
            try (Stream<Path> childrenStream = Files.list(folderPath)) {

                List<Folder> children = new ArrayList<>();
                childrenStream.forEach(path -> { //
                    if (Files.isDirectory(path)) {
                        String pathStr = pathAsString(path);
                        try {
                            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                            Folder child = new Folder(pathStr, extractName(pathStr));
                            child.setLastModificationDate(attr.lastModifiedTime().to(TimeUnit.MILLISECONDS));
                            child.setCreationDate(attr.creationTime().to(TimeUnit.MILLISECONDS));
                            children.add(child);
                        } catch (IOException e) {
                            throw new TDPException(UNABLE_TO_LIST_FOLDER_CHILDREN, e, build().put("path", parentPath));
                        }
                    }
                });
                return children;
            }
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_LIST_FOLDER_CHILDREN, e, build().put("path", parentPath));
        }
    }

    /**
     * @param path the path to convert in String.
     * @return a path using {@link FolderRepositoryAdapter#PATH_SEPARATOR}
     */
    private String pathAsString(Path path) {
        Path relativePath;

        // take care of the root folder case to prevent IllegalArgumentException
        if (getRootFolder().getNameCount() == path.getNameCount()) {
            return PATH_SEPARATOR.toString();
        }
        else {
            relativePath = path.subpath(getRootFolder().getNameCount(), path.getNameCount());
        }

        final StringBuilder stringBuilder = new StringBuilder(String.valueOf(PATH_SEPARATOR));
        relativePath.iterator().forEachRemaining(thePath -> stringBuilder.append(thePath.toString()).append(PATH_SEPARATOR));
        return StringUtils.removeEnd(stringBuilder.toString(), "/");
    }

    /**
     * @see FolderRepository#addFolder(String)
     */
    @Override
    public Folder addFolder(String givenPath) {
        try {

            String path = givenPath;
            if (!StringUtils.startsWith(path, "/")) {
                path = "/" + path;
            }

            List<String> pathParts = Lists.newArrayList(StringUtils.split(path, PATH_SEPARATOR));

            Path pathToCreate = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));

            if (!Files.exists(pathToCreate)) {
                Files.createDirectories(pathToCreate);
            }
            FileInfo fileInfo = FileInfo.create(pathToCreate);
            return fileInfo != null
                    ? new Folder(path, extractName(path), fileInfo.getCreationDate(), fileInfo.getLastModificationDate())
                    : new Folder(path, extractName(path));

        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_ADD_FOLDER, e, build().put("path", givenPath));
        }
    }

    /**
     * @see FolderRepository#renameFolder(String, String)
     */
    @Override
    public void renameFolder(String path, String newPath) {
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
            throw new TDPException(UNABLE_TO_RENAME_FOLDER, e, build().put("path", path));
        }

    }

    /**
     * @see FolderRepository#addFolderEntry(FolderEntry, String)
     */
    @Override
    public FolderEntry addFolderEntry(FolderEntry folderEntry, String entryPath) {

        // we store the FolderEntry bean content as properties the file name is the name
        try {
            List<String> pathParts = Lists.newArrayList(StringUtils.split(entryPath, PATH_SEPARATOR));
            pathParts.add(buildFileName(folderEntry));
            Path path = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));
            // we delete it if exists
            Files.deleteIfExists(path);

            Path parentPath = path.getParent();
            // check parent path first
            if (Files.notExists(parentPath)) {
                Files.createDirectories(parentPath);
            }

            path = Files.createFile(path);
            folderEntry.setFolderId(cleanPath(entryPath));

            // use java Properties to save the files
            Properties properties = new Properties();
            properties.setProperty("contentType", folderEntry.getContentType().toString());
            properties.setProperty("contentId", folderEntry.getContentId());
            properties.setProperty("folderId", folderEntry.getFolderId());
            try (OutputStream outputStream = Files.newOutputStream(path)) {
                properties.store(outputStream, "saved");
            }

            return folderEntry;
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_ADD_FOLDER_ENTRY, e, build().put("path", entryPath));
        }
    }

    /**
     * @see FolderRepository#removeFolderEntry(String, String, FolderContentType)
     */
    @Override
    public void removeFolderEntry(String folderPath, String contentId, FolderContentType contentType) {

        if (contentType == null) {
            throw new IllegalArgumentException("The content type of the folder entry to be removed cannot be null.");
        }

        try {
            List<String> pathParts = Lists.newArrayList(StringUtils.split(folderPath, PATH_SEPARATOR));
            Path path = Paths.get(getRootFolder().toString(), pathParts.toArray(new String[pathParts.size()]));

            try (Stream<Path> paths = Files.list(path)) {
                paths.filter(pathFound -> !Files.isDirectory(pathFound)) //
                        .forEach(pathFile -> {
                            try (InputStream inputStream = Files.newInputStream(pathFile)) {
                                Properties properties = new Properties();
                                properties.load(inputStream);
                                if (contentType.equals(FolderContentType.fromName(properties.getProperty("contentType"))) && //
                                        StringUtils.equalsIgnoreCase(properties.getProperty("contentId"), //
                                                contentId)) {
                                    Files.delete(pathFile);
                                }
                            } catch (IOException e) {
                                throw new TDPException(UNABLE_TO_REMOVE_FOLDER_ENTRY, e, build().put("path", path));
                            }
                        });
            }
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_REMOVE_FOLDER_ENTRY, e, build().put("path", folderPath));
        }
    }

    /**
     * @see FolderRepository#removeFolder(String)
     */
    @Override
    public void removeFolder(String folder) throws NotEmptyFolderException {

        final Path path = Paths.get(getRootFolder().toString(), StringUtils.split(folder, PATH_SEPARATOR));
        final List<FolderEntry> folderEntries = new ArrayList<>();

        final FoldersConsumer foldersConsumer = new FoldersConsumer() {

            @Override
            public Collection<Folder> getFolders() {
                return emptyList();
            }

            @Override
            public Collection<FolderEntry> getFolderEntries() {
                return folderEntries;
            }

            @Override
            public void accept(Path path) {
                if (!Files.isDirectory(path)) {
                    // so we have a folderEntry here
                    // try to read it
                    try {
                        try (InputStream inputStream = Files.newInputStream(path)) {
                            Properties properties = new Properties();
                            properties.load(inputStream);

                            FolderEntry folderEntry = new FolderEntry();
                            folderEntry.setFolderId(path.getParent().toString());
                            folderEntry.setContentType(FolderContentType.fromName(properties.getProperty("contentType")));
                            folderEntry.setContentId(properties.getProperty("contentId"));
                            folderEntries.add(folderEntry);
                        }
                    } catch (IOException e) {
                        throw new TDPException(UNABLE_TO_READ_FOLDER_ENTRY, e, build().put("path", path).put("type", "unkown"));
                    }
                }
            }
        };

        visitFolders(foldersConsumer, path);

        if (!foldersConsumer.getFolderEntries().isEmpty()) {
            throw new NotEmptyFolderException("The folder or a child contains data");
        }

        try {
            FileUtils.deleteDirectory(path.toFile());
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_DELETE_FOLDER, e, build().put("path", path));
        }
    }

    /**
     * @see FolderRepository#entries(String, FolderContentType)
     */    @Override
    public Iterable<FolderEntry> entries(String folder, FolderContentType contentType) {

        Path path = Paths.get(getRootFolder().toString(), StringUtils.split(folder, PATH_SEPARATOR));

        if (Files.notExists(path)) {
            return emptyList();
        }

        try {
            try (Stream<Path> paths = Files.list(path)) {
                List<FolderEntry> folderEntries = new ArrayList<>();
                paths.filter(pathFound -> !Files.isDirectory(pathFound)) //
                        .forEach(pathFile -> {
                            try (InputStream inputStream = Files.newInputStream(pathFile)) {
                                Properties properties = new Properties();
                                properties.load(inputStream);
                                if (contentType.equals(FolderContentType.fromName(properties.getProperty("contentType")))) {

                                    FolderEntry folderEntry = new FolderEntry();
                                    folderEntry.setFolderId(properties.getProperty("folderId"));
                                    folderEntry.setContentType(contentType);
                                    folderEntry.setContentId(properties.getProperty("contentId"));
                                    folderEntries.add(folderEntry);
                                }
                            } catch (IOException e) {
                                throw new TDPException(UNABLE_TO_READ_FOLDER_ENTRY, e,
                                        build().put("path", path).put("type", contentType));
                            }
                        });

                return folderEntries;
            }
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_LIST_FOLDER_ENTRIES, e, build().put("path", path).put("type", contentType));
        }
    }

    /**
     * @see FolderRepository#findFolderEntries(String, FolderContentType)
     */
    @Override
    public Iterable<FolderEntry> findFolderEntries(String contentId, FolderContentType contentType) {
        Set<FolderEntry> folderEntries = new HashSet<>();

        try {
            Files.walkFileTree(getRootFolder(), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    final AtomicBoolean filesFound = new AtomicBoolean(false);

                    try (Stream<Path> paths = Files.list(dir)) {
                        paths.forEach(path -> filesFound.set(true));
                    }

                    return filesFound.get() ? CONTINUE : FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try (InputStream inputStream = Files.newInputStream(file)) {
                        Properties properties = new Properties();
                        properties.load(inputStream);
                        if (StringUtils.equals(properties.getProperty("contentId"), contentId) && //
                                contentType.equals(FolderContentType.fromName(properties.getProperty("contentType")))) {
                            final FolderEntry entry = new FolderEntry(contentType, contentId);
                            Path parent = file.getParent();
                            String folderId = parent.equals(getRootFolder()) ? "" : pathAsString(parent);
                            entry.setFolderId(folderId);
                            folderEntries.add(entry);
                        }
                    }
                    return CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return folderEntries;
    }

    /**
     * @see FolderRepository#clear()
     */
    @Override
    public void clear() {
        try {
            FileUtils.deleteDirectory(getRootFolder().toFile());
            init();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @see FolderRepository#allFolder()
     */
    @Override
    public Iterable<Folder> allFolder() {

        Set<Folder> folders = new HashSet<>();

        FoldersConsumer foldersConsumer = new FoldersConsumer() {

            @Override
            public Collection<Folder> getFolders() {
                return folders;
            }

            @Override
            public Collection<FolderEntry> getFolderEntries() {
                return emptyList();
            }

            @Override
            public void accept(Path path) {
                if (Files.isDirectory(path)) {
                    FileInfo fileInfo = FileInfo.create(path);
                    String pathStr = pathAsString(path);
                    Folder folder = fileInfo != null ? new Folder(pathStr, extractName(pathStr), fileInfo.getCreationDate(),
                            fileInfo.getLastModificationDate()) : new Folder(pathStr, extractName(pathStr));
                    folders.add(folder);
                }
            }
        };

        visitFolders(foldersConsumer, getRootFolder());

        return folders;
    }

    /**
     * @see FolderRepository#searchFolders(String)
     */
    @Override
    public Iterable<Folder> searchFolders(String queryString) {
        Set<Folder> folders = new HashSet<>();

        FoldersConsumer foldersConsumer = new FoldersConsumer() {

            @Override
            public Collection<Folder> getFolders() {
                return folders;
            }

            @Override
            public Collection<FolderEntry> getFolderEntries() {
                return emptyList();
            }

            @Override
            public void accept(Path path) {
                if (Files.isDirectory(path)) {
                    String pathStr = pathAsString(path);
                    String pathName = extractName(pathStr);
                    if (StringUtils.containsIgnoreCase(pathName, queryString)) {
                        FileInfo fileInfo = FileInfo.create(path);
                        Folder folder = fileInfo != null
                                ? new Folder(pathStr, pathName, fileInfo.getCreationDate(), fileInfo.getLastModificationDate())
                                : new Folder(pathStr, pathName);
                        folders.add(folder);
                    }
                }
            }
        };

        visitFolders(foldersConsumer, getRootFolder());

        return folders;
    }

    private void visitFolders(final FoldersConsumer foldersConsumer, final Path startFolder) {

        try {
            Files.walkFileTree(startFolder, new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    try (Stream<Path> paths = Files.list(dir)) {
                        paths.forEach(foldersConsumer);
                    }
                    return CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    return CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private interface FoldersConsumer extends Consumer<Path> {

        Collection<Folder> getFolders();

        Collection<FolderEntry> getFolderEntries();
    }

    private String buildFileName(FolderEntry folderEntry) {
        return folderEntry.getContentType().toString() + '@' + folderEntry.getContentId();
    }

    /**
     * @see FolderRepository#copyFolderEntry(FolderEntry, String)
     */
    @Override
    public void copyFolderEntry(FolderEntry folderEntry, String destinationPath) {
        FolderEntry cloned = new FolderEntry(folderEntry.getContentType(), folderEntry.getContentId());
        String targetPath = cleanPath(destinationPath);
        cloned.setFolderId(targetPath);
        addFolderEntry(cloned, targetPath);
    }

    /**
     * @see FolderRepository#moveFolderEntry(FolderEntry, String, String)
     */
    @Override
    public void moveFolderEntry(FolderEntry folderEntry, String sourcePath, String destinationPath) {
        Path path = Paths.get(getRootFolder().toString(), StringUtils.split(destinationPath, PATH_SEPARATOR));

        if (Files.notExists(path)) {
            throw new IllegalArgumentException("destinationPath doesn't exists");
        }

        Path entry = Paths.get(getRootFolder().toString(), StringUtils.split(sourcePath, PATH_SEPARATOR));
        Path originFile = Paths.get(entry.toString(), buildFileName(folderEntry));

        if (Files.notExists(originFile)) {
            throw new IllegalArgumentException("entry doesn't exists");
        }

        try {
            Path destinationFile = Paths.get(path.toString(), buildFileName(folderEntry));
            Files.move(originFile, destinationFile);
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_MOVE_FOLDER_ENTRY, e);
        }
    }

    /**
     * @see FolderRepository#locateEntry(String, FolderContentType)
     */
    @Override
    public Folder locateEntry(String contentId, FolderContentType type) {

        FolderLocator locator = new FolderLocator(contentId, type);
        try {
            Files.walkFileTree(getRootFolder(), locator);
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return locator.getResult();
    }


    /**
     * @see FolderRepository#size()
     */
    @Override
    public long size() {
        return foldersNumber(getRootFolder());
    }

    /**
     * @param path the path to number folders from.
     * @return the folder number of a directory recursively
     */
    private int foldersNumber(Path path) {

        if (!Files.isDirectory(path)) {
            return 0;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {

            int number = 0;

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

    private static class FileInfo {

        /**
         * This class' logger.
         */
        private static final Logger LOG = getLogger(FileInfo.class);

        private final long lastModificationDate;

        private final long creationDate;

        FileInfo(long lastModificationDate, long creationDate) {
            this.lastModificationDate = lastModificationDate;
            this.creationDate = creationDate;
        }

        public long getLastModificationDate() {
            return lastModificationDate;
        }

        public long getCreationDate() {
            return creationDate;
        }

        public static FileInfo create(Path path) {
            FileInfo result = null;
            BasicFileAttributes attributes = null;
            try {
                attributes = Files.readAttributes(path, BasicFileAttributes.class);
            } catch (IOException e) {
                LOG.debug("cannot read file attributes {}", path, e);
            }
            if (attributes != null) {
                result = new FileInfo(attributes.creationTime().to(TimeUnit.MILLISECONDS),
                        attributes.lastModifiedTime().to(TimeUnit.MILLISECONDS));
            }

            return result;
        }

    }

    private class FolderLocator implements FileVisitor<Path> {

        private String wantedContentId;
        private FolderContentType wantedContentType;
        private Folder result;

        public FolderLocator(String wantedContentId, FolderContentType wantedContentType) {
            this.wantedContentId = wantedContentId;
            this.wantedContentType = wantedContentType;
            result = null;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try (InputStream inputStream = Files.newInputStream(file)) {
                Properties properties = new Properties();
                properties.load(inputStream);
                if (StringUtils.equals(properties.getProperty("contentId"), wantedContentId) && //
                        wantedContentType.equals(FolderContentType.fromName(properties.getProperty("contentType")))) {

                    final Path parent = file.getParent();
                    String parentPath = pathAsString(parent);
                    FileInfo fileInfo = FileInfo.create(parent);
                    result =  new Folder(parentPath, extractName(parentPath), fileInfo.getCreationDate(), fileInfo.getLastModificationDate());
                    return TERMINATE;
                }
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return CONTINUE;
        }

        /**
         * @return the Result
         */
        public Folder getResult() {
            return result;
        }
    }
}
