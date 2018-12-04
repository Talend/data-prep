package org.talend.dataprep.preparation.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.MixedContentMap;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.talend.dataprep.preparation.service.ReorderStepsUtils.COLUMN_IDS;

public class ReorderStepsUtilsTest {

    private ReorderStepsUtils reorderStepsUtils = new ReorderStepsUtils();

    @Test
    public void useAColumnBeforeCreationOrAfterDeletion_coherentStepsList() throws Exception {
        List<AppendStep> steps = new ArrayList<>();

        steps.add(createAppendStep("0002", asList("0003", "0004")));
        steps.add(createAppendStep("0003", emptyList()));

        assertTrue(reorderStepsUtils.isStepOrderInvalid(steps));
    }

    @Test
    public void useAColumnBeforeCreationOrAfterDeletion_incoherentStepsList() throws Exception {
        List<AppendStep> steps = new ArrayList<>();

        steps.add(createAppendStep("0003", emptyList()));
        steps.add(createAppendStep("0002", asList("0003", "0004")));

        assertFalse(reorderStepsUtils.isStepOrderInvalid(steps));
    }

    @Test
    public void useColumnsBeforeCreationOrAfterDeletion_coherentStepsList() throws Exception {
        List<AppendStep> steps = new ArrayList<>();

        steps.add(createAppendStep("0002", asList("0003", "0004")));
        AppendStep step = createAppendStepMultiColumn("[\"0003\",\"0004\"]", emptyList());
        step.getActions().get(0).getParameters().put("scope", "multi_columns");
        steps.add(step);

        assertTrue(reorderStepsUtils.isStepOrderInvalid(steps));
    }

    @Test
    public void useColumnsBeforeCreationOrAfterDeletion_incoherentStepsList() throws Exception {
        List<AppendStep> steps = new ArrayList<>();

        AppendStep step = createAppendStepMultiColumn("[\"0003\",\"0004\"]", emptyList());
        step.getActions().get(0).getParameters().put("scope", "multi_columns");
        steps.add(step);
        steps.add(createAppendStep("0002", asList("0003", "0004")));

        assertFalse(reorderStepsUtils.isStepOrderInvalid(steps));
    }

    @Test
    public void renameCreatedColumns_reorderCorrectlyColumnIdsAndUpdateRefs() throws Exception {
        List<AppendStep> steps = new ArrayList<>();

        AppendStep firstStep = createAppendStep("0002", singletonList("0004"));
        steps.add(firstStep);
        AppendStep secondStep = createAppendStep("0002", singletonList("0003"));
        steps.add(secondStep);
        AppendStep thirdStep = createAppendStep("0003", emptyList());
        steps.add(thirdStep);

        reorderStepsUtils.renameCreatedColumns(steps);

        assertEquals("0003", firstStep.getDiff().getCreatedColumns().iterator().next());
        assertEquals("0004", secondStep.getDiff().getCreatedColumns().iterator().next());
        assertEquals("0004",
                thirdStep.getActions().iterator().next().getParameters().get(ImplicitParameters.COLUMN_ID.getKey()));
    }

    private static AppendStep createAppendStep(String usedColumnId, List<String> createdColumns) {
        AppendStep firstStep = new AppendStep();
        StepDiff firstStepDiff = new StepDiff();
        firstStepDiff.setCreatedColumns(createdColumns);
        firstStep.setDiff(firstStepDiff);
        Action firstStepAction = new Action();
        MixedContentMap parameters = new MixedContentMap();
        parameters.put(ImplicitParameters.COLUMN_ID.getKey(), usedColumnId);
        firstStepAction.setParameters(parameters);
        firstStep.setActions(Collections.singletonList(firstStepAction));
        return firstStep;
    }

    private static AppendStep createAppendStepMultiColumn(String usedColumnIds, List<String> createdColumns) {
        AppendStep firstStep = new AppendStep();
        StepDiff firstStepDiff = new StepDiff();
        firstStepDiff.setCreatedColumns(createdColumns);
        firstStep.setDiff(firstStepDiff);
        Action firstStepAction = new Action();
        MixedContentMap parameters = new MixedContentMap();
        parameters.put(COLUMN_IDS, usedColumnIds);
        firstStepAction.setParameters(parameters);
        firstStep.setActions(Collections.singletonList(firstStepAction));
        return firstStep;
    }
}
