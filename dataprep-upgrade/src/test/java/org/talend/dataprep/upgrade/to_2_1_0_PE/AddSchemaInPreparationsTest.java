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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.preparation.store.file.FileSystemPreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

public class AddSchemaInPreparationsTest extends Base_2_1_0_PE_Test {

    @Autowired
    private PreparationRepository preparationRepository;

    @Autowired
    private DataSetMetadataRepository dataSetMetadataRepository;

    /** The task to test. */
    @Autowired
    private AddSchemaInPreparations task;

    private FileSystemPreparationRepository fileSystemPreparationRepository;

    @PostConstruct
    private void postInitialize() throws IllegalAccessException {
        fileSystemPreparationRepository =
                (FileSystemPreparationRepository) FieldUtils.readField(preparationRepository, "delegate", true);
    }

    @Override
    protected UpgradeTaskId getTaskId() {
        return task.getId();
    }

    @Override
    protected int getExpectedTaskOrder() {
        return 0;
    }

    @Test
    public void targetShouldBeVersion() throws Exception {
        assertEquals(VERSION, task.getTarget());
    }

    @Test
    public void integrationTest() throws Exception {
        // given
        assertTrue(fileSystemPreparationRepository.list(Preparation.class).allMatch(p -> p.getRowMetadata() == null));

        // when
        task.run();

        // then
        assertTrue(fileSystemPreparationRepository.list(Preparation.class).allMatch(p -> p.getRowMetadata() != null));
    }

}
