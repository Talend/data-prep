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

package org.talend.dataprep.upgrade.to_2_1_0_PE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.StepRowMetadata;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

public class StepRowMetadataMigrationTest extends Base_2_1_0_PE_Test {

    @Autowired
    private StepRowMetadataMigration task;

    @Autowired
    private PreparationRepository preparationRepository;

    @Test
    public void shouldUpdateStepMetadata() {
        // when
        task.run();

        // then
        final Stream<StepRowMetadata> list = preparationRepository.list(StepRowMetadata.class);
        assertEquals(10, list.count());
        preparationRepository.list(PersistentStep.class) //
                .filter(s -> s.getRowMetadata() != null)
                .forEach(s -> assertNotNull(preparationRepository.get(s.getRowMetadata(), StepRowMetadata.class)));

    }

    @Override
    protected UpgradeTaskId getTaskId() {
        return task.getId();
    }

    @Override
    protected int getExpectedTaskOrder() {
        return 5;
    }
}