// ============================================================================
//
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

package org.talend.dataprep.upgrade.to_1_2_0_PE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

/**
 * Unit test for the org.talend.dataprep.upgrade.to_One_Two_Zero_PE.RemoveAllFolders class.
 *
 * @see RemoveAllFolders
 */
public class RemoveAllFoldersTest extends Base_1_2_0_PE_Test {

    /** The task to test. */
    @Autowired
    private RemoveAllFolders task;

    /** The folder repository. */
    @Autowired
    private FolderRepository repository;

    @Test
    public void shouldRemoveFolders() throws Exception {
        // given
        assertTrue(repository.size() > 0);

        // when
        task.run();

        // then
        assertEquals(0, repository.size());
    }

    /**
     * @see Base_1_2_0_PE_Test#getTaskId()
     */
    @Override
    protected UpgradeTaskId getTaskId() {
        return task.getId();
    }

    /**
     * @see Base_1_2_0_PE_Test#getExpectedTaskOrder()
     */
    @Override
    protected int getExpectedTaskOrder() {
        return 1;
    }

}