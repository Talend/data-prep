//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.common;

import static org.talend.dataprep.api.preparation.Action.Builder.builder;
import static org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters.ROW_ID;
import static org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters.SCOPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.transformation.api.action.DataSetMetadataAction;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionScope;
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
     * <p>
     * Adapts the current action metadata to the scope. This method may return <code>this</code> if no action specific
     * change should be done. It may return a different instance with information from scope (like a different label).
     * </p>
     *
     * @param scope A {@link ScopeCategory scope}.
     * @return <code>this</code> if no change is required.
     * OR a new action metadata with information extracted from
     * <code>scope</code>.
     */
    public ActionMetadata adapt(final ScopeCategory scope) {
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
     * @return The label of the action, translated in the user locale.
     * @see MessagesBundle
     */
    public String getLabel() {
        return MessagesBundle.getString("action." + getName() + ".label");
    }

    /**
     * @return The description of the action, translated in the user locale.
     * @see MessagesBundle
     */
    public String getDescription() {
        return MessagesBundle.getString("action." + getName() + ".desc");
    }

    /**
     * @return The url of the optionnal help page.
     * @see MessagesBundle
     */
    public String getDocUrl() {
        return MessagesBundle.getString("action." + getName() + ".url", StringUtils.EMPTY);
    }

    /**
     * Defines the list of scopes this action belong to.
     * 
     * Scope scope is a concept that allow us to describe on which scope(s) each action can be applied.
     *
     * @return list of scopes of this action
     * @see ActionScope
     */
    public List<String> getActionScope(){
        return new ArrayList<>();
    }

    /**
     * @return True if the action is dynamic (i.e the parameters depends on the context
     * (dataset/preparation/previous_actions)
     */
    public boolean isDynamic() {
        return false;
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
        final Predicate<DataSetRow> predicate;
        if (filterService == null) {
            predicate = r -> true;
        } else {
            predicate = filterService.build(parameters.get(ImplicitParameters.FILTER.getKey()));
        }
        final ScopeCategory scope = getScope(parameters);
        if (scope == ScopeCategory.CELL || scope == ScopeCategory.LINE) {
            final Long rowId;
            final String rowIdAsString = parameters.get(ROW_ID.getKey());
            if (StringUtils.isNotBlank(rowIdAsString)) {
                rowId = Long.parseLong(rowIdAsString);
            } else {
                rowId = null;
            }
            final Predicate<DataSetRow> rowFilter = r -> ObjectUtils.equals(r.getTdpId(), rowId);
            return filterService == null ? rowFilter : predicate.and(rowFilter);
        } else {
            return predicate;
        }
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
            case DATASET:
                return this instanceof DataSetAction;
            default:
                return false;
        }
    }

    /**
     * Called by transformation process <b>before</b> the first transformation occurs. This method allows action
     * implementation to compute reusable objects in actual transformation execution. Implementations may also indicate
     * that action is not applicable and should be discarded (
     * {@link org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus#CANCELED}.
     * 
     * @param actionContext The action context that contains the parameters and allows compile step to change action
     * status.
     * @see ActionContext#setActionStatus(ActionContext.ActionStatus)
     */
    public void compile(ActionContext actionContext) {
        final RowMetadata input = actionContext.getInputRowMetadata();
        final ScopeCategory scope = actionContext.getScope();
        if (scope != null) {
            switch (scope) {
            case CELL:
            case COLUMN:
                // Stop action if: there's actually column information in input AND column is not found
                if (input != null && !input.getColumns().isEmpty() && input.getById(actionContext.getColumnId()) == null) {
                    actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
                    return;
                }
                break;
            case LINE:
            case DATASET:
            default:
                break;

            }
        }
        actionContext.setActionStatus(ActionContext.ActionStatus.OK);
    }

    /**
     * Creates an {@link Action action} based on provided parameters.
     *
     * @param parameters Action-dependent parameters, can be empty.
     * @return An {@link Action action} that can implement {@link DataSetRowAction row action} and/or
     * {@link DataSetMetadataAction metadata action}.
     */
    public final Action create(final Map<String, String> parameters) {
        if (validator != null) {
            validator.checkScopeConsistency(this, parameters);
        }
        final Map<String, String> parametersCopy = new HashMap<>(parameters);
        final ScopeCategory scope = getScope(parametersCopy);
        final Predicate<DataSetRow> filter = getFilter(parametersCopy);

        return builder().withCompile((actionContext) -> {
            try {
                actionContext.setParameters(parametersCopy);
                compile(actionContext);
            } catch (Exception e) {
                LOGGER.error("Unable to use action '{}' due to unexpected error.", this.getName(), e);
                actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }
        })
        .withRow((row, context) -> {
            try {
                if (implicitFilter() && !filter.test(row)) {
                    // Return non-modifiable row since it didn't pass the filter (but metadata might be modified).
                    row = row.unmodifiable();
                }
                // Select the correct method to call depending on scope.
                switch (scope) {
                case CELL:
                    ((CellAction) this).applyOnCell(row, context);
                    break;
                case LINE:
                    ((RowAction) this).applyOnLine(row, context);
                    break;
                case COLUMN:
                    ((ColumnAction) this).applyOnColumn(row, context);
                    break;
                case DATASET:
                    ((DataSetAction) this).applyOnDataSet(row, context);
                    break;
                default:
                    LOGGER.warn("Is there a new action scope ??? {}", scope);
                    break;
                }
                // For following actions, returns the row as modifiable to allow further modifications.
                return row.modifiable();
            } catch (Exception e) {
                LOGGER.error("Unable to use action '{}' (parameters: {}) due to unexpected error.", this.getName(), parameters, e);
                context.setActionStatus(ActionContext.ActionStatus.CANCELED);
                return row.modifiable();
            }
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
