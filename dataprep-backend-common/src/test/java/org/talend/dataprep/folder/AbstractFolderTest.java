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
        Folder rootFolder = getFolderRepository().rootFolder();

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

}
