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

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
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

public class OldDefaultActionParser  {

    private static final Logger LOGGER = LoggerFactory.getLogger(OldDefaultActionParser.class);

    private static final String API_URL = "http://127.0.0.1:9999";

    private static final ActionRegistry actionRegistry = new ClassPathActionRegistry(
            "org.talend.dataprep.transformation.actions");

    private static final ActionFactory actionFactory = new ActionFactory();

    private static final ObjectMapper mapper = new ObjectMapper();

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
    private static List<Action> getActions(String actionsAsString) {
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
    private static Action parseAction(JsonNode n) {
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
    private static String parseParameter(final JsonNode value) {
        final String result;
        if (value.isTextual()) {
            result = value.asText();
        } else if (value.isArray()) {
            ObjectMapper mapper = new ObjectMapper();

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

    private static HashMap<String, DataSetRow> getLookupDataset(String dataSetId, String joinOnColumn) {
        LOGGER.info("Trying to retrieve the lookup dataset Bouba: ");

            RemoteResourceGetter clientFormLogin = new RemoteResourceGetter();
        Header jwt = clientFormLogin.login(API_URL+"/login?client-app=STUDIO", "boubacar@dataprep.com", "boubacar");

        return (HashMap<String, DataSetRow>) clientFormLogin.retrieveLookupDataSet(API_URL, "boubacar@dataprep.com", "boubacar", dataSetId, joinOnColumn);
    }

    private static HashMap<String, DataSetRow> retrieveLookupDataSetFromAction(Action action) {
        final HashMap<String, DataSetRow> result;

        if (StringUtils.equals(action.getName(), Lookup.LOOKUP_ACTION_NAME)) {
            final String dataSetId = action.getParameters().get(Lookup.Parameters.LOOKUP_DS_ID.getKey());
            if (StringUtils.isEmpty(dataSetId)) {
                throw new IllegalArgumentException("A lookup action must have a lookup dataset id: " + action);
            } else {
                final String joinOn = action.getParameters().get(Lookup.Parameters.LOOKUP_JOIN_ON.getKey());
                result = getLookupDataset(dataSetId, joinOn);
            }
        } else {
            throw new IllegalArgumentException(
                    "Trying to retrieve a lookup dataset from the following action: " + action.getName());
        }
        return result;
    }

    private static HashMap<String, HashMap<String, DataSetRow>> retrieveLookupDataSets(List<Action> actions) {
        final HashMap<String, HashMap<String, DataSetRow>> result = new HashMap<>();
        actions.stream().filter(a -> StringUtils.equals(a.getName(), Lookup.LOOKUP_ACTION_NAME))
                .forEach(a -> result.put(a.getParameters().get(Lookup.Parameters.LOOKUP_DS_ID.getKey()),
                        retrieveLookupDataSetFromAction(a)));
        return result;
    }

    private static Function<IndexedRecord, IndexedRecord> internalParse(InputStream preparation) {
        assertPreparation(preparation);
        ObjectMapper mapper = new ObjectMapper();

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

        HashMap<String, HashMap<String, DataSetRow>> lookupDataSets = retrieveLookupDataSets(actions);

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

    public Function<IndexedRecord, IndexedRecord> parse(String preparation, String encoding) throws UnsupportedEncodingException {
        assertPreparation(preparation);
        return parse(new ByteArrayInputStream(preparation.getBytes(encoding)));
    }

    public Function<IndexedRecord, IndexedRecord> parse(InputStream preparation) {
        return internalParse(preparation);
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
            if (!loaded){
                LOG.info("Adding cached data sets to LookupDataSetManager");
                lookupDataSets.entrySet().stream().forEach(entry -> LookupDatasetsManager.put(entry.getKey(), entry.getValue()));
                lookupDataSets.entrySet().stream().forEach(entry -> LOG.info("cached a dataset with id: "+entry.getKey()));
                loaded = true;
            }
            LOG.info("Bouba");
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
