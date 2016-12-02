// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.command.dataset.DataSetGetMetadata;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.security.SecurityProxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Command used to add information from the dataset into the preparation details.
 */
@Component
@Scope("prototype")
public class EnrichedPreparationDetails extends GenericCommand<InputStream> {

    private static final String JOB_TYPE = "job";
    private final String preparationId;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SecurityProxy securityProxy;

    /**
     * Default constructor.
     *
     * @param preparationId the preparation id.
     */
    // private constructor to ensure the use of IoC
    private EnrichedPreparationDetails(final String preparationId) { // NOSONAR used by IoC
        super(PREPARATION_GROUP);
        this.preparationId = preparationId;
    }

    /**
     * Wraps the call to get the dataset as the technical user as the dataset may not be shared for instance.
     *
     * @see GenericCommand#run()
     */
    @Override
    protected InputStream run() throws Exception {
        final ObjectNode preparationJsonRootNode = getPreparationDetails();

        // convert the steps object array to a string array
        objectStepToId((ArrayNode) preparationJsonRootNode.get("steps"));

        final String dataSetId = preparationJsonRootNode.get("dataSetId").asText();
        final DataSetMetadata dataset = getDatasetMetadata(dataSetId);

        this.enrichPreparation(preparationJsonRootNode, dataset);

        return IOUtils.toInputStream(preparationJsonRootNode.toString());
    }

    /**
     * Replace the given "steps" array node by an array of steps id (object[] -> string[]).
     *
     * @param stepsNode the node that contains steps object to update.
     */
    private void objectStepToId(ArrayNode stepsNode) {
        List<String> stepsId = new ArrayList<>();
        for (JsonNode step : stepsNode) {
            stepsId.add(step.get("id").textValue());
        }
        stepsNode.removeAll();
        stepsId.forEach(stepsNode::add);
    }

    /**
     * Get preparation details from the right command
     */
    private ObjectNode getPreparationDetails() throws IOException {
        final PreparationDetailsGet preparationDetails = context.getBean(PreparationDetailsGet.class, this.preparationId);
        return (ObjectNode) objectMapper.readTree(preparationDetails.execute());
    }

    /**
     * Get dataset metadata from the right command
     */
    private DataSetMetadata getDatasetMetadata(final String dataSetId) {
        try {
            securityProxy.asTechnicalUser();
            return context.getBean(DataSetGetMetadata.class, dataSetId).execute();
        } finally {
            securityProxy.releaseIdentity();
        }
    }

    /**
     * Enrich preparation details
     */
    private void enrichPreparation(final ObjectNode preparationJsonRootNode, final DataSetMetadata dataset) {
        // update the full run flag
        boolean allowFullRun = JOB_TYPE.equals(dataset.getLocation().getLocationType()) || dataset.getContent().getLimit().isPresent();
        preparationJsonRootNode.put("allowFullRun", allowFullRun);
    }
}
