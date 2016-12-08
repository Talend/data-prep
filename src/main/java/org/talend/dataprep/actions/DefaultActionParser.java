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

import static org.talend.dataprep.api.action.ActionDefinition.Behavior.FORBID_DISTRIBUTED;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.METADATA_CREATE_COLUMNS;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.actions.resources.DictionaryFunctionResourceProvider;
import org.talend.dataprep.actions.resources.FunctionResource;
import org.talend.dataprep.actions.resources.FunctionResourceProvider;
import org.talend.dataprep.actions.resources.LookupFunctionResourceProvider;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.dataset.StatisticsAdapter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.common.ActionFactory;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;
import org.talend.dataprep.transformation.pipeline.Pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultActionParser implements ActionParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultActionParser.class);

    private static final ActionRegistry actionRegistry = new ClassPathActionRegistry(
            "org.talend.dataprep.transformation.actions");

    private static final ActionFactory actionFactory = new ActionFactory();

    private static final ObjectMapper mapper = new ObjectMapper();

    private final String apiUrl;

    private final String login;

    private final String password;

    private final List<FunctionResourceProvider> providers;

    private boolean allowNonDistributedActions = false;

    public DefaultActionParser(String apiUrl, String login, String password) {
        this.apiUrl = apiUrl;
        this.login = login;
        this.password = password;
        this.providers = Arrays.asList( //
                new LookupFunctionResourceProvider(apiUrl, login, password), //
                new DictionaryFunctionResourceProvider(actionRegistry, apiUrl, login, password) //
        );
    }

    /**
     * Indicate if parser should skip non distributed actions (actions that can't run in distributed contexts).
     *
     * @param allowNonDistributedActions <code>true</code> to allow those actions, <code>false</code> otherwise. Defaults
     * to <code>false</code> (non distributed actions are <b>not</b> allowed).
     */
    public void setAllowNonDistributedActions(boolean allowNonDistributedActions) {
        this.allowNonDistributedActions = allowNonDistributedActions;
    }

    private static void assertPreparation(Object preparation) {
        if (preparation == null) {
            throw new IllegalArgumentException("Preparation can not be null.");
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
        final ActionDefinition actionDefinition = actionRegistry.get(actionName);

        if (actionDefinition == null) {
            LOGGER.error("No action implementation found for '{}'.", actionName);
        } else {
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

            LOGGER.info("Action metadata found for '{}': {}", actionName, actionDefinition.getClass().getName());
            final Iterator<Map.Entry<String, JsonNode>> parameters = n.get("parameters").fields();
            Map<String, String> parametersAsMap = new HashMap<>();
            while (parameters.hasNext()) {
                final Map.Entry<String, JsonNode> next = parameters.next();
                final JsonNode value = next.getValue();
                parametersAsMap.put(next.getKey(), parseParameter(value));
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

    private String getPreparation(RemoteResourceGetter clientFormLogin, String preparationId) {
        LOGGER.debug("Retrieving preparation '{}'", preparationId);
        return clientFormLogin.retrievePreparation(apiUrl, login, password, preparationId);
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
            return new NoOpFunction();
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

        // get the list of resources for function
        FunctionResource[] resources = providers.stream() //
                .map(provider -> provider.get(actions)) //
                .collect(Collectors.toList()) //
                .toArray(new FunctionResource[providers.size()]);

        LOGGER.trace("The initial row metadata is: " + rowMetadata);

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
        return new SerializableFunction(pipeline, stackedNode, rowMetadata, resources);
    }

    @Override
    public Function<IndexedRecord, IndexedRecord> parse(String preparationId) {
        final RemoteResourceGetter clientFormLogin = new RemoteResourceGetter();
        String preparation = getPreparation(clientFormLogin, preparationId);
        return internalParse(IOUtils.toInputStream(preparation));
    }

    // Can't use identity() because result isn't serializable
    private static class NoOpFunction implements Function<IndexedRecord, IndexedRecord>, Serializable {

        @Override
        public IndexedRecord apply(IndexedRecord indexedRecord) {
            return indexedRecord;
        }
    }
}
