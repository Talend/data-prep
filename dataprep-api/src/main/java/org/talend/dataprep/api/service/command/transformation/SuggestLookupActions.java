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

package org.talend.dataprep.api.service.command.transformation;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.action.ActionForm;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.service.command.common.ChainedCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.transformation.actions.datablending.Lookup;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.netflix.hystrix.HystrixCommand;

/**
 * Suggestion Lookup actions in addition to dataset actions.
 *
 * Take the suggested column actions as input and add the lookup ones.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class SuggestLookupActions extends ChainedCommand<List<ActionForm>, DataSetMetadata> {

    @Autowired
    private ActionRegistry actionRegistry;

    /**
     * Constructor.
     *
     * @param dataSetMetadata the command to execute to get the DataSetMetadata.
     */
    public SuggestLookupActions(HystrixCommand<DataSetMetadata> dataSetMetadata) {
        super(dataSetMetadata);
        execute(() -> new HttpGet(datasetServiceUrl + "/api/v1/datasets"));
        on(HttpStatus.OK).then(process());
        // on error, @see getFallBack()
    }

    @Override
    protected List<ActionForm> getFallback() {
        return Collections.emptyList();
    }

    /**
     * @return the function that aggregates the SuggestColumnActions with the lookups.
     */
    private BiFunction<HttpRequestBase, HttpResponse, List<ActionForm>> process() {
        return (request, response) -> {

            try {
                List<DataSetMetadata> dataSets = objectMapper.readValue(response.getEntity().getContent(),
                        new TypeReference<List<DataSetMetadata>>() {
                        });

                return dataSets.stream().map(dataset -> {
                    Lookup lookup = (Lookup) actionRegistry.get(Lookup.LOOKUP_ACTION_NAME);
                    return lookup.adapt(dataset).getActionForm(getLocale());
                }).collect(Collectors.toList());
            } catch (IOException e) {
                throw new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_SUGGESTED_ACTIONS, e);
            } finally {
                request.releaseConnection();
            }

        };

    }

}
