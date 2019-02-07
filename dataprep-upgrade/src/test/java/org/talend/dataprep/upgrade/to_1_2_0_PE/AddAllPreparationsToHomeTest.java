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
import static org.talend.dataprep.api.folder.FolderContentType.PREPARATION;

import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.folder.Folder;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.folder.store.FolderRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

/**
 * Unit test for the org.talend.dataprep.upgrade.to_One_Two_Zero_PE.AddAllPreparationsTaskToHome class.
 *
 * @see AddAllPreparationsTaskToHome
 */
public class AddAllPreparationsToHomeTest extends Base_1_2_0_PE_Test {

    /** The task to test. */
    @Autowired
    private AddAllPreparationsTaskToHome task;

    /** The preparation repository. */
    @Autowired
    private PreparationRepository preparationRepository;

    /** The folder repository. */
    @Autowired
    private FolderRepository folderRepository;

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
        return 3;
    }

    @Test
    public void shouldAddAllPreparationsToHome() throws Exception {

        // given
        final String homeFolderId = folderRepository.getOrCreateHome().getId();

        // when
        task.run();

        // then
        final Stream<Preparation> preparations = preparationRepository.list(Preparation.class);
        preparations.forEach(p -> {
            final Folder folder = folderRepository.locateEntry(p.getId(), PREPARATION);
            assertEquals(homeFolderId, folder.getId());
        });

    }
}
