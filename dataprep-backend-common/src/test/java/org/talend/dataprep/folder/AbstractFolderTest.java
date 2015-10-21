package org.talend.dataprep.folder;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.folder.store.FolderRepository;

public abstract class AbstractFolderTest {

    protected abstract FolderRepository getFolderRepository();

    @Test
    public void create_child_then_remove() throws Exception {
        Folder rootFolder = Folder.Builder.folder().name("").build();

        int sizeBefore = getFolderRepository().size();

        Folder child = Folder.Builder.folder().name("foo").build();

        child = getFolderRepository().addFolder(rootFolder, child);

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 1);

        Iterable<Folder> iterable = getFolderRepository().childs(rootFolder);
        List<Folder> folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(1);

        getFolderRepository().removeFolder(child);

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore);

        iterable = getFolderRepository().childs(rootFolder);
        folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isEmpty();

    }

    @Test
    public void create_two_childs_little_child_then_remove() throws Exception {

        //  - foo
        //  - beer
        //        - bar


        Folder rootFolder = Folder.Builder.folder().name("").build();

        int sizeBefore = getFolderRepository().size();

        Folder foo = Folder.Builder.folder().name("foo").build();

        getFolderRepository().addFolder(rootFolder, foo);

        Folder beer = Folder.Builder.folder().name("beer").build();

        beer = getFolderRepository().addFolder(rootFolder, beer);

        Folder bar = Folder.Builder.folder().name("bar").build();

        bar = getFolderRepository().addFolder(beer, bar);

        int sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 3);

        Iterable<Folder> iterable = getFolderRepository().childs(rootFolder);
        List<Folder> folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isNotEmpty().hasSize(2);

        getFolderRepository().removeFolder(beer);

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore + 1);

        getFolderRepository().removeFolder(foo);

        sizeAfter = getFolderRepository().size();

        Assertions.assertThat(sizeAfter).isEqualTo(sizeBefore);

        iterable = getFolderRepository().childs(rootFolder);
        folders = new ArrayList<>();
        iterable.forEach(folders::add);

        Assertions.assertThat(folders).isNotNull().isEmpty();

    }

}
