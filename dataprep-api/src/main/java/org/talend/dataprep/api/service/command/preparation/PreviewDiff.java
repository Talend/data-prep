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

package org.talend.dataprep.api.service.command.preparation;

import java.io.InputStream;
import java.util.*;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.api.PreviewDiffParameters;

/**
 * Command used to retrieve preview from a diff.
 */
@Component
@Scope("request")
public class PreviewDiff extends PreviewAbstract {

    /** The diff parameters. */
    private final PreviewDiffParameters input;
    /** Preparation actions up to the last active step. */
    private final List<Action> lastActiveStepActions;
    /** Preparation actions up to the preview step. */
    private final List<Action> previewStepActions;

    /**
     * Default constructor.
     * @param input the parameters.
     * @param preparation the preparation to deal with.
     * @param lastActiveStepActions preparation actions up to the last active step.
     * @param previewStepActions preparation actions up to the preview step.
     */
    // private constructor used to ensure the IoC
    private PreviewDiff(final PreviewDiffParameters input, Preparation preparation, List<Action> lastActiveStepActions, List<Action> previewStepActions) {
        super(preparation, new ArrayList<>(0));
        this.input = input;
        this.lastActiveStepActions = lastActiveStepActions;
        this.previewStepActions = previewStepActions;
    }

    /**
     * @see PreviewAbstract#run()
     */
    @Override
    protected InputStream run() throws Exception {

        // get preparation details
        final String dataSetId = preparation.getDataSetId();

        // get steps from first operation to head
        final List<Step> steps = preparation.getSteps();
        steps.remove(0);

        // extract actions by steps in chronological order, until defined last active step (from input)
        final Map<String, Action> originalActions = new LinkedHashMap<>();
        final Iterator<Action> actions = lastActiveStepActions.iterator();
        steps.stream().filter(step -> actions.hasNext()).forEach(step -> originalActions.put(step.id(), actions.next()));

        // extract actions by steps in chronological order, until preview step (from input)
        final Map<String, Action> previewActions = new LinkedHashMap<>();
        final Iterator<Action> previewActionsIterator = previewStepActions.iterator();
        steps.stream().filter(step -> previewActionsIterator.hasNext()).forEach(step -> previewActions.put(step.id(), previewActionsIterator.next()));

        // execute transformation preview with content and the 2 transformations
        setContext(originalActions.values(), previewActions.values(), dataSetId, input.getPreparationId(), input.getTdpIds(), input.getSourceType());
        return super.run();
    }


}
