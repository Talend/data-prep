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
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

/**
 * Unit test for the org.talend.dataprep.upgrade.to_One_Two_Zero_PE.ComputePreparationId class.
 *
 * @see ComputePreparationId
 */
public class ComputePreparationIdTest extends Base_1_2_0_PE_Test {

    /** The task to test. */
    @Autowired
    private ComputePreparationId task;

    /** The preparation repository. */
    @Autowired
    private PreparationRepository preparationRepository;

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
        return 2;
    }

    @Test
    public void shouldComputePreparationsId() throws Exception {
        // given
        final Stream<Preparation> initialPreparations = preparationRepository.list(Preparation.class);
        final List<String> initialIds = initialPreparations.map(Preparation::getId).collect(Collectors.toList());

        // when
        task.run();

        // then
        final Stream<Preparation> preparations = preparationRepository.list(Preparation.class);
        final List<String> updatedIds = preparations.map(Preparation::getId).collect(Collectors.toList());
        assertEquals(initialIds.size(), updatedIds.size());
        updatedIds.forEach(id -> assertFalse(initialIds.contains(id)));

    }
}