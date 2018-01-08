//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.preparation.service;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.Flag;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;

/**
 * Generates the metadata changes of a preparation between 2 steps.
 */
@Component
public class MetadataChangesOnActionsGenerator {

    public StepDiff computeCreatedColumns(final RowMetadata metadata, List<RunnableAction> currentActions,
            List<RunnableAction> newActions) {

        RowMetadata workingMetadata = compileActionsOnMetadata(currentActions, metadata);
        return computeCreatedColumns(newActions, workingMetadata);
    }

    /**
     * Return the StepDiff for the given actions from the row metadata reference.
     *
     * @param newActions the actions to apply to compute the step diff.
     * @param reference the reference row metadata.
     * @return the StepDiff starting from the reference
     */
    StepDiff computeCreatedColumns(List<RunnableAction> newActions, RowMetadata reference) {

        RowMetadata updatedMetadata = compileActionsOnMetadata(newActions, reference);

        updatedMetadata.diff(reference);

        List<String> createdColumnIds = updatedMetadata.getColumns()
                .stream()
                .filter(c -> Flag.NEW.getValue().equals(c.getDiffFlagValue()))
                .map(ColumnMetadata::getId)
                .collect(toList());

        StepDiff stepDiff = new StepDiff();
        stepDiff.setCreatedColumns(createdColumnIds);
        return stepDiff;
    }

    /**
     * Compile the given actions, hence updating the given row metadata and return the later.
     *
     * @param actions the actions to compile.
     * @param startingRowMetadata the row metadata to start from.
     * @return the updated row metadata from the actions compilation.
     */
    private RowMetadata compileActionsOnMetadata(List<RunnableAction> actions, final RowMetadata startingRowMetadata) {

        final RowMetadata updatedRowMetadata = startingRowMetadata.clone();

        TransformationContext transformationContext = new TransformationContext();

        // compile every action within the transformation context
        for (RunnableAction action : actions) {
            final DataSetRowAction rowAction = action.getRowAction();
            final ActionContext actionContext = transformationContext.create(rowAction, updatedRowMetadata);
            rowAction.compile(actionContext);
        }

        // the cleanup is REALLY important (as it can close http connection in case of a lookup for instance)
        transformationContext.cleanup();

        return updatedRowMetadata;

    }

}
