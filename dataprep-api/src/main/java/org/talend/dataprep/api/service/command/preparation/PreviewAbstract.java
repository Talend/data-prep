package org.talend.dataprep.api.service.command.preparation;

import static org.talend.dataprep.api.service.command.common.Defaults.pipeStream;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.http.HttpStatus;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.PreparationCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.transformation.preview.api.PreviewParameters;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Base class for preview commands.
 */
public abstract class PreviewAbstract extends PreparationCommand<InputStream> {

    /** The preview parameters. */
    private PreviewParameters parameters;

    /**
     * Default constructor.
     * 
     * @param client the http client to use.
     */
    public PreviewAbstract(final HttpClient client) {
        super(APIService.PREPARATION_GROUP, client);
    }

    @Override
    protected InputStream run() throws Exception {
        if (parameters == null) {
            throw new IllegalStateException("Missing preview context.");
        }
        execute(this::onExecute);
        on(HttpStatus.OK).then(pipeStream());
        return super.run();
    }

    private HttpRequestBase onExecute() {
        final String uri = this.transformationServiceUrl + "/transform/preview";
        HttpPost transformationCall = new HttpPost(uri);

        final String paramsAsJson;
        try {
            paramsAsJson = builder.build().writer().writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PERFORM_PREVIEW, e);
        }
        HttpEntity reqEntity = new StringEntity(paramsAsJson, ContentType.APPLICATION_JSON);
        transformationCall.setEntity(reqEntity);
        return transformationCall;
    }

    /**
     * Set the preview context.
     *
     * @param baseActions the list of actions to use as the starting state for the preview.
     * @param newActions the list of action to add to the starting ones.
     * @param datasetId the datasret id.
     * @param tdpIds the list of rows to apply.
     * @throws JsonProcessingException if an error occurs.
     */
    protected void setContext(Collection<Action> baseActions, Collection<Action> newActions, String datasetId,
            List<Integer> tdpIds) throws JsonProcessingException {
        this.parameters = new PreviewParameters( //
                serializeActions(baseActions), //
                serializeActions(newActions), //
                datasetId, //
                serializeIds(tdpIds));
    }
}
