package org.talend.dataprep.folder.store.file;

import org.junit.Test;

import static org.junit.Assert.*;

public class FolderPathTest {

    @Test
    public void folderPath_fromParent() throws Exception {
        FolderPath parent = new FolderPath("foo", "bar");
        assertEquals(new FolderPath("foo", "bar", "beer"), new FolderPath(parent, "beer"));
    }

    @Test
    public void getParent() throws Exception {
        assertEquals(new FolderPath("foo", "bar"), new FolderPath("foo", "bar", "beer").getParent());
    }

    @Test
    public void isRoot() throws Exception {
        assertTrue(new FolderPath().isRoot());
    }

    @Test
    public void isRoot_notRoot() throws Exception {
        assertFalse(new FolderPath("foo").isRoot());
    }

    @Test
    public void getName() throws Exception {
        FolderPath folderPath = new FolderPath("foo", "bar", "beer");
        assertEquals("beer", folderPath.getName());
    }

    @Test
    public void setName() throws Exception {
        String oldName = "beer";
        FolderPath folderPath = new FolderPath("foo", "bar", oldName);
        assertEquals(oldName, folderPath.getName());

        String newName = "tom";
        folderPath.setName(newName);

        assertEquals(newName, folderPath.getName());
    }

    @Test
    public void serializeAsString() throws Exception {
        assertEquals("/foo/bar/beer", new FolderPath("foo", "bar", "beer").serializeAsString());
    }

    @Test
    public void deserializeFromString() throws Exception {
        assertEquals(new FolderPath("foo", "bar", "beer"), FolderPath.deserializeFromString("/foo/bar/beer"));
    }
}
