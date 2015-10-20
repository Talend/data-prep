package org.talend.dataprep.folder.file;

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.folder.AbstractFolderTest;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FileFolderStoreTest.class)
@ComponentScan(basePackages = "org.talend.dataprep.folder")
@TestPropertySource(inheritLocations = false, inheritProperties = false, properties = { "folder.store=file",
        "folder.store.file.location=target/test/store/folders" })
public class FileFolderStoreTest extends AbstractFolderTest {
}
