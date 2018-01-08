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

package org.talend.dataprep.api.service.command.transformation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.service.command.common.ChainedCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.io.ReleasableInputStream;
import org.talend.dataprep.transformation.actions.datablending.Lookup;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.netflix.hystrix.HystrixCommand;

/**
 * Suggestion Lookup actions in addition to dataset actions.
 *
 * Take the suggested column actions as input and add the lookup ones.
 */
@Component
@Scope("request")
public class SuggestLookupActions extends ChainedCommand<InputStream, String> {

    @Autowired
    private ActionRegistry actionRegistry;

    /**
     * Constructor.
     *
     * @param input the command to execute to get the input.
     */
    public SuggestLookupActions(HystrixCommand<String> input, String dataSetId) {
        super(input);
        execute(() -> new HttpGet(datasetServiceUrl + "/datasets"));
        on(HttpStatus.OK).then(process(dataSetId));
        // on error, @see getFallBack()
    }

    /**
     * If this command fails, the previous command's response can always be returned.
     *
     * @see HystrixCommand#getFallback()
     */
    @Override
    protected InputStream getFallback() {
        // return the previous command result
        return new ByteArrayInputStream(getInput().getBytes());
    }

    /**
     * @param dataSetId the current dataset id.
     * @return the function that aggregates the SuggestColumnActions with the lookups.
     */
    private BiFunction<HttpRequestBase, HttpResponse, InputStream> process(String dataSetId) {

        return (request, response) -> {

            // read suggested actions from previous command
            ArrayNode suggestedActions;
            try {
                suggestedActions = (ArrayNode) objectMapper.readerFor(new TypeReference<Action>() {
                }).readTree(getInput());

                // list datasets from this command's response
                List<DataSetMetadata> dataSets = objectMapper.readValue(response.getEntity().getContent(),
                        new TypeReference<List<DataSetMetadata>>() {
                });

                // create and add all the possible lookup to the suggested actions
                for (DataSetMetadata dataset : dataSets) {
                    // exclude current dataset from possible lookup sources
                    if (StringUtils.equals(dataSetId, dataset.getId())) {
                        continue;
                    }

                    Lookup lookup = (Lookup) actionRegistry.get(Lookup.LOOKUP_ACTION_NAME);
                    lookup.adapt(dataset);
                    final JsonNode jsonNode = objectMapper.valueToTree(lookup.getActionForm(getLocale()));
                    suggestedActions.add(jsonNode);
                }

                // write the merged actions to the output streams
                return new ReleasableInputStream( //
                        IOUtils.toInputStream(suggestedActions.toString(), UTF_8), //
                        request::releaseConnection);
            } catch (IOException e) {
                throw new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_SUGGESTED_ACTIONS, e);
            }

        };

    }

}
