package org.talend.dataprep.transformation.pipeline.builder;

import static java.util.stream.Collectors.toSet;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.COLUMN_ID;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

class ActionsStaticProfiler {

    private final ActionRegistry actionRegistry;

    public ActionsStaticProfiler(final ActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
    }

    /**
     * Get the actions metadata by actions
     */
    public Map<Action, ActionDefinition> getActionMetadataByAction(final List<Action> actions) {
        final Map<Action, ActionDefinition> actionToMetadata = new HashMap<>(actions.size());
        for (final Action action : actions) {
            final ActionDefinition actionMetadata = actionRegistry.get(action.getName());
            actionToMetadata.put(action, actionMetadata);
        }
        return actionToMetadata;
    }

    public ActionsProfile profile(final List<ColumnMetadata> columns, final List<Action> actions,
            final Map<Action, ActionDefinition> actionToMetadata) {
        final Map<Action, ActionDefinition> metadataByAction = actionToMetadata == null ? getActionMetadataByAction(actions)
                : actionToMetadata;

        // Compile actions
        final Set<String> originalColumns = columns.stream().map(ColumnMetadata::getId).collect(toSet());
        final Set<String> valueModifiedColumns = new HashSet<>();
        final Set<String> metadataModifiedColumns = new HashSet<>();
        int createColumnActions = 0;

        for (final Action action : actions) {
            final ActionDefinition actionMetadata = actionRegistry.get(action.getName());
            metadataByAction.put(action, actionMetadata);
        }
        // Analyze what columns to look at during analysis
        for (Map.Entry<Action, ActionDefinition> entry : metadataByAction.entrySet()) {
            final ActionDefinition actionMetadata = entry.getValue();
            final Action action = entry.getKey();
            Set<ActionDefinition.Behavior> behavior = actionMetadata.getBehavior();
            for (ActionDefinition.Behavior currentBehavior : behavior) {
                switch (currentBehavior) {
                case VALUES_ALL:
                    // All values are going to be changed, and all original columns are going to be modified.
                    valueModifiedColumns.addAll(originalColumns);
                    break;
                case METADATA_CHANGE_TYPE:
                    valueModifiedColumns.add(action.getParameters().get(COLUMN_ID.getKey()));
                    metadataModifiedColumns.add(action.getParameters().get(COLUMN_ID.getKey()));
                    break;
                case VALUES_COLUMN:
                    valueModifiedColumns.add(action.getParameters().get(COLUMN_ID.getKey()));
                    break;
                case VALUES_MULTIPLE_COLUMNS:
                    // Add the action's source column
                    valueModifiedColumns.add(action.getParameters().get(COLUMN_ID.getKey()));
                    // ... then add all column parameter (COLUMN_ID is string, not column)
                    final List<Parameter> parameters = actionMetadata.getParameters();
                    valueModifiedColumns.addAll(parameters.stream() //
                            .filter(parameter -> ParameterType.valueOf(parameter.getType().toUpperCase()) == ParameterType.COLUMN) //
                            .map(parameter -> action.getParameters().get(parameter.getName())) //
                            .collect(Collectors.toList()));
                    break;
                case METADATA_COPY_COLUMNS:
                case METADATA_CREATE_COLUMNS:
                    createColumnActions++;
                    break;
                case METADATA_DELETE_COLUMNS:
                case METADATA_CHANGE_NAME:
                    // Do nothing: no need to re-analyze where only name was changed.
                    break;
                default:
                    break;
                }
            }
        }

        // when values are modified, we need to do a full analysis (schema + invalid + stats)
        boolean needFullAnalysis = !valueModifiedColumns.isEmpty() || createColumnActions > 0;
        // when only metadata is modified, we need to re-evaluate the invalids entries
        boolean needOnlyInvalidAnalysis = !needFullAnalysis && !metadataModifiedColumns.isEmpty();
        // only the columns with modified values or new columns need the schema + stats analysis
        Predicate<ColumnMetadata> filterForFullAnalysis = c -> valueModifiedColumns.contains(c.getId())
                || !originalColumns.contains(c.getId());
        // only the columns with metadata change or value changes need to re-evaluate invalids
        Predicate<ColumnMetadata> filterForInvalidAnalysis = filterForFullAnalysis
                .or(c -> metadataModifiedColumns.contains(c.getId()));
        Predicate<ColumnMetadata> filterForPatternAnalysis = filterForFullAnalysis
                .or(c -> metadataModifiedColumns.contains(c.getId()));

        return new ActionsProfile(needFullAnalysis, needOnlyInvalidAnalysis, filterForFullAnalysis, filterForInvalidAnalysis,
                filterForPatternAnalysis);
    }
}
