//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.folder.store.file;

import java.util.Base64;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.talend.dataprep.folder.store.AbstractFolderTest;
import org.talend.dataprep.folder.store.FolderRepository;

@TestPropertySource(inheritLocations = false, inheritProperties = false, properties = { "folder.store=file",
        "folder.store.file.location=target/test/store/folders" })
public class FileSystemFolderRepositoryTest extends AbstractFolderTest {

    @Inject
    @Named("folderRepository#file")
    private FolderRepository folderRepository;

    /** Where to store the folders */
    @Value("${folder.store.file.location}")
    private String foldersLocation;

    @Override
    protected FolderRepository getFolderRepository() {
        return folderRepository;
    }

    /**
     * @see AbstractFolderTest#pathToId(String)
     */
    @Override
    protected String pathToId(String path) {
        return Base64.getEncoder().encodeToString(path.getBytes());
    }
}
