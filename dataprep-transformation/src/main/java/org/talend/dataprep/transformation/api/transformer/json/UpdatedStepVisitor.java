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

package org.talend.dataprep.transformation.api.transformer.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.StepNode;
import org.talend.dataprep.transformation.service.StepMetadataRepository;

/**
 * <p>
 * A {@link Visitor} to get {@link Step steps} out of {@link StepNode step nodes} in transformation
 * {@link org.talend.dataprep.transformation.pipeline.Pipeline}.
 * </p>
 * <p>
 * This visitor takes into account action's status so steps don't get accidentally updated with wrong row metadata.
 * </p>
 */
class UpdatedStepVisitor extends Visitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatedStepVisitor.class);

    private final StepMetadataRepository preparationUpdater;

    public UpdatedStepVisitor(StepMetadataRepository preparationUpdater) {
        this.preparationUpdater = preparationUpdater;
    }

    @Override
    public void visitStepNode(StepNode stepNode) {
        stepNode.getEntryNode().accept(new Visitor() {

            @Override
            public void visitAction(ActionNode actionNode) {
                final ActionContext.ActionStatus status = actionNode.getActionContext().getActionStatus();
                final Step step = stepNode.getStep();
                switch (status) {
                case NOT_EXECUTED:
                case CANCELED:
                    LOGGER.debug("Not updating metadata for {} (action ended with status {}).", step.getId(), status);
                    step.setRowMetadata(null);
                    break;
                case OK:
                case DONE:
                    LOGGER.debug("Keeping metadata {} (action ended with status {}).", step.getId(), status);
                    break;
                }

                preparationUpdater.update(step.id(), stepNode.getRowMetadata());
            }
        });
        super.visitStepNode(stepNode);
    }
}
