// ============================================================================
//
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

package org.talend.dataprep.api.service.command.preparation;

import static org.talend.dataprep.command.Defaults.pipeStream;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.http.HttpStatus;
import org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.transformation.preview.api.PreviewParameters;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Base class for preview commands.
 */
public abstract class PreviewAbstract extends GenericCommand<InputStream> {

    /** The preview parameters. */
    private PreviewParameters parameters;

    /** The preparation to deal with (may be null if dealing with dataset). */
    protected final Preparation preparation;

    /** The preparation actions to deal with (may be null if dealing with dataset). */
    protected final List<Action> actions;

    /**
     * Default constructor.
     */
    // private constructor to ensure the IoC
    protected PreviewAbstract(Preparation preparation, List<Action> actions) {
        super(GenericCommand.PREPARATION_GROUP);
        this.preparation = preparation;
        this.actions = actions;
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
            paramsAsJson = objectMapper.writer().writeValueAsString(parameters);
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
     * @param datasetId the dataset id.
     * @param preparationId the preparation id.
     * @param tdpIds the list of rows to apply.
     * @param sourceType the source type.
     * @throws JsonProcessingException if an error occurs.
     */
    protected void setContext(Collection<Action> baseActions,
                              Collection<Action> newActions,
                              String datasetId,
                              String preparationId,
                              List<Integer> tdpIds,
                              ExportParameters.SourceType sourceType) throws JsonProcessingException {

        this.parameters = new PreviewParameters( //
                serializeActions(baseActions), //
                serializeActions(newActions), //
                datasetId, //
                preparationId,
                serializeIds(tdpIds),
                sourceType);
    }

    /**
     * Serialize the list of integer to json string.
     *
     * @param listToEncode - list of integer to encode
     * @return the serialized and encoded list
     */
    private String serializeIds(final List<Integer> listToEncode) throws JsonProcessingException {
        return objectMapper.writeValueAsString(listToEncode);
    }

}
