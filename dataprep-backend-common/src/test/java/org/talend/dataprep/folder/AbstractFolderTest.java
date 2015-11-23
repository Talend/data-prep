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

public abstract class AbstractFolderTest {

    protected abstract FolderRepository getFolderRepository();

    @After
    public void cleanAfter(){
        getFolderRepository().clear();
    }

    /**
     *
     * This test create one child under root assert size, child list then delete
     */
    @Test
    public void create_child_then_remove() throws Exception {


        int sizeBefore = getFolderRepository().size();

        Folder child = getFolderRepository().addFolder( "foo");

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

        int sizeBefore = getFolderRepository().size();


        Folder foo = getFolderRepository().addFolder("foo");

        Folder beer = getFolderRepository().addFolder("beer");

        Folder bar = getFolderRepository().addFolder("beer/bar");

        int sizeAfter = getFolderRepository().size();

        List<Folder> folders = new ArrayList<>();
        Iterable<Folder> iterable =  getFolderRepository().allFolder();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotEmpty().hasSize( sizeBefore + 3);

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 3);

        iterable = getFolderRepository().childs("");
        folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(2);

        // testing child of /bar

        iterable = getFolderRepository().childs("/beer");
        folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat( folders.get( 0 ).getPath() ).isEqualToIgnoringCase( "beer/bar" );
        Assertions.assertThat( folders.get( 0 ).getName() ).isEqualToIgnoringCase( "bar" );

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

        Folder foo = getFolderRepository().addFolder("foo");

        Folder foobeer = getFolderRepository().addFolder("foo/beer");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 2);

        assertChildsSize( "", 1 );

        FolderEntry beerEntry = new FolderEntry(DataSet.class.getName(), "littlecreatures", "/foo");

        FolderEntry wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo");

        getFolderRepository().addFolderEntry(beerEntry);

        getFolderRepository().addFolderEntry(wineEntry);


        wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo/beer");

        getFolderRepository().addFolderEntry(wineEntry);

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        List<FolderEntry> entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);


        folderEntries = getFolderRepository().findFolderEntries( "bordeaux", DataSet.class.getName() );
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries( "littlecreatures", DataSet.class.getName() );
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolderEntry("foo", "littlecreatures", DataSet.class.getName());
            
        folderEntries = getFolderRepository().entries("/foo", DataSet.class.getName());
        entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolder("/foo");

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore);

        assertChildsSize( "", 0 );

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

        assertChildsSize( "", 2 );


        FolderEntry wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo");

        getFolderRepository().addFolderEntry(wineEntry);

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        List<FolderEntry> entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains( wineEntry );

        getFolderRepository().copyFolderEntry( wineEntry, "bar" );

        folderEntries = getFolderRepository().entries("bar", DataSet.class.getName());
        entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        // path has changed
        wineEntry.setPath( "bar" );
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains( wineEntry );

        // still in foo as it's a copy
        folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        entries = new ArrayList<>();
        folderEntries.forEach(entries::add);


        wineEntry.setPath( "foo" );
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains( wineEntry );

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

        assertChildsSize( "", 2 );

        FolderEntry wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo");

        getFolderRepository().addFolderEntry(wineEntry);

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        List<FolderEntry> entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains( wineEntry );

        getFolderRepository().moveFolderEntry( wineEntry, "bar" );

        folderEntries = getFolderRepository().entries("bar", DataSet.class.getName());
        entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        // new path is bar for assert
        wineEntry.setPath( "bar" );

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1).contains( wineEntry );

        // not in foo as it's a move
        folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        Assertions.assertThat(entries).isNotNull().isEmpty();

    }

    @Test
    public void rename_folder_with_entries_and_subfolders() throws Exception{

        int sizeBefore = getFolderRepository().size();

        Folder foo = getFolderRepository().addFolder("foo");

        Folder foobeer = getFolderRepository().addFolder("foo/beer");

        Folder foobar = getFolderRepository().addFolder("foo/bar");

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 3);

        assertChildsSize( "", 1 );

        FolderEntry beerEntry = new FolderEntry(DataSet.class.getName(), "littlecreatures", "/foo");

        FolderEntry wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo");

        getFolderRepository().addFolderEntry(beerEntry);

        getFolderRepository().addFolderEntry(wineEntry);


        wineEntry = new FolderEntry(DataSet.class.getName(), "bordeaux", "foo/beer");

        getFolderRepository().addFolderEntry(wineEntry);

        Iterable<FolderEntry> folderEntries = getFolderRepository().entries("foo", DataSet.class.getName());
        List<FolderEntry> entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);


        folderEntries = getFolderRepository().findFolderEntries( "bordeaux", DataSet.class.getName() );
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(2);

        folderEntries = getFolderRepository().findFolderEntries( "littlecreatures", DataSet.class.getName() );
        entries.clear();
        folderEntries.forEach(entries::add);
        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolderEntry("foo", "littlecreatures", DataSet.class.getName());

        folderEntries = getFolderRepository().entries("/foo", DataSet.class.getName());
        entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        assertChildsSize( "/foo", 2 );

        getFolderRepository().renameFolder( "/foo", "/wine" );

        assertChildsSize( "/wine", 2 );

        // test FolderEntry moved as well
        folderEntries = getFolderRepository().entries("/wine", DataSet.class.getName());
        entries = new ArrayList<>();
        folderEntries.forEach(entries::add);

        Assertions.assertThat(entries).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolder("/wine");

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore);

        assertChildsSize( "/", 0 );

    }

    protected void assertChildsSize(String folder, int childsNumber){
        Iterable<Folder> iterable = getFolderRepository().childs(folder);
        List<Folder> folders = new ArrayList<>();
        iterable.forEach(folders::add);
        if (childsNumber>0) {
            Assertions.assertThat( folders ).isNotNull().isNotEmpty().hasSize( childsNumber );
        } else {
            Assertions.assertThat( folders ).isNotNull().isEmpty();
        }
    }
}
