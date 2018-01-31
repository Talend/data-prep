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

package org.talend.dataprep.upgrade.to_1_3_0_PE;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;
import org.talend.dataprep.upgrade.to_1_2_0_PE.Base_1_2_0_PE_Test;

/**
 * Unit test for the org.talend.dataprep.upgrade.to_1_3_0_PE.ChangeUserIdToTargetId class.
 *
 * @see ChangeUserIdToTargetId
 */
public class ChangeUserIdToTargetIdTest extends Base_1_3_0_PE_Test {

    /** The task to test. */
    @Autowired
    private ChangeUserIdToTargetId task;

    /** Where to store the dataset metadata */
    @Value("${upgrade.store.file.location}")
    private String storeLocation;

    /**
     * @see Base_1_3_0_PE_Test#getTaskId()
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
        return 0;
    }

    @Test
    public void shouldRenameUpgradeUserFolders() throws Exception {
        // given
        List<String> expected = Arrays.asList("USER-chuck norris", "USER-mr anonymous", "USER-william from arcachon",
                "VERSION-1.3.0");

        // when
        task.run();

        // then
        Path rootFolder = Paths.get(storeLocation);
        List<String> actual = Arrays.asList(rootFolder.toFile().list());
        assertTrue(actual.containsAll(expected) && expected.containsAll(actual));

    }
}