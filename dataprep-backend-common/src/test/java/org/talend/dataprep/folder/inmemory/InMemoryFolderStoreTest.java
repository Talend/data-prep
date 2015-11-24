package org.talend.dataprep.folder.inmemory;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.folder.AbstractFolderTest;
import org.talend.dataprep.folder.store.FolderRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMemoryFolderStoreTest.class)
@ComponentScan(basePackages = "org.talend.dataprep")
@TestPropertySource(inheritLocations = false, inheritProperties = false, properties = { "folder.store=in-memory" })
public class InMemoryFolderStoreTest extends AbstractFolderTest {

    @Inject
    @Named("folderRepository#in-memory")
    private FolderRepository folderRepository;

    @Before
    public void init() throws IOException {

    }

    @Override
    protected FolderRepository getFolderRepository() {
        return folderRepository;
    }
}
