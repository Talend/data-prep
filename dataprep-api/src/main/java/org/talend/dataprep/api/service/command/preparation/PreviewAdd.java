package org.talend.dataprep.api.service.command.preparation;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.api.PreviewAddInput;

@Component
@Scope("request")
public class PreviewAdd extends PreviewAbstract {

    /** The parameters to perform the preview on the Add Action request. */
    private final PreviewAddInput input;

    /**
     * Default constructor.
     *
     * @param client the http client to use.
     * @param input the parameters to perform the request.
     */
    public PreviewAdd(final HttpClient client, final PreviewAddInput input) {
        super(client);
        this.input = input;
    }

    @Override
    protected InputStream run() throws Exception {
        final Map<String, Action> originalActions = new LinkedHashMap<>();
        String dataSetId = input.getDatasetId();

        // get preparation details to initialize actions list
        if (StringUtils.isNotBlank(input.getPreparationId())) {
            final Preparation preparation = getPreparation(input.getPreparationId());
            dataSetId = preparation.getDataSetId();

            // Get steps from first transformation
            final List<String> steps = preparation.getSteps();
            steps.remove(0);

            // extract actions by steps in chronological order, until defined last active step (from input)
            final Iterator<Action> actions = getPreparationActions(preparation, "head").iterator();
            steps.stream().filter(step -> actions.hasNext()).forEach(step -> originalActions.put(step, actions.next()));
        }

        // modify actions to include the update
        final Map<String, Action> modifiedActions = new LinkedHashMap<>(originalActions);
        modifiedActions.put("preview", input.getAction());

        // execute transformation preview with content and the 2 transformations
        setContext(originalActions.values(), modifiedActions.values(), dataSetId, input.getTdpIds());
        return super.run();
    }

}
