// ============================================================================
//
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

package org.talend.dataprep.transformation.actions.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import org.apache.avro.generic.GenericRecord;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.category.ActionScope;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Adapter for {@link ActionDefinition} to have default implementation and behavior for actions. Every dataprep actions
 * derive from it but it is not an obligation.
 */
public abstract class AbstractActionMetadata implements InternalActionDefinition {

    public static final String ACTION_BEAN_PREFIX = "action#"; //$NON-NLS-1$

    @Override
    public ActionDefinition adapt(ColumnMetadata column) {
        return this;
    }

    /**
     * <p>
     * Adapts the current action metadata to the scope. This method may return <code>this</code> if no action specific
     * change should be done. It may return a different instance with information from scope (like a different label).
     * </p>
     *
     * @param scope A {@link ScopeCategory scope}.
     * @return <code>this</code> if no change is required. OR a new action metadata with information extracted from
     * <code>scope</code>.
     */
    @Override
    public ActionDefinition adapt(final ScopeCategory scope) {
        return this;
    }

    /**
     * @return A unique name used to identify action.
     */
    @Override
    public abstract String getName();

    /**
     * @return A 'category' for the action used to group similar actions (eg. 'math', 'repair'...).
     * @see ActionCategory
     */
    @Override
    public abstract String getCategory();

    /**
     * Return true if the action can be applied to the given column metadata.
     *
     * @param column the column metadata to transform.
     * @return true if the action can be applied to the given column metadata.
     */
    @Override
    public abstract boolean acceptField(final ColumnMetadata column);

    /**
     * @return The label of the action, translated in the user locale.
     * @see MessagesBundle
     */
    @Override
    public String getLabel() {
        return ActionsBundle.INSTANCE.actionLabel(this, Locale.ENGLISH, getName());
    }

    /**
     * @return The description of the action, translated in the user locale.
     * @see MessagesBundle
     */
    @Override
    public String getDescription() {
        return ActionsBundle.INSTANCE.actionDescription(this, Locale.ENGLISH, getName());
    }

    @Override
    public String getDocUrl() {
        return ActionsBundle.INSTANCE.actionDocUrl(this, Locale.ENGLISH, getName());
    }

    /**
     * Defines the list of scopes this action belong to.
     * <p>
     * Scope scope is a concept that allow us to describe on which scope(s) each action can be applied.
     *
     * @return list of scopes of this action
     * @see ActionScope
     */
    @Override
    public List<String> getActionScope() {
        return new ArrayList<>();
    }

    /**
     * TODO Only here for JSON serialization purposes.
     *
     * @return True if the action is dynamic (i.e the parameters depends on the context
     * (dataset/preparation/previous_actions)
     */
    @Override
    public boolean isDynamic() {
        return false;
    }

    /**
     * Return true if the action can be applied to the given scope.
     *
     * @param scope the scope to test
     * @return true if the action can be applied to the given scope.
     */
    @Override
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
     * {@link ActionContext.ActionStatus#CANCELED}.
     *
     * @param actionContext The action context that contains the parameters and allows compile step to change action
     *                      status.
     * @see ActionContext#setActionStatus(ActionContext.ActionStatus)
     */
    @Override
    public void compile(ActionContext actionContext) {
        final RowMetadata input = actionContext.getRowMetadata();
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
     * @return <code>true</code> if there should be an implicit filtering before the action gets executed. Actions that
     * don't want to take care of filtering should return <code>true</code> (default). Implementations may override this
     * method and return <code>false</code> if they want to handle themselves filtering.
     */
    @Override
    public boolean implicitFilter() {
        return true;
    }

    /**
     * @return The list of parameters required for this Action to be executed.
     **/
    @Override
    public List<Parameter> getParameters() {
        return ActionsBundle.attachToAction(ImplicitParameters.getParameters(), this);
    }

    @JsonIgnore
    @Override
    public abstract Set<ActionDefinition.Behavior> getBehavior();

    @Override
    public Function<GenericRecord, GenericRecord> action(List<Parameter> parameters) {
        return r -> r;
    }
}
