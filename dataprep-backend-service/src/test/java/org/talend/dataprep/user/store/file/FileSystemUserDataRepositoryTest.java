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

package org.talend.dataprep.user.store.file;

import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.talend.dataprep.api.user.UserData;
import org.talend.dataprep.user.store.AbstractUserDataRepositoryTest;
import org.talend.dataprep.user.store.UserDataRepository;

/**
 * Unit test for the FileSystemUserDataRepository.
 *
 * @see FileSystemUserDataRepository
 */
@TestPropertySource(inheritLocations = false, inheritProperties = false, properties = { "user.data.store:file",
        "user.data.store.file.location:target/test/store/userdata" })
public class FileSystemUserDataRepositoryTest extends AbstractUserDataRepositoryTest<UserData> {

    /** The user data repository to test. */
    @Autowired
    private FileSystemUserDataRepository repository;

    @Override
    protected UserData getRandomUserData() {
        UserData userData = new UserData();
        userData.setUserId(UUID.randomUUID().toString());
        userData.setAppVersion("12.3");
        return userData;
    }

    @Override
    protected UserDataRepository getUserRepository() {
        return repository;
    }

    @Test
    public void shouldIgnoreHiddenFiles() throws Exception {
        // given
        repository.clear();

        // when
        File hidden = new File("target/test/store/userdata/.hidden_file");
        FileOutputStream fos = new FileOutputStream(hidden);
        fos.write("hello".getBytes());

        // then
        final UserData userData = repository.get(".hidden");
        hidden.delete();
        assertNull(userData);

    }
}
