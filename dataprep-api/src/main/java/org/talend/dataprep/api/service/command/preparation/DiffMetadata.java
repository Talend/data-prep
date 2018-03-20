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

package org.talend.dataprep.api.service.command.preparation;

import static java.util.stream.Collectors.toList;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.talend.daikon.exception.ExceptionContext.withBuilder;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;
import static org.talend.dataprep.command.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.service.command.common.ChainedCommand;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.command.preparation.PreparationGetActions;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.preview.api.PreviewParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Return the differences of metadata for some actions to add within a preparation.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class DiffMetadata extends ChainedCommand<InputStream, InputStream> {

    /**
     * Default constructor.
     *
     * @param dataSetId     The dataSetId id.
     * @param preparationId The preparation id.
     * @param actionsToAdd  The actions to add.
     * @param input         the command to execute to get the actions of the preparation.
     */
    public DiffMetadata(String dataSetId, String preparationId, List<Action> actionsToAdd, PreparationGetActions input) {
        super(GenericCommand.PREPARATION_GROUP, input);
        execute(() -> onExecute(dataSetId, preparationId, actionsToAdd));
        on(HttpStatus.OK).then(pipeStream());
    }


    private HttpRequestBase onExecute(final String dataSetId, final String preparationId, final List<Action> actionsToAdd) {
        // original actions (currently applied on the preparation)
        final List<Action> originalActions;
        try {
            originalActions = objectMapper
                    .readerFor(new TypeReference<List<Action>>() {
                    })
                    .readValue(getInput());
        } catch (final IOException e) {
            throw new TDPException(UNABLE_TO_READ_PREPARATION, e, withBuilder().put("id", preparationId).build());
        }

        // prepare the preview parameters out of the preparation actions
        final List<PreviewParameters> previewParameters = IntStream.range(0, actionsToAdd.size())
                .mapToObj((index) -> {
                    try {
                        // base actions = original actions + actions to add from 0 to index
                        final List<Action> previousActionsToAdd = actionsToAdd.subList(0, index);
                        final List<Action> baseActions = new ArrayList<>(originalActions);
                        baseActions.addAll(previousActionsToAdd);

                        // diff actions actions = base actions + the action to add for diff
                        final Action singleActionToAdd = actionsToAdd.get(index);
                        final List<Action> diffActions = new ArrayList<>(baseActions);
                        diffActions.add(singleActionToAdd);

                        return new PreviewParameters( //
                                serializeActions(baseActions), //
                                serializeActions(diffActions), //
                                dataSetId, //
                                null, //
                                null, //
                                HEAD);
                    } catch (IOException e) {
                        throw new TDPException(UNABLE_TO_READ_PREPARATION, e, withBuilder().put("id", preparationId).build());
                    }
                })
                .collect(toList());

        // create the http action to perform
        try {
            final String uri = transformationServiceUrl + "/transform/diff/metadata";
            final HttpPost transformationCall = new HttpPost(uri);
            transformationCall.setEntity(new StringEntity(objectMapper.writer().writeValueAsString(previewParameters), APPLICATION_JSON));
            return transformationCall;
        } catch (JsonProcessingException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }

    }
}
