//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.command.common;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.preparation.StepDiff;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.PreparationErrorCodes;
import org.talend.dataprep.transformation.preview.api.PreviewParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommandGroupKey;

public abstract class PreparationCommand<T> extends GenericCommand<T> {

    @Autowired
    protected Jackson2ObjectMapperBuilder builder;

    /** The root step. */
    @Resource(name = "rootStep")
    private Step rootStep;


    protected PreparationCommand(final HystrixCommandGroupKey groupKey, final HttpClient client) {
        super(groupKey, client);
    }

    /**
     * Get the diff metadata (containing the created columns ids) by adding the 'actionsToAdd' to the preparation (preparationId) at the 'insertionStepId'
     * @param preparationId     The preparation id
     * @param insertionStepId   The step id where we want to add the actions
     * @param actionsToAdd      The actions to add
     */
    protected StepDiff getDiffMetadata(final String preparationId, final String insertionStepId, final List<Action> actionsToAdd) throws IOException {

        // get preparation details
        final Preparation preparation = getPreparation(preparationId);
        final String dataSetId = preparation.getDataSetId();

        // extract insertion point actions
        final List<Action> actions = getPreparationActions(preparation, insertionStepId);

        final List<Action> diffActions = new ArrayList<>(actions);
        diffActions.addAll(actionsToAdd);

        final PreviewParameters previewParameters = new PreviewParameters( //
                serializeActions(actions), //
                serializeActions(diffActions), //
                dataSetId, //
                null);

        final String uri = transformationServiceUrl + "/transform/diff/metadata";
        final HttpPost transformationCall = new HttpPost(uri);

        final ObjectMapper mapper = builder.build();
        try {
            transformationCall
                    .setEntity(new StringEntity(mapper.writer().writeValueAsString(previewParameters), APPLICATION_JSON));
            final InputStream diffInputStream = client.execute(transformationCall).getEntity().getContent();
            return mapper.readValue(diffInputStream, StepDiff.class);
        }
        finally {
            transformationCall.releaseConnection();
        }
    }

    /**
     * Call Preparation Service to get preparation.
     *
     * @param preparationId - the preparation id
     * @return the resulting Json node object
     * @throws java.io.IOException if an error occurs.
     */
    protected Preparation getPreparation(final String preparationId) throws IOException {
        if (StringUtils.isEmpty(preparationId)) {
            throw new IllegalArgumentException("Preparation id cannot be empty.");
        }
        final HttpGet preparationRetrieval = new HttpGet(preparationServiceUrl + "/preparations/" + preparationId);
        try {
            // execute the request & check the response
            final org.apache.http.HttpResponse response = client.execute(preparationRetrieval);
            final org.springframework.http.HttpStatus status = org.springframework.http.HttpStatus
                    .valueOf(response.getStatusLine().getStatusCode());
            if (status.is4xxClientError() || status.is5xxServerError()) {
                throw new TDPException(PreparationErrorCodes.UNABLE_TO_READ_PREPARATION,
                        ExceptionContext.build().put("id", preparationId).put("version", "head"));
            }
            // parsre the preparation
            InputStream content = response.getEntity().getContent();
            return builder.build().readerFor(Preparation.class).readValue(content);
        } finally {
            preparationRetrieval.releaseConnection();
        }
    }

    /**
     * Return the dataset metadata from its id.
     *
     * @param datasetId The dataset id.
     * @return the dataset metadata.
     * @throws IOException S**t happens...
     */
    protected DataSetMetadata getDatasetMetadata(final String datasetId) throws IOException {
        final HttpGet datasetRetrieval = new HttpGet(datasetServiceUrl + "/datasets/" + datasetId + "/metadata");
        try {
            InputStream content = client.execute(datasetRetrieval).getEntity().getContent();
            final DataSet dataset = builder.build().readerFor(DataSet.class).readValue(content);
            return dataset.getMetadata();
        } finally {
            datasetRetrieval.releaseConnection();
        }
    }

    /**
     * Serialize the actions to string.
     *
     * @param stepActions - map of couple (stepId, action)
     * @return the serialized actions
     */
    protected String serializeActions(final Collection<Action> stepActions) throws JsonProcessingException {
        return "{\"actions\": " + builder.build().writeValueAsString(stepActions) + "}";
    }

    /**
     * Serialize the list of integer to json string.
     * 
     * @param listToEncode - list of integer to encode
     * @return the serialized and encoded list
     */
    protected String serializeIds(final List<Integer> listToEncode) throws JsonProcessingException {
        return builder.build().writeValueAsString(listToEncode);
    }

    protected List<Action> getPreparationActions(Preparation preparation, String stepId) throws IOException {
        if (StringUtils.isEmpty(stepId) || rootStep.id().equals(stepId)) {
            // No need for a connection to retrieve an empty list.
            return Collections.emptyList();
        }
        final HttpGet actionsRetrieval = new HttpGet(preparationServiceUrl + "/preparations/" + preparation.id()
                + "/actions/" + stepId);
        try {
            InputStream content = client.execute(actionsRetrieval).getEntity().getContent();
            return builder.build() //
                    .readerFor(new TypeReference<List<Action>>() { //
                    }) //
                    .readValue(content);
        } finally {
            actionsRetrieval.releaseConnection();
        }
    }

}
