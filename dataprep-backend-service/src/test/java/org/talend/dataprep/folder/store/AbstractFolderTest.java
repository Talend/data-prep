// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.folder.store;

import static junit.framework.TestCase.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.folder.FolderContentType.DATASET;
import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderContentType;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.FolderErrorCodes;
import org.talend.dataprep.security.Security;

import com.google.common.collect.Lists;

public abstract class AbstractFolderTest extends ServiceBaseTest {

    /**
     * @return the folder repository to test.
     */
    protected abstract FolderRepository getFolderRepository();

    /**
     * Some repository implementation may need to convert a path to an id.
     * @param path the path to convert to id.
     * @return the path converted into an id.
     */
    protected abstract String pathToId(String path);

    /** The home folder id. */
    private String homeFolderId;

    @Autowired
    protected Security security;

    @Before
    public void setUp() {
        super.setUp();
        this.homeFolderId = getFolderRepository().getHome().getId();
    }

    @After
    public void cleanAfter() {
        getFolderRepository().clear();
    }

    @Test
    public void folderShouldExist() throws Exception {
        // given
        final Folder foo = getFolderRepository().addFolder(homeFolderId, "/foo");

        // when
        final boolean exists = getFolderRepository().exists(foo.getId());

        // then
        assertTrue(exists);
    }

    @Test
    public void folderShouldNotExist() throws Exception {
        // when
        final boolean exists = getFolderRepository().exists(pathToId("should not exist"));
        // then
        assertFalse(exists);
    }

    @Test
    public void shouldRenameSubfolder() throws Exception {
        // given
        final Folder foo = getFolderRepository().addFolder(homeFolderId, "/foo");
        Folder bar = getFolderRepository().addFolder(foo.getId(), "/bar");
        getFolderRepository().addFolder(bar.getId(), "/toto");

        // when
        bar = getFolderRepository().renameFolder(bar.getId(), "beer");

        // then
        assertEquals("beer", bar.getName());
        assertEquals("/foo/beer", bar.getPath());

        final List<Folder> homeChildren = getChildren(homeFolderId);
        assertThat(homeChildren).isNotNull().isNotEmpty().hasSize(1);
        final Folder firstChild = homeChildren.get(0);
        // When moving a folder, it is its parent that is modified in the UNIX filesystem, not itself.
        assertThat(firstChild).isEqualToIgnoringGivenFields(foo, "creationDate", "lastModificationDate");

        assertChildrenSize(bar.getId(), 1);
    }

    private List<Folder> getChildren(String folderId) {
        try (Stream<Folder> childrenStream = getFolderRepository().children(folderId)) {
            return childrenStream.collect(Collectors.toList());
        }
    }

    /**
     *
     * This test create one child under root assert size, child list then delete
     */
    @Test
    public void create_child_then_remove() throws Exception {

        // create /foo
        long sizeBefore = getFolderRepository().size();
        final Folder foo = getFolderRepository().addFolder(homeFolderId, "/foo");
        long sizeAfter = getFolderRepository().size();
        assertThat(sizeAfter).isEqualTo(sizeBefore + 1);

        // make sure /foo is listed as children
        assertChildrenSize(homeFolderId, 1);

        // remove /foo
        getFolderRepository().removeFolder(foo.getId());
        sizeAfter = getFolderRepository().size();
        assertThat(sizeAfter).isEqualTo(sizeBefore);

        // make sure it's removed
        assertChildrenSize(homeFolderId, 0);
    }

