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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.talend.dataprep.upgrade.model.UpgradeTask.target.VERSION;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.dataset.store.metadata.DataSetMetadataRepository;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

public class ToPEPersistentIdentifiableTest extends Base_2_1_0_PE_Test {

    /** The task to test. */
    @Autowired
    private ToPEPersistentIdentifiable task;

    @Autowired
    private PreparationRepository repository;

    @Autowired
    private DataSetMetadataRepository dataSetMetadataRepository;

    @Autowired
    private DataSetMetadataBuilder builder;

    @Override
    protected UpgradeTaskId getTaskId() {
        return task.getId();
    }

    @Override
    protected int getExpectedTaskOrder() {
        return 1;
    }

    @Test
    public void shouldUpdatePersistentClasses() {
        // when
        task.run();

        // then
        assertEquals(2, repository.list(PersistentPreparation.class).count());
        List<PersistentPreparation> persistentPreparationList = repository
                .list(PersistentPreparation.class)
                .sorted(Comparator.comparingInt(p1 -> p1.getSteps().size()))
                .collect(Collectors.toList());
        persistentPreparationList.forEach(p -> {
            assertNotNull(p.getSteps());
            assertNotNull(p.getRowMetadata());
            assertFalse(p.getSteps().isEmpty());
            assertEquals(Type.INTEGER.getName(), p.getRowMetadata().getColumns().get(0).getType());
        });
        assertEquals(6, persistentPreparationList.get(0).getRowMetadata().getColumns().size());
        assertEquals(5, persistentPreparationList.get(1).getRowMetadata().getColumns().size());

        repository.list(Preparation.class).forEach(p -> {
            // make sure preparation steps are read
            assertNotNull(p.getSteps());
            assertFalse(p.getSteps().isEmpty());
        });

        // root step is filtered
        assertEquals(10, repository.list(PersistentStep.class).filter(s -> s.getParentId() != null).count());
        repository.list(Step.class).forEach(s -> assertNotNull(s.getId()));
    }

    @Test
    public void targetShouldBeVersion() throws Exception {
        assertEquals(VERSION, task.getTarget());
    }
}