package org.talend.dataprep.folder;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.folder.store.FolderRepository;

import com.google.common.collect.Lists;
import org.talend.dataprep.folder.store.NotEmptyFolderException;

public abstract class AbstractFolderTest {

    protected abstract FolderRepository getFolderRepository();

    @After
    public void cleanAfter() {
        getFolderRepository().clear();
    }

    /**
     *
     * This test create one child under root assert size, child list then delete
     */
    @Test
    public void create_child_then_remove() throws Exception {

        int sizeBefore = getFolderRepository().size();

        Folder child = getFolderRepository().addFolder("foo");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 1);

        Iterable<Folder> iterable = getFolderRepository().children("");
        List<Folder> folders = Lists.newArrayList(iterable);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolder("/foo");

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore);

        iterable = getFolderRepository().children("");
        folders = Lists.newArrayList(iterable);

        Assertions.assertThat(folders).isNotNull().isEmpty();

    }

    /**
     * this test create a hierarchy then delete part of it doing some assert on list, size then delete part of it
     * asserting the deletion
     */
    @Test
    public void create_two_children_little_children_then_remove() throws Exception {

        // - foo
        // - beer-
        // | - bar

        int sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("foo");

        Folder beer = getFolderRepository().addFolder("beer");

        Folder bar = getFolderRepository().addFolder("beer/bar");

        int sizeAfter = getFolderRepository().size();

        List<Folder> folders = null;
        Iterable<Folder> iterable = getFolderRepository().allFolder();
        folders = Lists.newArrayList(iterable);

        Assertions.assertThat(folders).isNotEmpty().hasSize(sizeBefore + 3);

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 3);

        iterable = getFolderRepository().children("");
        folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(2);

        // testing child of /bar

        iterable = getFolderRepository().children("/beer");
        folders = Lists.newArrayList(iterable);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(folders.get(0).getPath()).isEqualToIgnoringCase("beer/bar");
        Assertions.assertThat(folders.get(0).getName()).isEqualToIgnoringCase("bar");

        getFolderRepository().removeFolder("/beer/bar");

        // testing child of /beer after removing the first child
        iterable = getFolderRepository().children("/beer");
        folders = Lists.newArrayList(iterable);

        Assertions.assertThat(folders).isNotNull().isEmpty();

        // testing the whole size

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(2);

        getFolderRepository().removeFolder("/foo");

        getFolderRepository().removeFolder("/beer");

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore);

        iterable = getFolderRepository().children("");
        folders = Lists.newArrayList(iterable);

        Assertions.assertThat(folders).isNotNull().isEmpty();

    }

    /**
     *
     * This test create one child under root assert size, child list then create three folder entries then delete
     */
    @Test
    public void create_child_with_two_entries_then_remove() throws Exception {

        int sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("foo");

        Folder foobeer = getFolderRepository().addFolder("foo/beer");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 2);

        assertChildrenSize("", 1);

        FolderEntry beerEntry = new FolderEntry(DataSet.class.getName(), "littlecreatures", "/foo");

        FolderEntry wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo");

        getFolderRepository().addFolderEntry(beerEntry);

        getFolderRepository().addFolderEntry(wineEntry);

        wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo/beer");

        getFolderRepository().addFolderEntry(wineEntry);

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        List<FolderEntry> entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("bordeaux", DataSet.class.getName());
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("littlecreatures", DataSet.class.getName());
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolderEntry("/foo", "littlecreatures", DataSet.class.getName());

        getFolderRepository().removeFolderEntry("foo", "bordeaux", DataSet.class.getName());

        getFolderRepository().removeFolderEntry("foo/beer", "bordeaux", DataSet.class.getName());

        folderEntries = getFolderRepository().entries("/foo", DataSet.class.getName());
        entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isEmpty();

        getFolderRepository().removeFolder("/foo");

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore);

        assertChildrenSize("", 0);

    }

    /**
     *
     * This test create one child under root assert size, child list then create three folder entries then delete
     * expect exception
     */
    @Test(expected = NotEmptyFolderException.class)
    public void create_child_with_two_entries_then_remove_expect_exception() throws Exception {

        int sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("foo");

        Folder foobeer = getFolderRepository().addFolder("foo/beer");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 2);

        assertChildrenSize("", 1);

        FolderEntry beerEntry = new FolderEntry(DataSet.class.getName(), "littlecreatures", "/foo");

        FolderEntry wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo");

        getFolderRepository().addFolderEntry(beerEntry);

        getFolderRepository().addFolderEntry(wineEntry);

        wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo/beer");

        getFolderRepository().addFolderEntry(wineEntry);

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        List<FolderEntry> entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("bordeaux", DataSet.class.getName());
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("littlecreatures", DataSet.class.getName());
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        folderEntries = getFolderRepository().entries("/foo", DataSet.class.getName());
        entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize( 2 );

        getFolderRepository().removeFolder("/foo");

    }


    /**
     *
     * This test create two folders and a folder entry then copy it to the other folder
     */
    @Test
    public void create_entry_then_copy() throws Exception {

        int sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("foo");

        Folder bar = getFolderRepository().addFolder("bar");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 2);

        assertChildrenSize("", 2);

        FolderEntry wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo");

        getFolderRepository().addFolderEntry(wineEntry);

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        List<FolderEntry> entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains(wineEntry);

        getFolderRepository().copyFolderEntry(wineEntry, "bar");

        folderEntries = getFolderRepository().entries("bar", DataSet.class.getName());
        entries = Lists.newArrayList(folderEntries);

        // path has changed
        wineEntry.setPath("bar");
        wineEntry.buildId();
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains(wineEntry);

        // still in foo as it's a copy
        folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        entries = Lists.newArrayList(folderEntries);

        wineEntry.setPath("foo");
        wineEntry.buildId();
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains(wineEntry);

    }

    /**
     *
     * This test create two folders and a folder entry then move it to the other folder
     */
    @Test
    public void create_entry_then_move() throws Exception {

        int sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("foo");

        Folder bar = getFolderRepository().addFolder("bar");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 2);

        assertChildrenSize("", 2);

        FolderEntry wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo");

        getFolderRepository().addFolderEntry(wineEntry);

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        List<FolderEntry> entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains(wineEntry);

        getFolderRepository().moveFolderEntry(wineEntry, "bar");

        folderEntries = getFolderRepository().entries("bar", DataSet.class.getName());
        entries = Lists.newArrayList(folderEntries);

        // new path is bar for assert
        wineEntry.setPath("bar");

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains(wineEntry);

        // not in foo as it's a move
        folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isEmpty();

    }

    @Test
    public void rename_folder_with_entries_and_subfolders() throws Exception {

        int sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("foo");

        Folder foobeer = getFolderRepository().addFolder("foo/beer");

        Folder foobar = getFolderRepository().addFolder("foo/bar");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 3);

        assertChildrenSize("", 1);

        FolderEntry beerEntry = new FolderEntry(DataSet.class.getName(), "littlecreatures", "/foo");

        FolderEntry wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo");

        getFolderRepository().addFolderEntry(beerEntry);

        getFolderRepository().addFolderEntry(wineEntry);

        wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo/beer");

        getFolderRepository().addFolderEntry(wineEntry);

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        List<FolderEntry> entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("bordeaux", DataSet.class.getName());
        entries = Lists.newArrayList(folderEntries);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("littlecreatures", DataSet.class.getName());
        entries = Lists.newArrayList(folderEntries);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolderEntry("foo", "littlecreatures", DataSet.class.getName());

        folderEntries = getFolderRepository().entries("/foo", DataSet.class.getName());
        entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        assertChildrenSize("/foo", 2);

        getFolderRepository().renameFolder("/foo", "/wine");

        assertChildrenSize("/wine", 2);

        // test FolderEntry moved as well
        folderEntries = getFolderRepository().entries("/wine", DataSet.class.getName());
        entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolderEntry("/wine", "bordeaux", DataSet.class.getName());

        getFolderRepository().removeFolderEntry("/wine/beer", "bordeaux", DataSet.class.getName());

        getFolderRepository().removeFolder("/wine");

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore);

        assertChildrenSize("/", 0);

    }

    /**
     *
     * This test create folders and search in names
     */
    @Test
    public void create_folders_then_search() throws Exception {

        int sizeBefore = getFolderRepository().size();

        getFolderRepository().addFolder("foo");

        getFolderRepository().addFolder("bar");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 2);

        assertChildrenSize("", 2);

        getFolderRepository().addFolder("foo/beer");

        getFolderRepository().addFolder("foo/wine");

        getFolderRepository().addFolder("foo/wine/toto");

        getFolderRepository().addFolder("foo/wine/titi");

        getFolderRepository().addFolder("foo/wine/thetiti");

        getFolderRepository().addFolder("foo/wine/yupTITI");

        getFolderRepository().addFolder("foo/wine/yeahTITI");

        getFolderRepository().addFolder("foo/wine/goodwine");

        getFolderRepository().addFolder("foo/wine/verygoodWInE");

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 2 + 9);

        assertOnSearch("foo", 1);

        assertOnSearch("wine", 3);

        assertOnSearch("tIti", 4);

    }

    protected void assertChildrenSize(String folder, int childrenNumber) {
        Iterable<Folder> iterable = getFolderRepository().children(folder);
        List<Folder> folders = Lists.newArrayList(iterable);
        if (childrenNumber > 0) {
            Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(childrenNumber);
        } else {
            Assertions.assertThat(folders).isNotNull().isEmpty();
        }
    }

    protected void assertOnSearch(String query, int foundNumber) {
        Iterable<Folder> folders = getFolderRepository().searchFolders(query);
        Assertions.assertThat(Lists.newArrayList(folders)).isNotNull().isNotEmpty().hasSize(foundNumber);
    }

}
