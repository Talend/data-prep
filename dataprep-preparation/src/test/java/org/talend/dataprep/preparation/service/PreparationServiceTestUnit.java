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

package org.talend.dataprep.preparation.service;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.talend.ServiceBaseTest.TEST_LOCALE;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.lock.store.NoOpLockedResourceRepository;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.NoOpSecurity;

/**
 * Unit/integration tests for the PreparationService
 */
@RunWith(SpringRunner.class)
// @Import({ LocalContentServiceConfiguration.class, DataPrepComponentScanConfiguration.class })
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = { "dataset.asynchronous.analysis=false",
        "content-service.store=local", "dataprep.locale:" + TEST_LOCALE })
@TestPropertySource(properties = { "audit.log.enabled = false" })
public class PreparationServiceTestUnit {

    @Autowired
    private PreparationService preparationService;

    @Autowired
    private PreparationRepository repository;

    private NoOpLockedResourceRepository lockedResourceRepository = new NoOpLockedResourceRepository();

    private NoOpSecurity security = new NoOpSecurity();

    private PreparationUtils preparationUtils = new PreparationUtils();

    private MetadataChangesOnActionsGenerator stepDiffDelegate = new MetadataChangesOnActionsGenerator();

    @Test
    public void shouldAddAction() {

        // given
        ReflectionTestUtils.setField(preparationService, "stepDiffDelegate", stepDiffDelegate);
        ReflectionTestUtils.setField(preparationService, "lockedResourceRepository", lockedResourceRepository);
        ReflectionTestUtils.setField(preparationService, "security", security);
        ReflectionTestUtils.setField(preparationService, "preparationUtils", preparationUtils);

        // init preparation
        PersistentPreparation persistentPrepWithRootStep = new PersistentPreparation();
        persistentPrepWithRootStep.setId("prepId");
        persistentPrepWithRootStep.setHeadId(Step.ROOT_STEP.id());

        ColumnMetadata columnMetadata1 = column().id(0).name("col 1").type(Type.STRING).build();
        ColumnMetadata columnMetadata2 = column().id(1).name("col 2").type(Type.INTEGER).build();
        ColumnMetadata columnMetadata3 = column().id(2).name("col 3").type(Type.DATE).build();
        RowMetadata rowMetadata = new RowMetadata(
                Stream.of(columnMetadata1, columnMetadata2, columnMetadata3).collect(Collectors.toList()));
        persistentPrepWithRootStep.setRowMetadata(rowMetadata);

        // test that root step and root action are before to add the action
        assertEquals(1, repository.list(PersistentStep.class).count());
        assertEquals(1, repository.list(PreparationActions.class).count());
        repository.add(persistentPrepWithRootStep);

        // init action of the append step
        PreparationActions prepActionsListToAdd = new PreparationActions();
        final Action action = new Action();
        action.setName("uppercase");
        action.getParameters().put("column_id", "0000");
        action.getParameters().put("column_name", "col 1");
        action.getParameters().put("create_new_column", "true");
        action.getParameters().put("row_id", "null");
        action.getParameters().put("scope", "column");
        final List<Action> actionsStart = new ArrayList<>();
        prepActionsListToAdd.setActions(actionsStart);

        // init append step to add
        AppendStep appendStep = new AppendStep();
        List<Action> emptyActionsList = new ArrayList<>();
        emptyActionsList.add(action);
        appendStep.setActions(emptyActionsList);

        // test that there is nothing else root step and root action before to add the action
        PersistentPreparation beforeAddAction = repository.get("prepId", PersistentPreparation.class);
        assert (beforeAddAction.getSteps().size() == 1);
        assert (beforeAddAction.getSteps().iterator().next().equals(Step.ROOT_STEP.getId()));
        assertEquals(1, repository.list(PersistentStep.class).count());
        assertEquals(1, repository.list(PreparationActions.class).count());

        // when
        preparationService.addPreparationAction("prepId", appendStep);

        // then
        PersistentPreparation afterAddAction = repository.get("prepId", PersistentPreparation.class);
        assertEquals(2, afterAddAction.getSteps().size());
        // headId should have been changed to the last step
        assertEquals(afterAddAction.getHeadId(), afterAddAction.getSteps().get(1));

        // check that step has been added to the repository
        assertEquals(2, repository.list(PersistentStep.class).count());
        PersistentStep addedStep = repository.get(afterAddAction.getHeadId(), PersistentStep.class);
        assertEquals(addedStep.getParentId(), Step.ROOT_STEP.id());

        // check that action has been added to the repository
        assertEquals(2, repository.list(PreparationActions.class).count());
        PreparationActions addedAcions = repository.get(addedStep.getContent(), PreparationActions.class);
        assertEquals(1, addedAcions.getActions().size());
        Action appendAction = addedAcions.getActions().get(0);
        assertEquals("uppercase", appendAction.getName());

        // check that the step diff is computed
        assertEquals(1, addedStep.getDiff().getCreatedColumns().size());
        assertEquals("0003", addedStep.getDiff().getCreatedColumns().get(0));
    }

}
