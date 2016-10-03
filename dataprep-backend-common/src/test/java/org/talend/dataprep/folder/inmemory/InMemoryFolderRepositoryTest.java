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

package org.talend.dataprep.folder.inmemory;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.dataprep.folder.AbstractFolderTest;
import org.talend.dataprep.folder.store.FolderRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = InMemoryFolderRepositoryTest.class)
@ComponentScan(basePackages = "org.talend.dataprep")
@TestPropertySource(inheritLocations = false, inheritProperties = false, properties = { "folder.store=in-memory" })
public class InMemoryFolderRepositoryTest extends AbstractFolderTest {

    @Inject
    @Named("folderRepository#in-memory")
    private FolderRepository folderRepository;

    @Override
    protected FolderRepository getFolderRepository() {
        return folderRepository;
    }

    /**
     * @see AbstractFolderTest#pathToId(String)
     */
    @Override
    protected String pathToId(String path) {
        return path;
    }


}
