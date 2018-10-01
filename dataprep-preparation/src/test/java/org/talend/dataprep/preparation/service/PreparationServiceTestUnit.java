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

import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.talend.ServiceBaseTest.TEST_LOCALE;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.talend.ServiceBaseTest;
import org.talend.daikon.content.local.LocalContentServiceConfiguration;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.audit.BaseDataprepAuditService;
import org.talend.dataprep.audit.NoOpAuditService;
import org.talend.dataprep.configuration.DataPrepComponentScanConfiguration;
import org.talend.dataprep.lock.store.NoOpLockedResourceRepository;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.NoOpSecurity;

/**
 * Unit/integration tests for the PreparationService
 */
@RunWith(SpringRunner.class)
//@Import({ LocalContentServiceConfiguration.class, DataPrepComponentScanConfiguration.class })
@SpringBootTest(webEnvironment = RANDOM_PORT,
        properties = { "dataset.asynchronous.analysis=false", "content-service.store=local",
                "dataprep.locale:" + TEST_LOCALE })
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
    public void shouldAddAction(){

        // preparation
        PersistentPreparation persistentPrep = new PersistentPreparation();
        persistentPrep.setId("prepId");
        persistentPrep.setHeadId("stepId");

        ColumnMetadata columnMetadata = column().id(1).name("1234").type(Type.STRING).build();
        RowMetadata rowMetadata = new RowMetadata(Collections.singletonList(columnMetadata));
        persistentPrep.setRowMetadata(rowMetadata);

        // steps
        PersistentStep persitentStep = new PersistentStep();
        persitentStep.setId("stepId");

        // actions
        PreparationActions prepActions = new PreparationActions();
        final List<Action> actions = getSimpleAction("uppercase", "column_name", "lastname");
        prepActions.setActions(actions);

        AppendStep appendStep = new AppendStep();

        repository.add(persistentPrep);
        repository.add(persitentStep);
        repository.add(prepActions);

        ReflectionTestUtils.setField(preparationService, "stepDiffDelegate", stepDiffDelegate);
        ReflectionTestUtils.setField(preparationService, "lockedResourceRepository", lockedResourceRepository);
        ReflectionTestUtils.setField(preparationService, "security", security);
        ReflectionTestUtils.setField(preparationService, "preparationUtils", preparationUtils);

        preparationService.addPreparationAction("prepId", appendStep);

        PersistentPreparation after = repository.get("prepId", PersistentPreparation.class);
        after.getSteps().isEmpty();
    }

    public static List<Action> getSimpleAction(final String actionName, final String paramKey,
                                               final String paramValue) {
        final Action action = new Action();
        action.setName(actionName);
        action.getParameters().put(paramKey, paramValue);

        final List<Action> actions = new ArrayList<>();
        actions.add(action);

        return actions;
    }
}
