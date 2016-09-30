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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContentType;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.api.share.Owner;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.folder.store.NotEmptyFolderException;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.util.StringsHelper;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.api.folder.FolderBuilder.folder;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.*;
import static org.talend.dataprep.folder.store.FoldersRepositoriesConstants.PATH_SEPARATOR;
import static org.talend.dataprep.folder.store.file.FileSystemUtils.*;

/**
 * File system folder repository implementation.
 */
@Component("folderRepository#file")
@ConditionalOnProperty(name = "folder.store", havingValue = "file")
public class FileSystemFolderRepository implements FolderRepository {

    @Autowired
    private Security security;

    @Autowired
    private PathsConverter pathsConverter;

    /**
     * Make sure the root folder is there.
     */
    @PostConstruct
    private void init() {
        try {
            Path rootPath = pathsConverter.getRootFolder();
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Folder getHome() {
        return toFolder(pathsConverter.getRootFolder(), security.getUserId());
    }

    @Override
    public boolean exists(String folderId) {
        FolderPath path = fromId(folderId);
        return path != null && Files.exists(pathsConverter.toPath(path));
    }

    @Override
    public Iterable<Folder> children(String parentId) {
        final FolderPath parentDpPath = fromId(parentId);
        try {
            Path folderPath;
            if (parentDpPath != null) {
                folderPath = pathsConverter.toPath(parentDpPath);
            } else {
                folderPath = pathsConverter.getRootFolder();
            }
            List<Folder> children;
            if (Files.notExists(folderPath)) {
                children = emptyList();
            } else {
                try (Stream<Path> childrenStream = Files.list(folderPath)) {
                    children = childrenStream //
                            .map(p -> toFolderIfDirectory(p, security.getUserId())) //
                            .filter(f -> f != null) //
                            .collect(toList());
                }
            }
            return children;
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_LIST_FOLDER_CHILDREN, e,
                    build().put("path", parentDpPath == null ? null : parentDpPath.serializeAsString()));
        }
    }

    @Override
    public Folder addFolder(String parentId, String givenPath) {
        // parent path must be set and exists
        FolderPath parentFolderPath = fromId(parentId);

        List<String> pathToAppend = Arrays.stream(givenPath.split(PATH_SEPARATOR.toString()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        if (parentFolderPath == null || !exists(parentId)) {
            throw new TDPException(UNABLE_TO_ADD_FOLDER, build().put("path", givenPath));
        }

        FolderPath folderPathToCreate = new FolderPath(parentFolderPath, pathToAppend.toArray(new String[pathToAppend.size()]));
        try {
            Path pathToCreate = pathsConverter.toPath(folderPathToCreate);
            Files.createDirectories(pathToCreate);
            return toFolder(pathToCreate, security.getUserId());
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_ADD_FOLDER, e, build().put("path", givenPath));
        }
    }

    @Override
    public Folder getFolderById(String folderId) {
        final FolderPath folderDpPath = fromId(folderId);
        final Path folderPath = pathsConverter.toPath(folderDpPath);
        return toFolder(folderPath, security.getUserId());
    }

    @Override
    public Folder renameFolder(String folderId, String newName) {

        final Folder folder = getFolderById(folderId);

        if (folder == null) {
            throw new IllegalArgumentException("Cannot rename a folder that cannot be found");
        }

        FolderPath folderToMovePath = FolderPath.deserializeFromString(folder.getPath());

        if (folderToMovePath.isRoot()) {
            throw new IllegalArgumentException("Cannot rename home folder");
        }

        if (newName.contains(PATH_SEPARATOR.toString())) {
            throw new TDPException(ILLEGAL_FOLDER_NAME, build().put("name", newName));
        }

        FolderPath targetFolderPath = new FolderPath(folderToMovePath.getParent(), newName);
        Path folderPath = pathsConverter.toPath(folderToMovePath);
        Path newFolderPath = pathsConverter.toPath(targetFolderPath);

        try {
            FileUtils.moveDirectory(folderPath.toFile(), newFolderPath.toFile());
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_RENAME_FOLDER, e, build().put("path", folder.getPath()));
        }

        return getFolderById(toId(pathsConverter.toFolderPath(newFolderPath)));
    }

    @Override
    public FolderEntry addFolderEntry(FolderEntry folderEntry, String folderId) {
        FolderPath folderPath = fromId(folderId);

        if (folderPath == null) {
            throw new TDPException(FOLDER_DOES_NOT_EXIST, build().put("id", folderId));
        }

        // we store the FolderEntry bean content as properties the file name is the name
        try {
            String fileName = buildFileName(folderEntry);

            Path path = pathsConverter.toPath(folderPath).resolve(fileName);

            // we delete it if exists
            Files.deleteIfExists(path);

            Path parentPath = path.getParent();
            // check parent path first
            if (Files.notExists(parentPath)) {
                Files.createDirectories(parentPath);
            }

            path = Files.createFile(path);
            folderEntry.setFolderId(folderId);

            try (OutputStream outputStream = Files.newOutputStream(path)) {
                writeEntryToStream(folderEntry, outputStream);
            }
            return folderEntry;
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_ADD_FOLDER_ENTRY, e, build().put("path", folderPath));
        }
    }

