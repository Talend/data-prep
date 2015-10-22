package org.talend.dataprep.folder;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.folder.FolderEntry;
import org.talend.dataprep.folder.store.FolderRepository;

public abstract class AbstractFolderTest {

    protected abstract FolderRepository getFolderRepository();

    /**
     *
     * This test create one child under root assert size, child list then delete
     */
    @Test
    public void create_child_then_remove() throws Exception {


        int sizeBefore = getFolderRepository().size();

        Folder child = getFolderRepository().addFolder("/", "foo");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 1);

        Iterable<Folder> iterable = getFolderRepository().childs("");
        List<Folder> folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolder("/foo");

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore);

        iterable = getFolderRepository().childs("");
        folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isEmpty();

    }

    /**
     * this test create a hierarchy then delete part of it doing some assert on list, size then delete part of it
     * asserting the deletion
     */
    @Test
    public void create_two_childs_little_child_then_remove() throws Exception {

        // - foo
        // - beer-
        //       | - bar

        Folder rootFolder = Folder.Builder.folder().name("").build();

        int sizeBefore = getFolderRepository().size();


        Folder foo = getFolderRepository().addFolder("", "foo");

        Folder beer = getFolderRepository().addFolder("", "beer");

        Folder bar = getFolderRepository().addFolder("beer", "bar");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 3);

        Iterable<Folder> iterable = getFolderRepository().childs("");
        List<Folder> folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(2);

        // testing child of /bar

        iterable = getFolderRepository().childs("/beer");
        folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolder("/beer/bar");

        // testing child of /beer after removing the first child
        iterable = getFolderRepository().childs("/beer");
        folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isEmpty();


        // testing the whole size

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(2);

        getFolderRepository().removeFolder("/foo");

        getFolderRepository().removeFolder("/beer");

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore);

        iterable = getFolderRepository().childs("");
        folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isEmpty();

    }

    /**
     *
     * This test create one child under root assert size, child list then create two folder entries then delete
     */
    @Test
    public void create_child_with_two_entries_then_remove() throws Exception {


        int sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("", "foo");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 1);

        Iterable<Folder> iterable = getFolderRepository().childs("");
        List<Folder> folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(1);

        FolderEntry beerEntry = new FolderEntry("beer", DataSet.class.getName(), "littlecreatures");

        FolderEntry wineEntry = new FolderEntry("wine", DataSet.class.getName(), "bordeaux");

        getFolderRepository().addFolderEntry("/foo", beerEntry);

        getFolderRepository().addFolderEntry("foo", wineEntry);

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        List<FolderEntry> entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        getFolderRepository().removeFolderEntry( "/foo", beerEntry );

        folderEntries = getFolderRepository().entries("/foo", DataSet.class.getName());
        entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolder("/foo");

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore);

        iterable = getFolderRepository().childs("");
        folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isEmpty();

    }

}
