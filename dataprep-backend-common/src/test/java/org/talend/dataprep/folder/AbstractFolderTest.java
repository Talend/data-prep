//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.folder;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.folder.FolderContentType.DATASET;
import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContentType;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.folder.store.NotEmptyFolderException;

import com.google.common.collect.Lists;

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

        long sizeBefore = getFolderRepository().size();

        Folder child = getFolderRepository().addFolder("foo");

        long sizeAfter = getFolderRepository().size();

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

        long sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("foo");

        Folder beer = getFolderRepository().addFolder("beer");

        Folder bar = getFolderRepository().addFolder("beer/bar");

        long sizeAfter = getFolderRepository().size();

        List<Folder> folders = null;
        Iterable<Folder> iterable = getFolderRepository().allFolder();
        folders = Lists.newArrayList(iterable);

        Assertions.assertThat(folders).isNotEmpty().hasSize((int)sizeBefore + 3);

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 3);

        iterable = getFolderRepository().children("");
        folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(2);

        // testing child of /bar

        iterable = getFolderRepository().children("/beer");
        folders = Lists.newArrayList(iterable);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(StringUtils.strip(folders.get(0).getPath(), "/")).isEqualToIgnoringCase("beer/bar");
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

        long sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("foo");

        Folder foobeer = getFolderRepository().addFolder("foo/beer");

        long sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 2);

        assertChildrenSize("", 1);

        FolderEntry beerEntry = new FolderEntry(DATASET, "littlecreatures");

        FolderEntry wineEntry = new FolderEntry(DATASET, "bordeaux");

        getFolderRepository().addFolderEntry(beerEntry, "/foo");

        getFolderRepository().addFolderEntry(wineEntry, "foo");

        wineEntry = new FolderEntry(DATASET, "bordeaux");

        getFolderRepository().addFolderEntry(wineEntry, "foo/beer");

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DATASET);
        List<FolderEntry> entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("bordeaux", DATASET);
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("littlecreatures", DATASET);
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolderEntry("/foo", "littlecreatures", DATASET);

        getFolderRepository().removeFolderEntry("foo", "bordeaux", DATASET);

        getFolderRepository().removeFolderEntry("foo/beer", "bordeaux", DATASET);

        folderEntries = getFolderRepository().entries("/foo", DATASET);
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

        long sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("foo");

        Folder foobeer = getFolderRepository().addFolder("foo/beer");

        long sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 2);

        assertChildrenSize("", 1);

        FolderEntry beerEntry = new FolderEntry(DATASET, "littlecreatures");

        FolderEntry wineEntry = new FolderEntry(DATASET, "bordeaux");

        getFolderRepository().addFolderEntry(beerEntry, "/foo");

        getFolderRepository().addFolderEntry(wineEntry, "foo");

        wineEntry = new FolderEntry(DATASET, "bordeaux");

        getFolderRepository().addFolderEntry(wineEntry, "foo/beer");

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DATASET);
        List<FolderEntry> entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("bordeaux", DATASET);
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("littlecreatures", DATASET);
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        folderEntries = getFolderRepository().entries("/foo", DATASET);
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

        // 2 folders /foo & /bar
        long sizeBefore = getFolderRepository().size();
        Folder foo = getFolderRepository().addFolder("foo");
        Folder bar = getFolderRepository().addFolder("bar");
        long sizeAfter = getFolderRepository().size();
        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 2);
        assertChildrenSize("", 2);

        //  bordeaux in /foo
        FolderEntry wineEntry = new FolderEntry(DATASET, "bordeaux");
        getFolderRepository().addFolderEntry(wineEntry, "foo");
        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DATASET);
        List<FolderEntry> entries = Lists.newArrayList(folderEntries);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains(wineEntry);

        // copy bordeaux in /bar
        getFolderRepository().copyFolderEntry(wineEntry, "bar");
        folderEntries = getFolderRepository().entries("bar", DATASET);
        entries = Lists.newArrayList(folderEntries);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);
        assertFolderEntry(entries.get(0), "bordeaux", DATASET);

        // still in foo as it's a copy
        folderEntries = getFolderRepository().entries("foo", DATASET);
        entries = Lists.newArrayList(folderEntries);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains(wineEntry);

    }

    private void assertFolderEntry(FolderEntry entry, String contentId, FolderContentType contentType) {
        assertEquals(entry.getContentId(), contentId);
        assertEquals(entry.getContentType(), contentType);

    }

    /**
     *
     * This test create two folders and a folder entry then move it to the other folder
     */
    @Test
    public void create_entry_then_move() throws Exception {

        // 2 folders /foo & /bar
        long sizeBefore = getFolderRepository().size();
        Folder foo = getFolderRepository().addFolder("foo");
        Folder bar = getFolderRepository().addFolder("bar");
        long sizeAfter = getFolderRepository().size();
        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 2);
        assertChildrenSize("", 2);

        // bordeaux in /foo
        FolderEntry wineEntry = new FolderEntry(DATASET, "bordeaux");
        getFolderRepository().addFolderEntry(wineEntry, "foo");
        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DATASET);
        List<FolderEntry> entries = Lists.newArrayList(folderEntries);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains(wineEntry);

        // move bordeaux to /bar
        getFolderRepository().moveFolderEntry(wineEntry, "foo", "bar");
        folderEntries = getFolderRepository().entries("bar", DATASET);
        entries = Lists.newArrayList(folderEntries);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);
        assertFolderEntry(entries.get(0), "bordeaux", DATASET);

        // not in foo as it's a move
        folderEntries = getFolderRepository().entries("foo", DATASET);
        entries = Lists.newArrayList(folderEntries);
        Assertions.assertThat(entries).isNotNull().isEmpty();

    }

    @Test
    public void rename_folder_with_entries_and_subfolders() throws Exception {

        long sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("foo");

        Folder foobeer = getFolderRepository().addFolder("foo/beer");

        Folder foobar = getFolderRepository().addFolder("foo/bar");

        long sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 3);

        assertChildrenSize("", 1);

        FolderEntry beerEntry = new FolderEntry(DATASET, "littlecreatures");
        getFolderRepository().addFolderEntry(beerEntry, "/foo");

        FolderEntry wineEntry = new FolderEntry(DATASET, "bordeaux");
        getFolderRepository().addFolderEntry(wineEntry, "foo");

        wineEntry = new FolderEntry(DATASET, "bordeaux");
        getFolderRepository().addFolderEntry(wineEntry, "foo/beer");

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DATASET);
        List<FolderEntry> entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("bordeaux", DATASET);
        entries = Lists.newArrayList(folderEntries);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries("littlecreatures", DATASET);
        entries = Lists.newArrayList(folderEntries);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolderEntry("foo", "littlecreatures", DATASET);

        folderEntries = getFolderRepository().entries("/foo", DATASET);
        entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        assertChildrenSize("/foo", 2);

        getFolderRepository().renameFolder("/foo", "/wine");

        assertChildrenSize("/wine", 2);

        // test FolderEntry moved as well
        folderEntries = getFolderRepository().entries("/wine", DATASET);
        entries = Lists.newArrayList(folderEntries);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolderEntry("/wine", "bordeaux", DATASET);

        getFolderRepository().removeFolderEntry("/wine/beer", "bordeaux", DATASET);

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

        long sizeBefore = getFolderRepository().size();

        getFolderRepository().addFolder("foo");

        getFolderRepository().addFolder("bar");

        long sizeAfter = getFolderRepository().size();

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

    @Test
    public void shouldFindFolder() throws Exception {
        // given
        getFolderRepository().addFolder("foo");
        getFolderRepository().addFolder("foo/bar");

        // then
        assertTrue(getFolderRepository().exists("foo"));
        assertTrue(getFolderRepository().exists("foo/bar"));
    }

    @Test
    public void rootFolderShouldAlwaysBeAvailable() throws Exception {
        // then
        assertTrue(getFolderRepository().exists("/"));
    }


    @Test
    public void shouldLocateEntry() throws Exception {
        // given (root & 2 entries)
        final Folder root = getFolderRepository().addFolder("/");

        final FolderEntry littleCreatures = new FolderEntry(DATASET, "littleCreatures");
        getFolderRepository().addFolderEntry(littleCreatures, root.getPath());

        final FolderEntry bordeaux = new FolderEntry(PREPARATION, "bordeaux");
        getFolderRepository().addFolderEntry(bordeaux, root.getPath());

        // given (foo & 2 entries)
        final Folder foo = getFolderRepository().addFolder("/foo");

        final FolderEntry cars = new FolderEntry(DATASET, "cars");
        getFolderRepository().addFolderEntry(cars, foo.getPath());

        final FolderEntry boats = new FolderEntry(PREPARATION, "boats");
        getFolderRepository().addFolderEntry(boats, foo.getPath());

        // add some noise
        getFolderRepository().addFolder("/foo/bar");
        getFolderRepository().addFolder("/hello");

        // then
        assertNull(getFolderRepository().locateEntry("not to ne found", PREPARATION));
        assertEquals(root, getFolderRepository().locateEntry(bordeaux.getContentId(), bordeaux.getContentType()));
        assertEquals(root, getFolderRepository().locateEntry(littleCreatures.getContentId(), littleCreatures.getContentType()));

        assertEquals(foo, getFolderRepository().locateEntry(cars.getContentId(), cars.getContentType()));
        assertEquals(foo, getFolderRepository().locateEntry(boats.getContentId(), boats.getContentType()));
    }

    @Test
    public void shouldNotFindFolder() throws Exception {
        // given
        getFolderRepository().addFolder("foo");
        getFolderRepository().addFolder("foo/bar");

        // then
        assertFalse(getFolderRepository().exists("totototo"));
        assertFalse(getFolderRepository().exists("titititi/totototo"));
    }

    private void assertChildrenSize(String folder, int childrenNumber) {
        Iterable<Folder> iterable = getFolderRepository().children(folder);
        List<Folder> folders = Lists.newArrayList(iterable);
        if (childrenNumber > 0) {
            Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(childrenNumber);
        } else {
            Assertions.assertThat(folders).isNotNull().isEmpty();
        }
    }

    private void assertOnSearch(String query, int foundNumber) {
        Iterable<Folder> folders = getFolderRepository().searchFolders(query);
        Assertions.assertThat(Lists.newArrayList(folders)).isNotNull().isNotEmpty().hasSize(foundNumber);
    }

}
