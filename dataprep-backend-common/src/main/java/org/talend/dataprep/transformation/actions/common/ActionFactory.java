package org.talend.dataprep.transformation.actions.common;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.DataSetMetadataAction;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.validation.ActionMetadataValidation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static org.talend.dataprep.api.preparation.Action.Builder.builder;

@Component
public class ActionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionMetadata.class);

    /** The validator. */
    @Autowired(required = false)
    private ActionMetadataValidation validator;

    @Autowired(required = false)
    private FilterService filterService = (s, metadata) -> r -> true;

    /**
     * Get the scope category from parameters
     *
     * @param parameters the transformation parameters
     * @return the scope
     */
    private ScopeCategory getScope(final Map<String, String> parameters) {
        return ScopeCategory.from(parameters.get(ImplicitParameters.SCOPE.getKey()));
    }

    /**
     * Creates an {@link Action action} based on provided parameters.
     *
     * @param parameters Action-dependent parameters, can't be null and must contain {@link ImplicitParameters}.
     * @return An {@link Action action} that can implement {@link DataSetRowAction row action} and/or
     * {@link DataSetMetadataAction metadata action}.
     */
    public final Action create(ActionMetadata metadata, Map<String, String> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Parameters cannot be null.");
        }
        if (validator != null) {
            validator.checkScopeConsistency(metadata, parameters);
        }
        final Map<String, String> parametersCopy = new HashMap<>(parameters);
        final ScopeCategory scope = getScope(parametersCopy);

        return builder().withName(metadata.getName()) //
                .withParameters(parametersCopy) //
                .withCompile(actionContext -> {
                    try {
                        actionContext.setParameters(parametersCopy);
                        metadata.compile(actionContext);
                        actionContext.setFilter(getFilter(parametersCopy, actionContext.getRowMetadata()));
                    } catch (Exception e) {
                        LOGGER.error("Unable to use action '{}' due to unexpected error.", metadata.getName(), e);
                        actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
                    }
                }) //
                .withRow((row, context) -> handleRow(metadata, parameters, scope, row, context)) //
                .build();
    }

    /**
     * Get the row filter from parameters.
     *
     * @param parameters the transformation parameters
     * @param rowMetadata Row metadata to used to obtain information (valid/invalid, types...)
     * @return A {@link Predicate filter} for data set rows.
     */
    private Predicate<DataSetRow> getFilter(Map<String, String> parameters, RowMetadata rowMetadata) {
        final String filterAsString = parameters.get(ImplicitParameters.FILTER.getKey());
        final Predicate<DataSetRow> predicate = filterService.build(filterAsString, rowMetadata);
        final ScopeCategory scope = getScope(parameters);
        if (scope == ScopeCategory.CELL || scope == ScopeCategory.LINE) {
            final Long rowId;
            final String rowIdAsString = parameters.get(ImplicitParameters.ROW_ID.getKey());
            if (StringUtils.isNotBlank(rowIdAsString)) {
                rowId = Long.parseLong(rowIdAsString);
            } else {
                rowId = null;
            }
            final Predicate<DataSetRow> rowFilter = r -> Objects.equals(r.getTdpId(), rowId);
            return predicate.and(rowFilter);
        } else {
            return predicate;
        }
    }

    private DataSetRow handleRow(final ActionMetadata metadata, //
            final Map<String, String> parameters, //
            final ScopeCategory scope, //
            final DataSetRow row, //
            final ActionContext context) {
        try {
            final DataSetRow actionRow;
            if (metadata.implicitFilter() && !context.getFilter().test(row)) {
                // Return non-modifiable row since it didn't pass the filter (but metadata might be modified).
                actionRow = row.unmodifiable();
            } else {
                actionRow = row;
            }
            // Select the correct method to call depending on scope.
            switch (scope) {
            case CELL:
                ((CellAction) metadata).applyOnCell(actionRow, context);
                break;
            case LINE:
                ((RowAction) metadata).applyOnLine(actionRow, context);
                break;
            case COLUMN:
                ((ColumnAction) metadata).applyOnColumn(actionRow, context);
                break;
            case DATASET:
                ((DataSetAction) metadata).applyOnDataSet(actionRow, context);
                break;
            default:
                LOGGER.warn("Is there a new action scope ??? {}", scope);
                break;
            }
            // For following actions, returns the row as modifiable to allow further modifications.
            return actionRow.modifiable();
        } catch (Exception e) {
            LOGGER.error("Unable to use action '{}' (parameters: {}) due to unexpected error.", metadata.getName(), parameters,
                    e);
            context.setActionStatus(ActionContext.ActionStatus.CANCELED);
            return row.modifiable();
        }
    }

}
