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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.COLUMN_ID;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.SCOPE;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.audit.BaseDataprepAuditService;
import org.talend.dataprep.lock.store.NoOpLockedResourceRepository;
import org.talend.dataprep.preparation.store.PersistentPreparation;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.NoOpSecurity;
import org.talend.dataprep.transformation.actions.text.LowerCase;
import org.talend.dataprep.transformation.actions.text.UpperCase;
import org.talend.dataprep.transformation.api.action.validation.ActionMetadataValidation;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

/**
 * Unit/integration tests for the PreparationService
 */
@RunWith(MockitoJUnitRunner.class)
public class PreparationServiceTestMock {

    @InjectMocks
    private PreparationService preparationService;

    @Mock
    private PreparationRepository preparationRepositoryMock;

    @Mock
    private BaseDataprepAuditService auditServiceMock;

    @Mock
    private ActionRegistry actionRegistry;

    @Mock
    private ActionMetadataValidation validator;

    private NoOpLockedResourceRepository lockedResourceRepository = new NoOpLockedResourceRepository();

    private NoOpSecurity security = new NoOpSecurity();

    private PreparationUtils preparationUtils = new PreparationUtils();

    private MetadataChangesOnActionsGenerator stepDiffDelegate = new MetadataChangesOnActionsGenerator();

    @Test
    public void shouldAddAction() throws IllegalAccessException, InstantiationException {

        // preparation
        PersistentPreparation persistentPrep = new PersistentPreparation();
        persistentPrep.setId("prepId");
        persistentPrep.setHeadId("stepId");
        List<ColumnMetadata> columnMetadatas = new ArrayList<>();
        columnMetadatas.add(column().id(1).name("1234").type(Type.STRING).build());
        columnMetadatas.add(column().id(2).name("city").type(Type.STRING).build());
        RowMetadata rowMetadata = new RowMetadata(columnMetadatas);
        persistentPrep.setRowMetadata(rowMetadata);

        // steps
        PersistentStep persitentStep = new PersistentStep();
        persitentStep.setId("stepId");

        // actions
        PreparationActions prepActions = new PreparationActions();
        final List<Action> startingActions = new ArrayList<>();
        startingActions.add(getSimpleAction("uppercase", "column_name", "1234", "1"));
        prepActions.setActions(startingActions);

        final List<Action> actionToAppend = new ArrayList<>();
        actionToAppend.add(getSimpleAction("uppercase", "column_name", "1234", "1"));
        actionToAppend.add(getSimpleAction("lowercase", "column_name", "1234", "1"));
        prepActions.setActions(actionToAppend);
        AppendStep appendStep = new AppendStep();
        appendStep.setActions(actionToAppend);

        when(preparationRepositoryMock.get("prepId", PersistentPreparation.class)).thenReturn(persistentPrep);
        when(preparationRepositoryMock.get("stepId", PersistentStep.class)).thenReturn(persitentStep);
        when(preparationRepositoryMock.get(persitentStep.getContent(), PreparationActions.class)).thenReturn(prepActions);
        when(auditServiceMock.isActive()).thenReturn(false);
        when(actionRegistry.get("uppercase")).thenReturn(UpperCase.class.newInstance());
        when(actionRegistry.get("lowercase")).thenReturn(LowerCase.class.newInstance());
        // when(validator.checkScopeConsistency(any(),any())));

        ReflectionTestUtils.setField(preparationService, "stepDiffDelegate", stepDiffDelegate);
        ReflectionTestUtils.setField(preparationService, "lockedResourceRepository", lockedResourceRepository);
        ReflectionTestUtils.setField(preparationService, "security", security);
        ReflectionTestUtils.setField(preparationService, "preparationUtils", preparationUtils);

        preparationService.addPreparationAction("prepId", appendStep);
        persistentPrep.getSteps().isEmpty();
    }

    private Action getSimpleAction(final String actionName, final String paramKey, final String paramValue,
            final String columnId) {
        final Action action = new Action();
        action.setName(actionName);
        action.getParameters().put(paramKey, paramValue);
        action.getParameters().put(COLUMN_ID.getKey(), columnId);
        action.getParameters().put(SCOPE.getKey(), "column");
        return action;
    }
}
