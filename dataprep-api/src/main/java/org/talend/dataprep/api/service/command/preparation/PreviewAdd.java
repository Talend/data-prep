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
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.api.PreviewAddParameters;

/**
 * Command used to retrieve a preview when adding an action to a dataset/preparation.
 */
@Component
@Scope("request")
public class PreviewAdd extends PreviewAbstract {

    /** The parameters to perform the preview on the Add Action request. */
    private final PreviewAddParameters addParameters;

    /**
     * Default constructor.
     *
     * @param parameters the parameters to perform the request.
     * @param preparation the preparation to deal with (may be null if dealing with dataset).
     * @param actions the preparation actions (may be empty if dealing with dataset).
     */
    private PreviewAdd(final PreviewAddParameters parameters, Preparation preparation, List<Action> actions) {
        super(preparation, actions);
        this.addParameters = parameters;
    }

    /**
     * @see PreviewAbstract#run()
     */
    @Override
    protected InputStream run() throws Exception {
        final Map<String, Action> originalActionsByStep = new LinkedHashMap<>();
        String dataSetId = addParameters.getDatasetId();

        // get preparation details to initialize actions list
        if (StringUtils.isNotBlank(addParameters.getPreparationId())) {
            dataSetId = preparation.getDataSetId();

            // Get steps from first transformation
            final List<String> steps = preparation.getSteps().stream().map(Step::getId).collect(Collectors.toList());
            steps.remove(0);

            // extract actions by steps in chronological order, until defined last active step (from input)
            final Iterator<Action> iterator = actions.iterator();
            steps.stream().filter(step -> iterator.hasNext()).forEach(step -> originalActionsByStep.put(step, iterator.next()));
        }
        final Collection<Action> originalActions = originalActionsByStep.values();

        // modify actions to include the update
        final List<Action> modifiedActions = new ArrayList<>(originalActions);
        modifiedActions.addAll(addParameters.getActions());

        // execute transformation preview with content and the 2 transformations
        setContext(originalActions, modifiedActions, dataSetId, addParameters.getPreparationId(), addParameters.getTdpIds(), addParameters.getSourceType());
        return super.run();
    }

}
