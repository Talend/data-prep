package org.talend.dataprep.folder.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.folder.AbstractFolderTest;
import org.talend.dataprep.folder.store.FolderRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FileFolderStoreTest.class)
@ComponentScan(basePackages = "org.talend.dataprep")
@TestPropertySource(inheritLocations = false, inheritProperties = false, properties = { "folder.store=file",
        "folder.store.file.location=target/test/store/folders" })
public class FileFolderStoreTest extends AbstractFolderTest {

    @Inject
    private ApplicationContext applicationContext;

    /** Where to store the folders */
    @Value("${folder.store.file.location}")
    private String foldersLocation;

    @Before
    public void init() throws IOException {
        Path path = Paths.get(foldersLocation);
        if (Files.exists(path)) {
            FileUtils.deleteDirectory(path.toFile());
        }
    }

    @Override
    protected FolderRepository getFolderRepository() {
        return applicationContext.getBean("folderRepository#file", FolderRepository.class);
    }
}
