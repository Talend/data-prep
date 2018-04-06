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
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

class ActionsStaticProfiler {

    private final ActionRegistry actionRegistry;

    /** Copy of ActionsUtils column creation parameter to optimize if a column is created. */
    public static final String CREATE_NEW_COLUMN = "create_new_column";

    public ActionsStaticProfiler(final ActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
    }

    /**
     * Get the actions metadata by actions
     */
    private Map<Action, ActionDefinition> getActionMetadataByAction(final List<RunnableAction> actions) {
        final Map<Action, ActionDefinition> actionToMetadata = new HashMap<>(actions.size());
        for (final Action action : actions) {
            final ActionDefinition actionMetadata = actionRegistry
                    .get(action.getName()) //
                    .adapt(ScopeCategory.from(action.getParameters().get(ImplicitParameters.SCOPE.getKey())));
            actionToMetadata.put(action, actionMetadata);
        }
        return actionToMetadata;
    }

    public ActionsProfile profile(final List<ColumnMetadata> columns, final List<RunnableAction> actions) {
        final Map<Action, ActionDefinition> metadataByAction = getActionMetadataByAction(actions);

        // Compile actions
        final Set<String> originalColumns = columns.stream().map(ColumnMetadata::getId).collect(toSet());
        final Set<String> valueModifiedColumns = new HashSet<>();
        final Set<String> metadataModifiedColumns = new HashSet<>();
        int createColumnActions = 0;

        // Analyze what columns to look at during analysis
        for (Map.Entry<Action, ActionDefinition> entry : metadataByAction.entrySet()) {
            final ActionDefinition actionMetadata = entry.getValue();
            final Action action = entry.getKey();
            Set<ActionDefinition.Behavior> behavior = actionMetadata.getBehavior();

            boolean createColumn = false;

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
                    final List<Parameter> parameters = actionMetadata.getParameters(Locale.US);
                    valueModifiedColumns.addAll(parameters
                            .stream() //
                            .filter(parameter -> ParameterType
                                    .valueOf(parameter.getType().toUpperCase()) == ParameterType.COLUMN) //
                            .map(parameter -> action.getParameters().get(parameter.getName())) //
                            .collect(Collectors.toList()));
                    break;
                case METADATA_COPY_COLUMNS:
                case METADATA_CREATE_COLUMNS:
                    createColumn = true;
                    break;
                case METADATA_DELETE_COLUMNS:
                case METADATA_CHANGE_NAME:
                    // Do nothing: no need to re-analyze where only name was changed.
                    break;
                default:
                    break;
                }
            }

            if (createColumn || isCreateColumnParameterOn(action)) {
                createColumnActions++;
            }
        }

        // when values are modified, we need to do a full analysis (schema + invalid + stats)
        boolean needFullAnalysis = !valueModifiedColumns.isEmpty() || createColumnActions > 0;
        // when only metadata is modified, we need to re-evaluate the invalids entries
        boolean needOnlyInvalidAnalysis = !needFullAnalysis && !metadataModifiedColumns.isEmpty();
        // only the columns with modified values or new columns need the schema + stats analysis
        SerializablePredicate<ColumnMetadata> filterForFullAnalysis =
                new FilterForFullAnalysis(originalColumns, valueModifiedColumns);
        // only the columns with metadata change or value changes need to re-evaluate invalids
        Predicate<ColumnMetadata> filterForInvalidAnalysis =
                new FilterForInvalidAnalysis(filterForFullAnalysis, metadataModifiedColumns);

        return new ActionsProfile(needFullAnalysis, needOnlyInvalidAnalysis, filterForFullAnalysis,
                filterForInvalidAnalysis, filterForInvalidAnalysis, metadataByAction);
    }

    private static boolean isCreateColumnParameterOn(Action action) {
        return action //
                .getParameters() //
                .entrySet() //
                .stream() //
                .anyMatch(e -> Objects.equals(e.getKey(), CREATE_NEW_COLUMN) && Boolean.parseBoolean(e.getValue()));
    }

    private static class FilterForFullAnalysis implements SerializablePredicate<ColumnMetadata> {

        private static final long serialVersionUID = 1L;

        private final Set<String> originalColumns;

        private final Set<String> valueModifiedColumns;

        private FilterForFullAnalysis(Set<String> originalColumns, Set<String> valueModifiedColumns) {
            this.originalColumns = originalColumns;
            this.valueModifiedColumns = valueModifiedColumns;
        }

        @Override
        public boolean test(ColumnMetadata columnMetadata) {
            return valueModifiedColumns.contains(columnMetadata.getId())
                    || !originalColumns.contains(columnMetadata.getId());
        }
    }

    private static class FilterForInvalidAnalysis implements SerializablePredicate<ColumnMetadata> {

        private static final long serialVersionUID = 1L;

        private final SerializablePredicate<ColumnMetadata> filterForFullAnalysis;

        private final Set<String> metadataModifiedColumns;

        private FilterForInvalidAnalysis(SerializablePredicate<ColumnMetadata> filterForFullAnalysis,
                Set<String> metadataModifiedColumns) {
            this.filterForFullAnalysis = filterForFullAnalysis;
            this.metadataModifiedColumns = metadataModifiedColumns;
        }

        @Override
        public boolean test(ColumnMetadata columnMetadata) {
            return filterForFullAnalysis.test(columnMetadata)
                    || metadataModifiedColumns.contains(columnMetadata.getId());
        }
    }

}