    /**
     * this test create a hierarchy then delete part of it doing some assert on list, size then delete part of it
     * asserting the deletion
     */
    @Test
    public void create_two_children_little_children_then_remove() throws Exception {

        // - foo
        // - beer
        // +- bar

        long sizeBefore = getFolderRepository().size();
        Folder foo = getFolderRepository().addFolder(homeFolderId, "foo");
        Folder beer = getFolderRepository().addFolder(homeFolderId, "beer");
        Folder bar = getFolderRepository().addFolder(beer.getId(), "bar");

        assertChildrenSize(homeFolderId, 2);

        // testing child of /bar
        List<Folder> folders = getChildren(beer.getId());
        assertThat(folders).isNotNull().isNotEmpty().hasSize(1);
        assertThat(StringUtils.strip(folders.get(0).getPath(), "/")).isEqualToIgnoringCase("beer/bar");
        assertThat(folders.get(0).getName()).isEqualToIgnoringCase("bar");

        getFolderRepository().removeFolder(bar.getId());

        // testing child of /beer after removing the first child
        assertChildrenSize(beer.getId(), 0);

        // testing the whole size
        long sizeAfter = getFolderRepository().size();

        assertThat(sizeAfter).isEqualTo(2);
        getFolderRepository().removeFolder(foo.getId());
        getFolderRepository().removeFolder(beer.getId());

        sizeAfter = getFolderRepository().size();
        assertThat(sizeAfter).isEqualTo(sizeBefore);

        assertChildrenSize(homeFolderId, 0);
    }

    /**
     *
     * This test create one child under root assert size, child list then create three folder entries then delete
     */
    @Test
    public void create_child_with_two_entries_then_remove() throws Exception {

        long sizeBefore = getFolderRepository().size();
        Folder foo = getFolderRepository().addFolder(homeFolderId, "foo");
        Folder foobeer = getFolderRepository().addFolder(foo.getId(), "beer");
        long sizeAfter = getFolderRepository().size();
        assertThat(sizeAfter).isEqualTo(sizeBefore + 2);

        assertChildrenSize(homeFolderId, 1);

        FolderEntry beerEntry = new FolderEntry(DATASET, "littlecreatures");
        FolderEntry wineEntry = new FolderEntry(DATASET, "bordeaux");

        getFolderRepository().addFolderEntry(beerEntry, foo.getId());
        getFolderRepository().addFolderEntry(wineEntry, foo.getId());

        wineEntry = new FolderEntry(DATASET, "bordeaux");
        getFolderRepository().addFolderEntry(wineEntry, foobeer.getId());

        assertThatFolderContainsExpectedNumberOfDatasets(foo.getId(), 2);

        assertThatExpectedNumberOfNamedDatasetIsFound("bordeaux", 2);
        assertThatExpectedNumberOfNamedDatasetIsFound("littlecreatures", 1);

        getFolderRepository().removeFolderEntry(foo.getId(), "littlecreatures", DATASET);
        getFolderRepository().removeFolderEntry(foo.getId(), "bordeaux", DATASET);
        getFolderRepository().removeFolderEntry(foobeer.getId(), "bordeaux", DATASET);

        assertThatFolderContainsExpectedNumberOfDatasets(foo.getId(), 0);

        getFolderRepository().removeFolder(foo.getId());

        sizeAfter = getFolderRepository().size();
        assertThat(sizeAfter).isEqualTo(sizeBefore);

        assertChildrenSize(homeFolderId, 0);

    }

    private void assertThatFolderContainsExpectedNumberOfDatasets(String folderId, int numberOfDatasets) {
        List<FolderEntry> datasets = getDatasetsFromFolder(folderId);
        if (numberOfDatasets > 0) {
            assertThat(datasets).isNotNull().isNotEmpty().hasSize(numberOfDatasets);
        } else {
            assertThat(datasets).isNotNull().isEmpty();
        }
    }

    private List<FolderEntry> getDatasetsFromFolder(String folderId) {
        try (Stream<FolderEntry> entriesStream = getFolderRepository().entries(folderId, DATASET)) {
            return entriesStream.collect(Collectors.toList());
        }
    }

    private void assertThatExpectedNumberOfNamedDatasetIsFound(String datasetId, int numberOfDatasets) {
        try (Stream<FolderEntry> folderEntriesStream = getFolderRepository().findFolderEntries(datasetId, DATASET)) {
            List<FolderEntry> entries = folderEntriesStream.collect(Collectors.toList());
            assertThat(entries).isNotNull().isNotEmpty().hasSize(numberOfDatasets);
        }
    }

