// ============================================================================
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

package org.talend.dataprep.actions;

import static java.util.function.Function.identity;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.RowMetadataUtils;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.actions.datablending.Lookup;
import org.talend.dataprep.transformation.actions.datablending.LookupDatasetsManager;
import org.talend.dataprep.transformation.actions.date.Providers;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.pipeline.Pipeline;
import org.talend.dataprep.transformation.pipeline.node.BasicNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultActionParser implements ActionParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultActionParser.class);

    private final String apiUrl;

    private final String login;

    private final String password;

    private static final ActionRegistry actionRegistry = new ClassPathActionRegistry(
            "org.talend.dataprep.transformation.actions");

    private static final ActionFactory actionFactory = new ActionFactory();

    private static final ObjectMapper mapper = new ObjectMapper();

    public DefaultActionParser(String apiUrl, String login, String password) {
        this.apiUrl = apiUrl;
        this.login = login;
        this.password = password;
    }

    /**
     * TODO the class representing the DQ library
     *
     * @return
     */
    private  Object retrieveDQDictionnary() {
        return null;
    }

    private static void assertPreparation(Object actions) {
        if (actions == null) {
            throw new IllegalArgumentException("Actions can not be null.");
        }
    }

    /**
     * Returns a list of actions from its json representation.
     *
     * @param actionsAsString the json representation of the list of action
     * @return a list of actions from its json representation
     */
    private List<Action> getActions(String actionsAsString) {
        try {
            final JsonNode jsonNode = mapper.readTree(actionsAsString);
            if (!jsonNode.isArray()) {
                throw new IllegalArgumentException("Expected array at stream start");
            }

            List<Action> parsedActions = new ArrayList<>();
            jsonNode.elements().forEachRemaining(n -> {
                final Action parsedAction = parseAction(n);
                if (parsedAction != null) {
                    parsedActions.add(parsedAction);
                }
            });
            return parsedActions;
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid action list", e);
        }
    }

    /**
     * Parses an action corresponding to a JSON node and returns the Action instance corresponding to the specified json node.
     *
     * @param n a json node corresponding to an action
     * @return returns the Action instance corresponding to the specified json node
     */
    private Action parseAction(JsonNode n) {
        String actionName = n.get("action").asText();
        LOGGER.info("New action: {}", actionName);
        final ActionDefinition actionMetadata = actionRegistry.get(actionName);

        if (actionMetadata == null) {
            LOGGER.error("No action implementation found for '{}'.", actionName);
        } else {
            LOGGER.info("Action metadata found for '{}': {}", actionName, actionMetadata.getClass().getName());
            final Iterator<Map.Entry<String, JsonNode>> parameters = n.get("parameters").fields();
            Map<String, String> parametersAsMap = new HashMap<>();
            while (parameters.hasNext()) {
                final Map.Entry<String, JsonNode> next = parameters.next();
                final JsonNode value = next.getValue();
                parametersAsMap.put(next.getKey(), parseParameter(value));
            }
            // Create action
            final Action action = actionFactory.create(actionMetadata, parametersAsMap);
            LOGGER.info("Wrap action execution for '{}' with parameters '{}'.", actionMetadata.getClass().getName(),
                    parametersAsMap);
            ActionContext context = new ActionContext(new TransformationContext());
            context.setParameters(parametersAsMap);

            LOGGER.info("New parsed action: {}", actionName);
            return action;
        }
        return null;
    }

    /**
     * Returns the string version of a parameter field from a json node.
     *
     * @param value the json node to be parsed
     * @return the string version of a parameter field from a json node
     */
    private String parseParameter(final JsonNode value) {
        final String result;
        if (value.isTextual()) {
            result = value.asText();
        } else if (value.isArray()) {
            try {
                result = mapper.writeValueAsString(value);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to return the string version of the action parameter: " + "value", e);
            }
        } else if (value.isObject()) {
            result = value.toString();
        } else if (value.isNull()) {
            result = StringUtils.EMPTY;
        } else {
            LOGGER.warn("Unknown JSON node type in parameters '{}', falls back to asText().", value);
            result = value.asText();
        }
        return result;
    }

    private HashMap<String, DataSetRow> getLookupDataset(RemoteResourceGetter clientFormLogin, String dataSetId,
            String joinOnColumn) {
        LOGGER.debug("Retrieving lookup dataset '{}'", dataSetId);
        return (HashMap<String, DataSetRow>) clientFormLogin.retrieveLookupDataSet(apiUrl, login, password, dataSetId, joinOnColumn);
    }

    private String getPreparation(RemoteResourceGetter clientFormLogin, String preparationId) {
        LOGGER.debug("Retrieving preparation '{}'", preparationId);
                return clientFormLogin.retrievePreparation(apiUrl, login, password, preparationId);
    }

    private HashMap<String, DataSetRow> retrieveLookupDataSetFromAction(RemoteResourceGetter clientFormLogin, Action action) {
        final HashMap<String, DataSetRow> result;

        if (StringUtils.equals(action.getName(), Lookup.LOOKUP_ACTION_NAME)) {
            final String dataSetId = action.getParameters().get(Lookup.Parameters.LOOKUP_DS_ID.getKey());
            if (StringUtils.isEmpty(dataSetId)) {
                throw new IllegalArgumentException("A lookup action must have a lookup dataset id: " + action);
            } else {
                final String joinOn = action.getParameters().get(Lookup.Parameters.LOOKUP_JOIN_ON.getKey());

                result = getLookupDataset(clientFormLogin, dataSetId, joinOn);
            }
        } else {
            throw new IllegalArgumentException(
                    "Trying to retrieve a lookup dataset from the following action: " + action.getName());
        }
        return result;
    }

    private HashMap<String, HashMap<String, DataSetRow>> retrieveLookupDataSets(List<Action> actions) {
        final HashMap<String, HashMap<String, DataSetRow>> result = new HashMap<>();
        final RemoteResourceGetter clientFormLogin = new RemoteResourceGetter();
        actions.stream().filter(action -> StringUtils.equals(action.getName(), Lookup.LOOKUP_ACTION_NAME))
                .forEach(action -> result.put(action.getParameters().get(Lookup.Parameters.LOOKUP_DS_ID.getKey()),
                        retrieveLookupDataSetFromAction(clientFormLogin, action)));
        return result;
    }

    private Function<IndexedRecord, IndexedRecord> internalParse(InputStream preparation) {
        assertPreparation(preparation);

        // Parse preparation JSON
        final JsonNode preparationNode;
        try {
            preparationNode = mapper.readTree(preparation);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to parse preparation", e);
        }
        Optional<JsonNode> preparationName = Optional.ofNullable(preparationNode.get("name"));
        LOGGER.info("Parsing actions from preparation '{}'",
                preparationName.isPresent() ? preparationName.get().asText() : "N/A");

        // Get action JSON node
        final JsonNode actionNode = preparationNode.get("actions");
        if (actionNode == null) {
            LOGGER.info("No action defined in preparation, returning identity function");
            return identity();
        }

        // Get row metadata JSON node
        final JsonNode rowMetadataNode = preparationNode.get("rowMetadata");
        final RowMetadata rowMetadata;
        if (rowMetadataNode == null) {
            throw new IllegalArgumentException("No metadata present in preparation.");
        } else {
            try {
                rowMetadata = mapper.reader(RowMetadata.class).readValue(rowMetadataNode);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to parse row metadata from preparation.", e);
            }
        }

        // Get the list of actions
        List<Action> actions = getActions(actionNode.toString());

        // get the list of lookup data sets
        HashMap<String, HashMap<String, DataSetRow>> lookupDataSets = retrieveLookupDataSets(actions);

        // get the DQ dictionary

        // Build internal transformation pipeline
        final StackedNode stackedNode = new StackedNode();
        final Pipeline pipeline = Pipeline.Builder.builder() //
                .withActionRegistry(actionRegistry) //
                .withActions(actions) //
                .withInitialMetadata(rowMetadata, true) //
                .withOutput(() -> stackedNode) //
                .withStatisticsAdapter(new StatisticsAdapter(40)) //
                .withGlobalStatistics(false) //
                .allowMetadataChange(false) //
                .withAnalyzerService(Providers.get(AnalyzerService.class)) //
                .build();
        return new SerializableFunction(pipeline, stackedNode, rowMetadata, lookupDataSets);
    }

    public Function<IndexedRecord, IndexedRecord> parse(InputStream preparation) {
        return internalParse(preparation);
    }

    @Override
    public Function<IndexedRecord, IndexedRecord> parse(String preparationId) {
        final RemoteResourceGetter clientFormLogin = new RemoteResourceGetter();
        String preparation = getPreparation(clientFormLogin, preparationId);
        return internalParse(IOUtils.toInputStream(preparation));
    }

    private static class StackedNode extends BasicNode {

        private transient Deque<DataSetRow> stack;

        @Override
        public void receive(DataSetRow row, RowMetadata metadata) {
            if (!row.isDeleted()) {
                getStack().push(row);
            }
            super.receive(row, metadata);
        }

        private Deque<DataSetRow> getStack() {
            if (stack == null) {
                stack = new ArrayDeque<>();
            }
            return stack;
        }

        DataSetRow pop() {
            final Deque<DataSetRow> dataSetRows = getStack();
            return dataSetRows.isEmpty() ? null : dataSetRows.pop();
        }
    }

    private static class SerializableFunction implements Function<IndexedRecord, IndexedRecord>, Serializable {

        private static final Logger LOG = LoggerFactory.getLogger(SerializableFunction.class);

        private final Pipeline pipeline;

        private final StackedNode stackedNode;

        private final RowMetadata initialRowMetadata;

        private final HashMap<String, HashMap<String, DataSetRow>> lookupDataSets;

        private transient boolean loaded = false;

        private SerializableFunction(Pipeline pipeline, StackedNode stackedNode, RowMetadata initialRowMetadata,
                HashMap<String, HashMap<String, DataSetRow>> lookupDatasets) {
            this.pipeline = pipeline;
            this.stackedNode = stackedNode;
            this.initialRowMetadata = initialRowMetadata;
            this.lookupDataSets = lookupDatasets;
        }

        @Override
        public IndexedRecord apply(IndexedRecord indexedRecord) {
            if (!loaded) {
                LOG.debug("Adding cached data sets to LookupDataSetManager");
                lookupDataSets.entrySet().stream().forEach(entry -> {
                    if (LookupDatasetsManager.put(entry.getKey(), entry.getValue()))
                        LOG.debug("Added {} to the lookup data sets", entry.getKey());
                });
                loaded = true;
            }
            Map<String, String> values = new HashMap<>();
            final List<Schema.Field> fields = indexedRecord.getSchema().getFields();
            DecimalFormat decimalFormat = new DecimalFormat("0000");
            int i = 0;
            for (Schema.Field field : fields) {
                values.put(decimalFormat.format(i++), String.valueOf(indexedRecord.get(field.pos())));
            }
            final DataSetRow row = new DataSetRow(values);
            pipeline.receive(row, initialRowMetadata);

            // Reapply values of data set row to the indexed record
            final Optional<DataSetRow> result = Optional.ofNullable(stackedNode.pop());
            if (result.isPresent()) {
                final DataSetRow modifiedRow = result.get();
                final RowMetadata modifiedRowRowMetadata = modifiedRow.getRowMetadata();
                final Schema outputSchema = RowMetadataUtils.toSchema(modifiedRowRowMetadata);
                GenericRecord modifiedRecord = new GenericData.Record(outputSchema);
                final Iterator<Object> iterator = modifiedRow.order().values().values().iterator();
                for (int j = 0; j < outputSchema.getFields().size() && iterator.hasNext(); j++) {
                    modifiedRecord.put(j, iterator.next());
                }
                return modifiedRecord;
            } else {
                return null;
            }
        }
    }
}
