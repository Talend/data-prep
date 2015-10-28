package org.talend.dataprep.transformation.api.action.metadata.common;

import static org.talend.dataprep.api.preparation.Action.Builder.builder;
import static org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters.*;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.transformation.api.action.DataSetMetadataAction;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.validation.ActionMetadataValidation;

/**
 * Model an action to perform on a dataset.
 * <p>
 * An "action" is created for each row, see {@link ActionMetadata#create(Map)}.
 * <p>
 * The actions are called from the
 */
public abstract class ActionMetadata {

    public static final String ACTION_BEAN_PREFIX = "action#"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionMetadata.class);

    /** The validator. */
    @Autowired
    private ActionMetadataValidation validator;

    @Autowired
    private FilterService filterService;

    /**
     * <p>
     * Adapts the current action metadata to the column. This method may return <code>this</code> if no action specific
     * change should be done. It may return a different instance with information from column (like a default value
     * inferred from column's name).
     * </p>
     * <p>
     * Implementations are also expected to return <code>this</code> if {@link #acceptColumn(ColumnMetadata)} returns
     * <code>false</code>.
     * </p>
     *
     * @param column A {@link ColumnMetadata column} information.
     * @return <code>this</code> if any of the following is true:
     * <ul>
     *     <li>no change is required.</li>
     *     <li>column type is not {@link #acceptColumn(ColumnMetadata) accepted} for current action.</li>
     * </ul>
     * OR a new action metadata with information extracted from
     * <code>column</code>.
     */
    public ActionMetadata adapt(ColumnMetadata column) {
        return this;
    }

    /**
     * @return A unique name used to identify action.
     */
    public abstract String getName();

    /**
     * @return A 'category' for the action used to group similar actions (eg. 'math', 'repair'...).
     * @see org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory
     */
    public abstract String getCategory();

    /**
     * Return true if the action can be applied to the given column metadata.
     *
     * @param column the column metadata to transform.
     * @return true if the action can be applied to the given column metadata.
     */
    public abstract boolean acceptColumn(final ColumnMetadata column);

    /**
     * @return The label of the parameter, translated in the user locale.
     * @see MessagesBundle
     */
    public String getLabel() {
        return MessagesBundle.getString("action." + getName() + ".label");
    }

    /**
     * @return The description of the parameter, translated in the user locale.
     * @see MessagesBundle
     */
    public String getDescription() {
        return MessagesBundle.getString("action." + getName() + ".desc");
    }

    /**
     * @return True if the action is dynamic (i.e the parameters depends on the context
     * (dataset/preparation/previous_actions)
     */
    public boolean isDynamic() {
        return false;
    }

    /**
     * Get the columnId from parameters
     *
     * @param parameters the transformation parameters
     * @return the column id
     */
    private String getColumnId(final Map<String, String> parameters) {
        return parameters.get(COLUMN_ID.getKey());
    }

    /**
     * Get the rowId from parameters
     *
     * @param parameters the transformation parameters
     * @return the row id
     */
    private Long getRowId(final Map<String, String> parameters) {
        final String rowIdAsString = parameters.get(ROW_ID.getKey());
        if (rowIdAsString != null) {
            return Long.parseLong(rowIdAsString);
        }
        return null;
    }

    /**
     * Get the scope category from parameters
     *
     * @param parameters the transformation parameters
     * @return the scope
     */
    private ScopeCategory getScope(final Map<String, String> parameters) {
        return ScopeCategory.from(parameters.get(SCOPE.getKey()));
    }

    /**
     * Get the row filter from parameters.
     *
     * @param parameters the transformation parameters
     * @return A {@link Predicate filter} for data set rows.
     */
    protected Predicate<DataSetRow> getFilter(Map<String, String> parameters) {
        return filterService.build(parameters.get(ImplicitParameters.FILTER.getKey()));
    }

    /**
     * Return true if the action can be applied to the given scope.
     *
     * @param scope the scope to test
     * @return true if the action can be applied to the given scope.
     */
    public final boolean acceptScope(final ScopeCategory scope) {
        switch (scope) {
        case CELL:
            return this instanceof CellAction;
        case LINE:
            return this instanceof RowAction;
        case COLUMN:
            return this instanceof ColumnAction;
        case TABLE:
            return this instanceof DataSetAction;
        default:
            return false;
        }
    }

    /**
     * Creates an {@link Action action} based on provided parameters.
     *
     * @param parameters Action-dependent parameters, can be empty.
     * @return An {@link Action action} that can implement {@link DataSetRowAction row action} and/or
     * {@link DataSetMetadataAction metadata action}.
     */
    public final Action create(final Map<String, String> parameters) {
        validator.checkScopeConsistency(this, parameters);

        final Long rowId = getRowId(parameters);
        final String columnId = getColumnId(parameters);
        final ScopeCategory scope = getScope(parameters);
        final Predicate<DataSetRow> filter = getFilter(parameters);

        return builder().withRow((row, context) -> {
            if (implicitFilter() && !filter.test(row)) {
                // Return non-modifiable row since it didn't pass the filter (but metadata might be modified).
                row = row.unmodifiable();
            }
            // Select the correct method to call depending on scope.
            switch (scope) {
            case CELL:
                if (rowId != null && rowId.equals(row.getTdpId())) {
                    ((CellAction) this).applyOnCell(row, context, parameters, rowId, columnId);
                }
                break;
            case COLUMN:
                ((ColumnAction) this).applyOnColumn(row, context, parameters, columnId);
                break;
            case LINE:
                if (rowId != null && rowId.equals(row.getTdpId())) {
                    ((RowAction) this).applyOnRow(row, context, parameters, rowId);
                }
                break;
            case TABLE:
                ((DataSetAction) this).applyOnDataSet(row, context, parameters);
                break;
            default:
                LOGGER.warn("Is there a new action scope ??? {}", scope);
                break;
            }
            // For following actions, returns the row as modifiable to allow further modifications.
            return row.modifiable();
        }).build();
    }

    /**
     * @return <code>true</code> if there should be an implicit filtering before the action gets executed. Actions that
     * don't want to take care of filtering should return <code>true</code> (default). Implementations may override this
     * method and return <code>false</code> if they want to handle themselves filtering.
     */
    protected boolean implicitFilter() {
        return true;
    }

    /**
     * @return The list of parameters required for this Action to be executed.
     **/
    public List<Parameter> getParameters() {
        return ImplicitParameters.getParameters();
    }

}
