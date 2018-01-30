/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */
package org.talend.dataprep.upgrade.to_2_1_0_PE;

import static org.junit.Assert.assertEquals;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

public class StepListPreparationsMigrationTest extends Base_2_1_0_PE_Test {

    @Autowired
    private StepListPreparationsMigration task;

    @Autowired
    private PreparationRepository preparationRepository;

    @Test
    public void shouldUpdateStepMetadata() {
        // when
        task.run();

        // then
        List<PersistentPreparation> preparationList = preparationRepository.list(PersistentPreparation.class)
                .sorted(Comparator.comparingInt(o -> o.getSteps().size())).collect(Collectors.toList());

        assertEquals(4, preparationList.get(0).getSteps().size());
        assertEquals(8, preparationList.get(1).getSteps().size());
    }

    @Override
    protected UpgradeTaskId getTaskId() {
        return task.getId();
    }

    @Override
    protected int getExpectedTaskOrder() {
        return 6;
    }
}