    /**
     *
     * This test create one child under root assert size, child list then create three folder entries then delete
     * expect exception
     */
    @Test
    public void create_child_with_two_entries_then_remove_expect_exception() throws Exception {

        long sizeBefore = getFolderRepository().size();
        Folder foo = getFolderRepository().addFolder(homeFolderId, "foo");
        Folder foobeer = getFolderRepository().addFolder(foo.getId(), "/beer");
        long sizeAfter = getFolderRepository().size();

        assertThat(sizeAfter).isEqualTo(sizeBefore + 2);

        assertChildrenSize(homeFolderId, 1);

        FolderEntry beerEntry = new FolderEntry(DATASET, "littlecreatures");
        FolderEntry wineEntry = new FolderEntry(DATASET, "bordeaux");

        getFolderRepository().addFolderEntry(beerEntry, foo.getId());
        getFolderRepository().addFolderEntry(wineEntry, foo.getId());

        wineEntry = new FolderEntry(DATASET, "bordeaux");

        getFolderRepository().addFolderEntry(wineEntry, foobeer.getId());

        assertThatFolderContainsExpectedNumberOfDatasets(foo.getId(), 2);

        assertThatExpectedNumberOfNamedDatasetIsFound("bordeaux", 2);
        assertThatExpectedNumberOfNamedDatasetIsFound("littlecreatures", 1);

        assertThatFolderContainsExpectedNumberOfDatasets(foo.getId(), 2);

        try {
            getFolderRepository().removeFolder(foo.getId());
            fail("Should throw exception because folder is not empty.");
        } catch (TDPException e) {
            assertEquals(FolderErrorCodes.FOLDER_NOT_EMPTY, e.getCode());
        }

    }


    /**
     *
     * This test create two folders and a folder entry then copy it to the other folder
     */
    @Test
    public void create_entry_then_copy() throws Exception {

        // 2 folders /foo & /bar
        long sizeBefore = getFolderRepository().size();
        Folder foo = getFolderRepository().addFolder(homeFolderId, "foo");
        Folder bar = getFolderRepository().addFolder(homeFolderId, "bar");
        long sizeAfter = getFolderRepository().size();
        assertThat(sizeAfter).isEqualTo(sizeBefore + 2);
        assertChildrenSize(homeFolderId, 2);

        //  bordeaux in /foo
        FolderEntry wineEntry = new FolderEntry(DATASET, "bordeaux");
        getFolderRepository().addFolderEntry(wineEntry, foo.getId());
        List<FolderEntry> entries = getDatasetsFromFolder(foo.getId());
        assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains(wineEntry);

        // copy bordeaux in /bar
        getFolderRepository().copyFolderEntry(wineEntry, bar.getId());
        entries = getDatasetsFromFolder(bar.getId());
        assertThat(entries).isNotNull().isNotEmpty().hasSize(1);
        assertFolderEntry(entries.get(0), "bordeaux", DATASET);

        // still in foo as it's a copy
        entries = getDatasetsFromFolder(foo.getId());
        assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains(wineEntry);

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
        Folder foo = getFolderRepository().addFolder(homeFolderId, "foo");
        Folder bar = getFolderRepository().addFolder(homeFolderId, "bar");
        long sizeAfter = getFolderRepository().size();
        assertThat(sizeAfter).isEqualTo(sizeBefore + 2);
        assertChildrenSize(homeFolderId, 2);

        // bordeaux in /foo
        FolderEntry wineEntry = new FolderEntry(DATASET, "bordeaux");
        getFolderRepository().addFolderEntry(wineEntry, foo.getId());
        List<FolderEntry> entries = getDatasetsFromFolder(foo.getId());
        assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains(wineEntry);

        // move bordeaux to /bar
        getFolderRepository().moveFolderEntry(wineEntry, foo.getId(), bar.getId());
        entries = getDatasetsFromFolder(bar.getId());
        assertThat(entries).isNotNull().isNotEmpty().hasSize(1);
        assertFolderEntry(entries.get(0), "bordeaux", DATASET);

        // not in foo as it's a move
        assertThatFolderContainsExpectedNumberOfDatasets(foo.getId(), 0);

    }

