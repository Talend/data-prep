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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.api.PreviewUpdateParameters;

/**
 * Command used to retrieve a preview when updating a command.
 */
@Component
@Scope("request")
public class PreviewUpdate extends PreviewAbstract {

    /** The all the preview parameters. */
    private final PreviewUpdateParameters parameters;

    /**
     * Default constructor.
     * @param parameters
     * @param preparation
     * @param actions
     */
    // private constructor used to ensure the IoC
    private PreviewUpdate(final PreviewUpdateParameters parameters, Preparation preparation, List<Action> actions) {
        super(preparation, actions);
        this.parameters = parameters;
    }

    /**
     * @see PreviewAbstract#run()
     */
    @Override
    protected InputStream run() throws Exception {

        final String dataSetId = preparation.getDataSetId();

        //Get steps from first transformation
        final List<String> steps = preparation.getSteps().stream().map(Step::getId).collect(Collectors.toList());
        steps.remove(0);

        // extract actions by steps in chronological order, until defined last active step (from input)
        Map<String, Action> originalActions = new LinkedHashMap<>();
        final Iterator<Action> iterator = actions.iterator();
        steps.stream().filter(step -> iterator.hasNext()).forEach(step -> originalActions.put(step, iterator.next()));

        // modify actions to include the update
        final Map<String, Action> modifiedActions = new LinkedHashMap<>(originalActions);
        if (modifiedActions.get(parameters.getUpdateStepId()) != null) {
            modifiedActions.put(parameters.getUpdateStepId(), parameters.getAction());
        }

        // execute transformation preview with content and the 2 transformations
        setContext(originalActions.values(), modifiedActions.values(), dataSetId, parameters.getPreparationId(), parameters.getTdpIds(), parameters.getSourceType());
        return super.run();
    }

}