    @Override
    public void removeFolderEntry(String folderId, String contentId, FolderContentType contentType) {

        if (contentType == null) {
            throw new IllegalArgumentException("The content type of the folder entry to be removed cannot be null.");
        }

        final FolderPath folderPath = fromId(folderId);

        try {
            Path path = pathsConverter.toPath(folderPath);

            try (Stream<Path> paths = Files.list(path)) {
                paths //
                        .filter(pathFound -> !Files.isDirectory(pathFound)) //
                        .filter(pathFile -> matches(pathFile, contentId, contentType)) //
                        .forEach(deleteFile());
            }
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_REMOVE_FOLDER_ENTRY, e, build().put("path", folderPath));
        }
    }

    @Override
    public void removeFolder(String folderId) throws NotEmptyFolderException {
        final Path path = pathsConverter.toPath(fromId(folderId));

        if (hasEntry(path)) {
            throw new NotEmptyFolderException("The folder or a child contains data");
        } else {
            try {
                FileUtils.deleteDirectory(path.toFile());
            } catch (IOException e) {
                throw new TDPException(UNABLE_TO_DELETE_FOLDER, e, build().put("path", path));
            }
        }
    }

    @Override
    public Iterable<FolderEntry> entries(String folderId, FolderContentType contentType) {
        FolderPath folderPath = fromId(folderId);

        if (folderPath == null) {
            throw new TDPException(FOLDER_DOES_NOT_EXIST, build().put("id", folderId));
        }

        final Path path = pathsConverter.toPath(folderPath);

        if (Files.notExists(path)) {
            return emptyList();
        }

        try {
            try (Stream<Path> paths = Files.list(path)) {
                return paths.filter(pathFound -> !Files.isDirectory(pathFound)) //
                        .map(FileSystemUtils::toFolderEntry) //
                        .filter(entry -> Objects.equals(contentType, entry.getContentType())) //
                        .collect(toList());
            }
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_LIST_FOLDER_ENTRIES, e, build().put("path", path).put("type", contentType));
        }
    }

    @Override
    public Iterable<FolderEntry> findFolderEntries(String contentId, FolderContentType contentType) {
        try (Stream<Path> filesStream = Files.walk(pathsConverter.getRootFolder())) {
            return filesStream //
                    .filter(Files::isRegularFile) //
                    .map(FileSystemUtils::toFolderEntry) //
                    .filter(entry -> StringUtils.equals(entry.getContentId(), contentId) //
                            && Objects.equals(contentType, entry.getContentType())) //
                    .collect(toSet());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void clear() {
        try {
            FileUtils.deleteDirectory(pathsConverter.getRootFolder().toFile());
            init();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Iterable<Folder> searchFolders(String queryString, boolean strict) {
        try (Stream<Path> walk = Files.walk(pathsConverter.getRootFolder())) {
            return walk //
                    .filter(path -> //
                            Files.isDirectory(path) //
                                    && StringsHelper.match(path.getFileName().toString(), queryString, strict)) //
                    .map(path -> toFolder(path, security.getUserId())) //
                    .collect(toList());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void copyFolderEntry(FolderEntry folderEntry, String destinationId) {
        FolderEntry cloned = new FolderEntry(folderEntry.getContentType(), folderEntry.getContentId());
        cloned.setFolderId(destinationId);
        addFolderEntry(cloned, destinationId);
    }

    @Override
    public void moveFolderEntry(FolderEntry folderEntry, String fromId, String toId) {
        Path destinationPath = pathsConverter.toPath(fromId(toId));
        if (Files.notExists(destinationPath)) {
            throw new IllegalArgumentException("destinationPath doesn't exists");
        }

        Path originFilePath = Paths.get(pathsConverter.toPath(fromId(fromId)).toString(), buildFileName(folderEntry));
        if (Files.notExists(originFilePath)) {
            throw new IllegalArgumentException("entry doesn't exists");
        }

        Path destinationFile = Paths.get(destinationPath.toString(), buildFileName(folderEntry));
        try {
            Files.move(originFilePath, destinationFile);
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_MOVE_FOLDER_ENTRY, e);
        }
    }

    @Override
    public Folder locateEntry(String contentId, FolderContentType type) {
        try (Stream<Path> walk = Files.walk(pathsConverter.getRootFolder())) {
            return walk //
                    .filter(Files::isRegularFile) //
                    .filter(file -> {
                        FolderEntry folderEntry = FileSystemUtils.toFolderEntry(file);
                        return Objects.equals(folderEntry.getContentId(), contentId) //
                                && Objects.equals(folderEntry.getContentType(), type);
                    }) //
                    .findFirst() //
                    .map(Path::getParent) //
                    .map(p -> toFolder(p, security.getUserId())) //
                    .orElse(null);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public long size() {
        return countSubDirectories(pathsConverter.getRootFolder());
    }

    private static String buildFileName(FolderEntry folderEntry) {
        return folderEntry.getContentType().toString() + '@' + folderEntry.getContentId();
    }

    /** If the path represents a directory, build the {@link Folder} object based on it. */
    private Folder toFolderIfDirectory(Path path, String ownerId) {
        final Folder child;
        if (Files.isDirectory(path)) {
            child = toFolder(path, ownerId);
        } else {
            return null;
        }
        return child;
    }

    private Folder toFolder(Path filePath, String ownerId) {
        FolderPath folderPath = pathsConverter.toFolderPath(filePath);
        FolderInfo folderInfo = FolderInfo.create(filePath);
        if (folderInfo == null) {
            // TODO: we have a problem here: moving (or renaming) a file modify its parent, not itself at least on UNIX file system.
            long currentTime = System.currentTimeMillis();
            folderInfo = new FolderInfo(currentTime, currentTime);
        }
        return folder() //
                .path(folderPath.serializeAsString()) //
                .id(toId(folderPath)) //
                .parentId(folderPath.isRoot() ? null : toId(folderPath.getParent())) //
                .name(folderPath.getName()) //
                .ownerId(ownerId) //
                .owner(new Owner(ownerId, ownerId, "")) // default owner information
                .creationDate(folderInfo.getCreationDate()) //
                .lastModificationDate(folderInfo.getLastModificationDate()) //
                .build();
    }

}
