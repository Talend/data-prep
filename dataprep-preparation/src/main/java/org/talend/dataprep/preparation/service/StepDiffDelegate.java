package org.talend.dataprep.preparation.service;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.Flag;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;

// TODO: find a good name
@Component
public class StepDiffDelegate {

    public StepDiff getActionCreatedColumns(RowMetadata metadata, List<RunnableAction> currentActions, List<RunnableAction> newActions) {
        RowMetadata workingMetadata = metadata.clone();
        compileActionOnMetadata(new TransformationContext(), workingMetadata, currentActions);
        return getActionCreatedColumns(workingMetadata, newActions);
    }

    public StepDiff getActionCreatedColumns(RowMetadata metadataRef, List<RunnableAction> actions) {
        RowMetadata workingMetadata = metadataRef.clone();
        TransformationContext transformationContext = new TransformationContext();
        compileActionOnMetadata(transformationContext, workingMetadata, actions);

        workingMetadata.diff(metadataRef);

        List<String> createdColumnIds = workingMetadata.getColumns()
                .stream()
                .filter(c -> Flag.NEW.getValue().equals(c.getDiffFlagValue()))
                .map(ColumnMetadata::getId)
                .collect(toList());

        StepDiff stepDiff = new StepDiff();
        stepDiff.setCreatedColumns(createdColumnIds);
        return stepDiff;
    }

    private void compileActionOnMetadata(TransformationContext transformationContext, RowMetadata workingMetadata, List<RunnableAction> actions) {
        ActionContext contextWithMetadata = new ActionContext(transformationContext, workingMetadata);
        for (RunnableAction action : actions) {
            action.getRowAction().compile(contextWithMetadata);
        }
    }

}
