package org.talend.dataprep.folder.store.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.folder.FolderContentType;
import org.talend.dataprep.api.folder.FolderEntry;

import static org.junit.Assert.*;
import static org.talend.dataprep.folder.store.FoldersRepositoriesConstants.*;

public class FileSystemUtilsTest {

    private Path testFolder;

    @Before
    public void setUp() throws Exception {
        testFolder = Files.createTempDirectory("dataprep_test");
        Files.createDirectory(testFolder.resolve("foo"));
        Files.createDirectory(testFolder.resolve("bar"));
        Files.createDirectory(testFolder.resolve("beer"));
    }

    @Test
    public void countSubDirectories() throws Exception {
        assertEquals(3, FileSystemUtils.countSubDirectories(testFolder));
    }

    @Test
    public void deleteFile() throws Exception {
        assertEquals(4, Files.walk(testFolder).count());

        Files.walk(testFolder) //
                .filter(path -> "foo".equals(path.getFileName().toString())) //
                .forEach(FileSystemUtils.deleteFile());

        assertEquals(3, Files.walk(testFolder).count());
    }

    @Test
    public void matches_entryMatches() throws Exception {
        Path testEntry = testFolder.resolve("testEntry");
        String contentId = "contentId";
        String folderId = "folderId";
        FolderContentType contentType = FolderContentType.PREPARATION;

        createEntry(testEntry, contentId, folderId, contentType);

        // test read
        assertTrue(FileSystemUtils.matches(testEntry, contentId, contentType));
    }

    @Test
    public void matches_entryDoesNotMatch() throws Exception {
        Path testEntry = testFolder.resolve("testEntry");
        String contentId = "contentId";
        String folderId = "folderId";
        FolderContentType contentType = FolderContentType.PREPARATION;

        createEntry(testEntry, contentId, folderId, contentType);

        // test read
        assertFalse(FileSystemUtils.matches(testEntry, "other id", contentType));
    }

    @Test
    public void toFolderEntry() throws Exception {
        Path testEntry = testFolder.resolve("testEntry");
        String contentId = "contentId";
        String folderId = "folderId";
        FolderContentType contentType = FolderContentType.PREPARATION;
        createEntry(testEntry, contentId, folderId, contentType);

        FolderEntry folderEntry = FileSystemUtils.toFolderEntry(testEntry);

        assertNotNull(folderEntry);
        assertEquals(contentId, folderEntry.getContentId());
        assertEquals(folderId, folderEntry.getFolderId());
        assertEquals(contentType, folderEntry.getContentType());
    }

    private static void createEntry(Path testEntry, String contentId, String folderId, FolderContentType contentType)
            throws IOException {
        Properties properties = new Properties();
        properties.setProperty(CONTENT_TYPE, contentType.toString());
        properties.setProperty(CONTENT_ID, contentId);
        properties.setProperty(FOLDER_ID, folderId);
        try (OutputStream out = Files.newOutputStream(testEntry)) {
            properties.store(out, "saved");
        }
    }

    @Test
    public void hasEntry_testFolderHasDirectChild() throws Exception {
        createEntry(testFolder.resolve("toto.dp"), "contentId", "folderId", FolderContentType.PREPARATION);

        assertTrue(FileSystemUtils.hasEntry(testFolder));
    }

    @Test
    public void hasEntry_testFolderHasInDirectChild() throws Exception {
        createEntry(testFolder.resolve("foo/toto.dp"), "contentId", "folderId", FolderContentType.PREPARATION);

        assertTrue(FileSystemUtils.hasEntry(testFolder));
    }

    @Test
    public void hasEntry_childFolderHasNoChild() throws Exception {
        assertFalse(FileSystemUtils.hasEntry(testFolder));
    }

    @Test
    public void writeEntryToStream() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FolderEntry folderEntry = new FolderEntry();
        folderEntry.setContentType(FolderContentType.PREPARATION);
        folderEntry.setContentId("contentId");
        folderEntry.setFolderId("folderId");

        FileSystemUtils.writeEntryToStream(folderEntry, outputStream);

        outputStream.close();
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(outputStream.toByteArray()));
        assertEquals("contentId", properties.getProperty("contentId"));
        assertEquals("folderId", properties.getProperty("folderId"));
        assertEquals(FolderContentType.PREPARATION, FolderContentType.fromName(properties.getProperty("contentType")));
    }

}