    @Test
    public void rename_folder_with_entries_and_subfolders() throws Exception {

        long sizeBefore = getFolderRepository().size();
        Folder foo = getFolderRepository().addFolder(homeFolderId, "foo");
        Folder foobeer = getFolderRepository().addFolder(foo.getId(), "beer");
        getFolderRepository().addFolder(foo.getId(), "bar");
        long sizeAfter = getFolderRepository().size();

        assertThat(sizeAfter).isEqualTo(sizeBefore + 3);

        assertChildrenSize(homeFolderId, 1);

        FolderEntry beerEntry = new FolderEntry(DATASET, "littlecreatures");
        getFolderRepository().addFolderEntry(beerEntry, foo.getId());

        FolderEntry wineEntry = new FolderEntry(DATASET, "bordeaux");
        getFolderRepository().addFolderEntry(wineEntry, foo.getId());

        wineEntry = new FolderEntry(DATASET, "bordeaux");
        getFolderRepository().addFolderEntry(wineEntry, foobeer.getId());

        assertThatFolderContainsExpectedNumberOfDatasets(foo.getId(), 2);

        assertThatExpectedNumberOfNamedDatasetIsFound("bordeaux", 2);
        assertThatExpectedNumberOfNamedDatasetIsFound("littlecreatures", 1);

        getFolderRepository().removeFolderEntry(foo.getId(), "littlecreatures", DATASET);

        assertThatFolderContainsExpectedNumberOfDatasets(foo.getId(), 1);
        assertChildrenSize(foo.getId(), 2);

        foo = getFolderRepository().renameFolder(foo.getId(), "wine");

        assertChildrenSize(foo.getId(), 2);

        // test FolderEntry moved as well
        assertThatFolderContainsExpectedNumberOfDatasets(foo.getId(), 1);

        getFolderRepository().removeFolderEntry(foo.getId(), "bordeaux", DATASET);
    }

    /**
     *
     * This test create folders and search in names
     */
    @Test
    public void create_folders_then_search() throws Exception {
        // given
        long sizeBefore = getFolderRepository().size();
        final Folder foo = getFolderRepository().addFolder(homeFolderId, "foo");
        getFolderRepository().addFolder(homeFolderId, "bar");
        long sizeAfter = getFolderRepository().size();
        assertThat(sizeAfter).isEqualTo(sizeBefore + 2);

        assertChildrenSize(homeFolderId, 2);

        getFolderRepository().addFolder(foo.getId(), "beer");
        Folder wine = getFolderRepository().addFolder(foo.getId(), "wine");
        getFolderRepository().addFolder(wine.getId(), "toto");
        getFolderRepository().addFolder(wine.getId(), "titi");
        getFolderRepository().addFolder(wine.getId(), "thetiti");
        getFolderRepository().addFolder(wine.getId(), "yupTITI");
        getFolderRepository().addFolder(wine.getId(), "yeahTITI");
        getFolderRepository().addFolder(wine.getId(), "goodwine");
        getFolderRepository().addFolder(wine.getId(), "verygoodWInE");

        sizeAfter = getFolderRepository().size();
        assertThat(sizeAfter).isEqualTo(sizeBefore + 2 + 9);

        // when / then
        assertOnSearch("foo", false, 1);
        assertOnSearch("wine", false, 3);
        assertOnSearch("tIti", false, 4);
        assertOnSearch("titi", true, 1); // strict
        assertOnSearch("", false, ((int) sizeAfter) + 1);

    }

    @Test
    public void shouldFindFolder() throws Exception {
        // given
        final Folder foo = getFolderRepository().addFolder(homeFolderId, "foo");
        final Folder bar = getFolderRepository().addFolder(foo.getId(), "bar");

        // then
        assertTrue(getFolderRepository().exists(foo.getId()));
        assertTrue(getFolderRepository().exists(bar.getId()));
    }

