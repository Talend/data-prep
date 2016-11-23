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
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.FORBID_DISTRIBUTED;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.METADATA_CREATE_COLUMNS;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang.StringUtils;
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

    private static final ActionRegistry actionRegistry = new ClassPathActionRegistry(
            "org.talend.dataprep.transformation.actions");

    private static final ActionFactory actionFactory = new ActionFactory();

    private static boolean allowNonDistributedActions = false;

    /**
     * Indicate if parser should skip non distributed actions (actions that can't run in distributed contexts).
     * 
     * @param allowNonDistributedActions <code>true</code> to allow those actions, <code>false</code> otherwise. Defaults
     * to <code>false</code>.
     */
    public static void setAllowNonDistributedActions(boolean allowNonDistributedActions) {
        DefaultActionParser.allowNonDistributedActions = allowNonDistributedActions;
    }

    private static void assertPreparation(Object actions) {
        if (actions == null) {
            throw new IllegalArgumentException("Actions can not be null.");
        }
    }

    private static List<Action> getActions(String actionsAsString) {
        ObjectMapper mapper = new ObjectMapper();
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

    // Parse a action JSON node and return the Action instance for this JSON description
    private static Action parseAction(JsonNode n) {
        String actionName = n.get("action").asText();
        LOGGER.info("New action: {}", actionName);
        final ActionDefinition actionDefinition = actionRegistry.get(actionName);

        if (actionDefinition == null) {
            LOGGER.error("No action implementation found for '{}'.", actionName);
            return null;
        }

        LOGGER.info("Action metadata found for '{}': {}", actionName, actionDefinition.getClass().getName());
        // Distributed run check for action
        final Set<ActionDefinition.Behavior> behavior = actionDefinition.getBehavior();
        // if non distributed actions are forbidden (e.g. running locally)
        if (!allowNonDistributedActions) {
            // if some actions cannot be run in distributed environment, let's see how bad it is...
            if (behavior.contains(FORBID_DISTRIBUTED)) {
                // actions that changes the schema (potentially really harmful for the preparation) throws an exception
                if (behavior.contains(METADATA_CREATE_COLUMNS)) {
                    throw new IllegalArgumentException("Action '" + actionName + "' cannot run in distributed environments.");
                }
                // else the action is just skipped
                else {
                    LOGGER.warn("Action '{}' cannot run in distributed environment, skip its execution.");
                    return null;
                }
            }
        }

        // Parameter parse
        final Iterator<Map.Entry<String, JsonNode>> parameters = n.get("parameters").fields();
        Map<String, String> parametersAsMap = new HashMap<>();
        while (parameters.hasNext()) {
            final Map.Entry<String, JsonNode> next = parameters.next();
            final JsonNode value = next.getValue();
            if (value.isTextual()) {
                parametersAsMap.put(next.getKey(), value.asText());
            } else if (value.isObject()) {
                parametersAsMap.put(next.getKey(), value.toString());
            } else if (value.isNull()) {
                parametersAsMap.put(next.getKey(), StringUtils.EMPTY);
            } else {
                LOGGER.warn("Unknown JSON node type in parameters '{}', falls back to asText().", value);
                parametersAsMap.put(next.getKey(), value.asText());
            }
        }
        // Create action
        final Action action = actionFactory.create(actionDefinition, parametersAsMap);
        LOGGER.info("Wrap action execution for '{}' with parameters '{}'.", actionDefinition.getClass().getName(),
                parametersAsMap);
        ActionContext context = new ActionContext(new TransformationContext());
        context.setParameters(parametersAsMap);

        LOGGER.info("New parsed action: {}", actionName);
        return action;
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

        // Build internal transformation pipeline
        final StackedNode stackedNode = new StackedNode();
        final Pipeline pipeline = Pipeline.Builder.builder() //
                .withActionRegistry(actionRegistry) //
                .withActions(getActions(actionNode.toString())) //
                .withInitialMetadata(rowMetadata, true) //
                .withOutput(() -> stackedNode) //
                .withStatisticsAdapter(new StatisticsAdapter(40)) //
                .withGlobalStatistics(false) //
                .allowMetadataChange(false) //
                .withAnalyzerService(Providers.get(AnalyzerService.class)) //
                .build();
        return new SerializableFunction(pipeline, stackedNode, rowMetadata);
    }

    @Override
    public Function<IndexedRecord, IndexedRecord> parse(String preparation, String encoding) throws UnsupportedEncodingException {
        assertPreparation(preparation);
        return parse(new ByteArrayInputStream(preparation.getBytes(encoding)));
    }

    @Override
    public Function<IndexedRecord, IndexedRecord> parse(InputStream preparation) {
        return internalParse(preparation);
    }

    private static class StackedNode extends BasicNode {

        /** For the serialization interface. */
        private static final long serialVersionUID = 1L;

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

        /** For the serialization interface. */
        private static final long serialVersionUID = 1L;

        private final Pipeline pipeline;

        private final StackedNode stackedNode;

        private final RowMetadata initialRowMetadata;

        private SerializableFunction(Pipeline pipeline, StackedNode stackedNode, RowMetadata initialRowMetadata) {
            this.pipeline = pipeline;
            this.stackedNode = stackedNode;
            this.initialRowMetadata = initialRowMetadata;
        }

        @Override
        public IndexedRecord apply(IndexedRecord indexedRecord) {
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
