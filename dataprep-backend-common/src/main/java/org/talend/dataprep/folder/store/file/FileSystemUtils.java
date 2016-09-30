package org.talend.dataprep.folder.store.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.folder.FolderContentType;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.exception.TDPException;

import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.*;
import static org.talend.dataprep.folder.store.FoldersRepositoriesConstants.*;

/**
 * Utility class to manipulate local files.
 */
class FileSystemUtils {

    private FileSystemUtils() {
    }

    /**
     * @param path the path to number folders from.
     * @return the folder number of a directory recursively
     */
    static long countSubDirectories(Path path) {
        try (Stream<Path> stream = Files.walk(path)) {
            // skip first to avoid counting input directory
            return stream.skip(1).filter(Files::isDirectory).count();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    static Consumer<Path> deleteFile() {
        return pathFile -> {
            try {
                Files.delete(pathFile);
            } catch (IOException e) {
                throw new TDPException(UNABLE_TO_REMOVE_FOLDER_ENTRY, e, build().put("path", pathFile));
            }
        };
    }

    static boolean matches(Path pathFile, String contentId, FolderContentType contentType) {
        boolean passFilter = false;
        try (InputStream inputStream = Files.newInputStream(pathFile)) {
            FolderEntry folderEntry = readEntryFromStream(inputStream);
            if (Objects.equals(contentType, folderEntry.getContentType()) && //
                    StringUtils.equalsIgnoreCase(folderEntry.getContentId(), contentId)) {
                passFilter = true;
            }
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_READ_FOLDER_ENTRY, e, build().put("path", pathFile));
        }
        return passFilter;
    }

    private static FolderEntry readEntryFromStream(InputStream inputStream) throws IOException {
        // use java Properties to save the files
        Properties properties = new Properties();
        properties.load(inputStream);
        FolderEntry folderEntry = new FolderEntry();
        folderEntry.setContentId(properties.getProperty(CONTENT_ID));
        folderEntry.setContentType(FolderContentType.fromName(properties.getProperty(CONTENT_TYPE)));
        folderEntry.setFolderId(properties.getProperty(FOLDER_ID));
        return folderEntry;
    }

    /**
     * Tries to read a file for a dataprep {@link FolderEntry}.
     *
     * @param pathFile the location of the file
     * @return the folder entry or throws an exception if it cannot be read
     */
    static FolderEntry toFolderEntry(Path pathFile) {
        FolderEntry folderEntry;
        try (InputStream inputStream = Files.newInputStream(pathFile)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            folderEntry = new FolderEntry();
            folderEntry.setFolderId(properties.getProperty(FOLDER_ID));
            folderEntry.setContentType(FolderContentType.fromName(properties.getProperty(CONTENT_TYPE)));
            folderEntry.setContentId(properties.getProperty(CONTENT_ID));
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_READ_FOLDER_ENTRY, e, build().put("path", pathFile));
        }
        return folderEntry;
    }

    static boolean hasEntry(Path path) {
        boolean hasChild;
        try (Stream<Path> pathsStream = Files.walk(path)) {
            hasChild = pathsStream.filter(Files::isRegularFile).map(FileSystemUtils::toFolderEntry).findAny().isPresent();
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_DELETE_FOLDER, e, build().put("path", path));
        }
        return hasChild;
    }

    static void writeEntryToStream(FolderEntry folderEntry, OutputStream outputStream) throws IOException {
        // use java Properties to save the files
        Properties properties = new Properties();
        properties.setProperty(CONTENT_TYPE, folderEntry.getContentType().toString());
        properties.setProperty(CONTENT_ID, folderEntry.getContentId());
        properties.setProperty(FOLDER_ID, folderEntry.getFolderId());
        properties.store(outputStream, "saved");
    }


    /**
     * Converts this path to an identifier usable in an URL.
     *
     * @return the Base64 encoding of the String serialization of this path.
     */
    public static String toId(FolderPath folderPath) {
        return new String(Base64.getEncoder().encode(folderPath.serializeAsString().getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Read an id generated from {@link #toId(FolderPath)} back to a {@link FolderPath}.
     *
     * @return the path or null if the input is null or not readable
     */
    public static FolderPath fromId(String id) {
        FolderPath folderPath;
        if (id == null) {
            folderPath = null;
        } else
            try {
                String pathAsString = new String(Base64.getDecoder().decode(id), StandardCharsets.UTF_8);
                folderPath = FolderPath.deserializeFromString(pathAsString);
            } catch (final NullPointerException | IllegalArgumentException e) { //NOSONAR no need to log such an exception
                // TODO: Maybe throwing an exception would be best, something like "InvalidFolderIdException"
                folderPath = null;
            }
        return folderPath;
    }
}
