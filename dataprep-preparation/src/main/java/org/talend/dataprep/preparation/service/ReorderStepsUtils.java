package org.talend.dataprep.preparation.service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.transformation.actions.category.ActionScope;
import org.talend.dataprep.transformation.actions.column.DeleteColumn;

import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;

@Component
public class ReorderStepsUtils {

    /**
     * Checks if the reordering implied by the specified list of steps is legal.
     * <p>
     * <p>
     * TODO: When the column metadata of the dataset become available to the preparation, we should improve this method.
     *
     * @param appendSteps the specified list of steps
     * @return either <tt>true</tt> if the specified list of steps have a step that use a column before it is created by
     * a following step or deleted by a preceding step or <t>false</t> otherwise
     */
    boolean isStepOrderInvalid(List<AppendStep> appendSteps) {
        // Add all the columns created by steps as not available at the beginning
        final Set<String> notYetAvailableColumnsIds = appendSteps
                .stream()
                .flatMap(step -> step.getDiff().getCreatedColumns().stream())
                .collect(Collectors.toSet());

        return appendSteps.stream().noneMatch(step -> {
            for (Action action : step.getActions()) {
                final Map<String, String> parameters = action.getParameters();
                final String[] columnIds;
                if (ActionScope.MULTI_COLUMNS
                        .getDisplayName()
                        .equals(action.getParameters().get(ImplicitParameters.SCOPE.getKey()))) {
                    columnIds = ActionsUtils.extractColumnsId(parameters.get("column_ids"));
                } else {
                    columnIds = ActionsUtils.extractColumnsId(parameters.get(ImplicitParameters.COLUMN_ID.getKey()));
                }

                // remove the created columns from not available columns
                notYetAvailableColumnsIds.removeAll(step.getDiff().getCreatedColumns());

                for (String columnId : columnIds) {
                    // if the columns is no
                    if (notYetAvailableColumnsIds.contains(columnId)) {
                        return true;
                    }

                    // add removed columns to non available
                    if (StringUtils.equalsIgnoreCase(DeleteColumn.DELETE_COLUMN_ACTION_NAME, action.getName())) {
                        notYetAvailableColumnsIds.add(columnId);
                    }
                }
            }
            return false;
        });
    }

    /**
     * Renames the created columns according to their order of creation starting from the minimum created column.
     *
     * @param appendSteps the specified list of append steps
     */
    void renameCreatedColumns(List<AppendStep> appendSteps) {

        final List<String> createdColumns = appendSteps
                .stream()
                .flatMap(step -> step.getDiff().getCreatedColumns().stream())
                .collect(Collectors.toList());

        if (createdColumns.isEmpty()) {
            return;
        }

        //retrieve the minimum index
        final int firstIndex = Integer.parseInt(createdColumns.stream().min(String::compareTo).get());

        // map old created column names to the new ones
        final DecimalFormat format = new DecimalFormat("0000");
        Map<String, String> rename = new HashMap<>();
        IntStream.range(0, createdColumns.size()).forEach(i -> {
            rename.put(createdColumns.get(i), format.format(i + firstIndex));
        });

        // walk over the list of append steps and change names (id) of created columns
        appendSteps.stream().forEach(step -> {
            // first for created columns
            List<String> renamedCreatedColumns = step.getDiff().getCreatedColumns().stream().map(s -> {
                if (rename.containsKey(s)) {
                    return rename.get(s);
                } else {
                    return s;
                }
            }).collect(Collectors.toList());

            // then within actions
            step.getDiff().setCreatedColumns(renamedCreatedColumns);
            for (Action action : step.getActions()) {
                final Map<String, String> parameters = action.getParameters();
                final String columnId = parameters.get(ImplicitParameters.COLUMN_ID.getKey());

                if (rename.containsKey(columnId)) {
                    parameters.put(ImplicitParameters.COLUMN_ID.getKey(), rename.get(columnId));
                }
            }
        });
    }

}
