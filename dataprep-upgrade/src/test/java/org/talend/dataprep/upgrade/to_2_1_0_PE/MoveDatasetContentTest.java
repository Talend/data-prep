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

package org.talend.dataprep.upgrade.to_2_1_0_PE;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

public class MoveDatasetContentTest extends Base_2_1_0_PE_Test {

    @Autowired
    private MoveDatasetContent task;

    @Before
    public void initTest() throws IOException {
        // before each test we delete new root directory if exist
        File newRootDirectory = new File(task.newRoot);
        if (newRootDirectory.exists()) {
            FileUtils.deleteDirectory(newRootDirectory);
        }
    }

    @Test
    public void shouldUpdateStepRowMetadata() throws Exception {

        // given
        Assert.assertTrue(new File(task.oldRoot).exists());
        Assert.assertFalse(new File(task.newRoot).exists());

        // when
        task.run();

        // then
        Assert.assertTrue(new File(task.newRoot).exists());
    }

    @Test
    public void testDoNotNeedToApplyUpgrade() throws IOException {

        // given
        Assert.assertTrue(new File(task.oldRoot).exists());

        File newRootDirectory = new File(task.newRoot);

        if (!newRootDirectory.exists()) {
            FileUtils.forceMkdir(newRootDirectory);
            Assert.assertTrue(newRootDirectory.exists());
        }

        // when

        // then
        Assert.assertFalse(task.haveToBeApply());
    }

    @Test
    public void testNeedToApplyUpgrade() {
        // given
        Assert.assertTrue(new File(task.oldRoot).exists());
        Assert.assertFalse(new File(task.newRoot).exists());

        // when

        // then
        Assert.assertTrue(task.haveToBeApply());

    }

    /**
     * @return the task id.
     */
    @Override
    protected UpgradeTaskId getTaskId() {
        return task.getId();
    }

    /**
     * @return the expected task order.
     */
    @Override
    protected int getExpectedTaskOrder() {
        return 2;
    }
}