    @Test
    public void shouldLocateEntry() throws Exception {
        // given (root & 2 entries)
        final Folder root = getFolderRepository().getHome();

        final FolderEntry littleCreatures = new FolderEntry(DATASET, "littleCreatures");
        getFolderRepository().addFolderEntry(littleCreatures, root.getId());

        final FolderEntry bordeaux = new FolderEntry(PREPARATION, "bordeaux");
        getFolderRepository().addFolderEntry(bordeaux, root.getId());

        // given (foo & 2 entries)
        final Folder foo = getFolderRepository().addFolder(homeFolderId, "foo");

        final FolderEntry cars = new FolderEntry(DATASET, "cars");
        getFolderRepository().addFolderEntry(cars, foo.getId());

        final FolderEntry boats = new FolderEntry(PREPARATION, "boats");
        getFolderRepository().addFolderEntry(boats, foo.getId());

        // add some noise
        getFolderRepository().addFolder(foo.getId(), "bar");
        getFolderRepository().addFolder(homeFolderId, "hello");

        // then
        assertNull(getFolderRepository().locateEntry(pathToId("not to be found"), PREPARATION));
        assertEquals(root.getId(), getFolderRepository().locateEntry(bordeaux.getContentId(), bordeaux.getContentType()).getId());
        assertEquals(root.getId(), getFolderRepository().locateEntry(littleCreatures.getContentId(), littleCreatures.getContentType()).getId());

        assertEquals(foo.getId(), getFolderRepository().locateEntry(cars.getContentId(), cars.getContentType()).getId());
        assertEquals(foo.getId(), getFolderRepository().locateEntry(boats.getContentId(), boats.getContentType()).getId());
    }

    @Test
    public void shouldNotFindFolder() throws Exception {
        // given
        final Folder foo = getFolderRepository().addFolder(homeFolderId, "foo");
        getFolderRepository().addFolder(foo.getId(), "bar");

        // then
        assertFalse(getFolderRepository().exists(pathToId("/totototo")));
        assertFalse(getFolderRepository().exists(pathToId("/titititi/totototo")));
    }


    @Test
    public void shouldAddMultipleInOneCallFolders() throws Exception {

        // given
        assertEquals(0, getFolderRepository().size());
        final Folder marketing = getFolderRepository().addFolder(homeFolderId, "marketing");

        // when
        final Folder q1 = getFolderRepository().addFolder(marketing.getId(), "2016/q1");

        // then
        assertNotNull(q1);
        assertEquals(3, getFolderRepository().size());

        final Folder folder2016 = getFolderRepository().getFolderById(q1.getParentId());
        assertEquals("2016", folder2016.getName());

        assertEquals(marketing.getId(), folder2016.getParentId());
    }


    @Test
    public void parentIdShouldBeNullForHome() throws Exception {
        // when
        final Folder home = getFolderRepository().getHome();

        // then
        assertNull(home.getParentId());
    }

    @Test
    public void shouldReturnHomeFolder() {
        // when
        final Folder home = getFolderRepository().getHome();

        // then
        Assert.assertThat(home.getPath(), is("/"));
        Assert.assertThat(home.getOwnerId(), is(security.getUserId()));
        assertNull(home.getParentId());
    }

    @Test
    public void shouldGetFolderById() {
        // given
        final Folder jsoFolder = getFolderRepository().addFolder(homeFolderId, "jso");

        // when
        final Folder fetchedFolder = getFolderRepository().getFolderById(jsoFolder.getId());

        // then
        Assert.assertThat(fetchedFolder, equalTo(jsoFolder));
    }

    private void assertChildrenSize(String folder, int childrenNumber) {
        List<Folder> folders = getChildren(folder);
        if (childrenNumber > 0) {
            assertThat(folders).isNotNull().isNotEmpty().hasSize(childrenNumber);
        } else {
            assertThat(folders).isNotNull().isEmpty();
        }
    }

    private void assertOnSearch(final String query, final boolean strict, final int foundNumber) {
        try (Stream<Folder> folderStream = getFolderRepository().searchFolders(query, strict)) {
            Iterable<Folder> folders = folderStream.collect(Collectors.toList());
            assertThat(Lists.newArrayList(folders)).isNotNull().isNotEmpty().hasSize(foundNumber);
        }
    }

}
